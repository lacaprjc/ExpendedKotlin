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
import com.lacaprjc.expended.ui.accountWithTransactions.AccountWithTransactionsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var accountAdapter: AccountAdapter
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: AccountWithTransactionsViewModel by activityViewModels()
    private val accountViewModel: AccountViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentHomeBinding.bind(view)

        setupMenuButtons()
        setupAccountsList()
        subscribeObservers()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupMenuButtons() {
        binding.settingsButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionNavigationHomeToNavigationSettings())
        }

        binding.newAccountButton.setOnClickListener {
            accountViewModel.startNewAccount(accountAdapter.itemCount)
        }
    }

    private fun setupAccountsList() {
        accountAdapter = AccountAdapter(
            onClickListener = {
                findNavController().navigate(
                    HomeFragmentDirections.actionNavigationHomeToAccountDetailsFragment(
                        it.accountId
                    )
                )
            },
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

                    adapter.moveItem(from, to)
                    adapter.notifyItemMoved(from, to)

                    println("from: $from\nto:$to")
                    return true
                }

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) { /* ignored */ }
            })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun subscribeObservers() {
        lifecycleScope.launchWhenStarted {
            homeViewModel.getAllAccountsWithTransactions().distinctUntilChanged().collect { allAccountsWithTransactions ->
//                var currentMax = allAccountsWithTransactions.maxOf { it.account.orderPosition }
//                val fixedAccountsWithTransactions: List<AccountWithTransactions> = allAccountsWithTransactions.map {
//                    if (it.account.orderPosition == -1) {
//                        AccountWithTransactions(it.account.copy(orderPosition = ++currentMax), it.transactions)
//                    } else {
//                        it
//                    }
//                }
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
        }
    }
}
