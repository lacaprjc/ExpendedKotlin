package com.lacaprjc.expended.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lacaprjc.expended.R
import com.lacaprjc.expended.databinding.FragmentHomeBinding
import com.lacaprjc.expended.listAdapters.AccountAdapter
import com.lacaprjc.expended.model.AccountWithBalance
import com.lacaprjc.expended.ui.account.AccountViewModel
import com.lacaprjc.expended.ui.account.AccountViewModel.Mode.IDLE
import com.lacaprjc.expended.ui.account.AccountViewModel.Mode.REORDERING
import com.lacaprjc.expended.ui.accountWithTransactions.AccountWithTransactionsViewModel
import com.lacaprjc.expended.util.getAssociatedColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var accountAdapter: AccountAdapter
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val accountWithTransactionsViewModel: AccountWithTransactionsViewModel by activityViewModels()
    private val accountViewModel: AccountViewModel by activityViewModels()

    private var accountsWereReordered = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentHomeBinding.bind(view)

        setupMenuButtons()
        setupAccountsList()
        subscribeObservers()
    }

    override fun onDestroyView() {
        binding.accountsRecyclerView.adapter = null
        _binding = null
        // update the database if the items were reordered
        super.onDestroyView()
    }

    private fun updateAccountOrdering() {
        lifecycleScope.launch(Dispatchers.Default) {
            val reorderedList = mutableListOf<AccountWithBalance>()
            for (i in 0 until accountAdapter.itemCount) {
                val currentAccountWithBalance: AccountWithBalance =
                    accountAdapter.getItemAtPosition(i)
                reorderedList.add(
                    currentAccountWithBalance.copy(
                        account = currentAccountWithBalance.account.copy(
                            orderPosition = i
                        )
                    )
                )
            }

            reorderedList.forEach {
                accountViewModel.updateAccount(it.account)
            }
        }
    }

    private fun setupMenuButtons() {
        binding.settingsButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionNavigationHomeToNavigationSettings())
        }

        binding.newAccountButton.setOnClickListener {
            accountViewModel.startNewAccount(accountAdapter.itemCount)
        }

        binding.arrangeAccountsButton.setOnClickListener {
            if (accountViewModel.getReorderingMode().value == REORDERING && accountsWereReordered) {
                updateAccountOrdering()
            }

            accountViewModel.toggleReorderMode()
        }
    }

    private fun setupAccountsList() {
        accountAdapter = AccountAdapter(
            onClickListener = {
                findNavController().navigate(
                    HomeFragmentDirections.actionNavigationHomeToAccountWithTransactionsFragment(
                        it.accountId,
                        it.accountType.getAssociatedColor()
                    )
                )
            }
        )

        val recyclerView = binding.accountsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = accountAdapter
        recyclerView.setHasFixedSize(true)

        // drag to reorder
        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(UP or DOWN, 0) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val adapter = recyclerView.adapter as AccountAdapter
                    val from = viewHolder.adapterPosition
                    val to = target.adapterPosition

                    if (from != to) {
                        adapter.moveItem(from, to)
                        accountsWereReordered = true
                    }

                    return true
                }

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) { /* ignored */
                }

                override fun isLongPressDragEnabled(): Boolean {
                    return accountViewModel.getReorderingMode().value == REORDERING
                }
            })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun subscribeObservers() {
        accountWithTransactionsViewModel.getAllAccountsWithTransactions()
            .observe(viewLifecycleOwner) { allAccountsWithTransactions ->
                val sortedAccountsWithTransactions =
                    allAccountsWithTransactions.sortedBy { it.account.orderPosition }
                val accountsWithBalances = sortedAccountsWithTransactions.map {
                    AccountWithBalance(
                        it.account,
                        it.transactions.sumOf { transaction -> transaction.amount })
                }

                accountAdapter.submitAccounts(accountsWithBalances)

                binding.noAccountsText.visibility =
                    if (allAccountsWithTransactions.isNotEmpty()) View.INVISIBLE else View.VISIBLE
            }

        lifecycleScope.launchWhenStarted {
            launch {
                accountViewModel.getReorderingMode()
                    .collect { mode ->
                        when (mode) {
                            REORDERING -> {
                                binding.arrangeAccountsButton.setIconResource(R.drawable.ic_baseline_check_circle_outline_24)
                            }
                            IDLE -> {
                                binding.arrangeAccountsButton.setIconResource(R.drawable.ic_noun_reorder)
                            }
                        }
                    }
            }
        }
    }
}