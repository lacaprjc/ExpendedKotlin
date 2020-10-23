package com.lacaprjc.expended.ui.accountWithTransactions

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.lacaprjc.expended.R
import com.lacaprjc.expended.databinding.FragmentAccountWithTransactionsBinding
import com.lacaprjc.expended.listAdapters.TransactionAdapter
import com.lacaprjc.expended.ui.transaction.TransactionViewModel
import com.lacaprjc.expended.util.getAssociatedColor
import com.lacaprjc.expended.util.getAssociatedIcon
import com.lacaprjc.expended.util.toStringWithDecimalPlaces
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AccountWithTransactionsFragment : Fragment(R.layout.fragment_account_with_transactions) {
    private lateinit var transactionAdapter: TransactionAdapter

    private var _binding: FragmentAccountWithTransactionsBinding? = null
    private val binding get() = _binding!!

    private val transactionViewModel: TransactionViewModel by activityViewModels()
    private val accountWithTransactionsViewModel: AccountWithTransactionsViewModel by activityViewModels()
    private val args: AccountWithTransactionsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentAccountWithTransactionsBinding.bind(view)

        setupMenuButtons()
        setupTransactionList()
        observeData()
        transactionViewModel.setForAccountId(args.accountId)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupMenuButtons() {
        binding.newTransactionButton.setOnClickListener {
            transactionViewModel.startNewTransaction()
        }

        binding.accountNotesButton.setOnClickListener {
            // TODO: 10/22/20 show notes
        }
    }

    private fun setupTransactionList() {
        transactionAdapter = TransactionAdapter(
            onClickListener = {
                transactionViewModel.startEditingTransaction(it)
            }
        )

        val recyclerView = binding.transactionsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = transactionAdapter
        recyclerView.setHasFixedSize(true)
    }

    private fun observeData() {
        lifecycleScope.launchWhenStarted {
            transactionViewModel.getState().collect { state ->
                when (state) {
                    else -> {

                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            accountWithTransactionsViewModel.getAccountWithTransactions(args.accountId)
                .collect { accountWithTransactions ->
                    val accountColor = ContextCompat.getColor(
                        requireContext(),
                        accountWithTransactions.account.accountType.getAssociatedColor()
                    )

                    requireActivity().window.statusBarColor = accountColor
                    binding.accountDetailsGroup.backgroundTintList =
                        ColorStateList.valueOf(accountColor)

                    binding.accountName.setCompoundDrawablesWithIntrinsicBounds(
                        accountWithTransactions.account.accountType.getAssociatedIcon(),
                        0,
                        0,
                        0
                    )

                    binding.accountName.text = accountWithTransactions.account.name
                    binding.accountBalance.text = accountWithTransactions.transactions.sumOf {
                        it.amount
                    }.toStringWithDecimalPlaces(2)

                    transactionAdapter.submitTransactions(accountWithTransactions.transactions)
                }
        }
    }
}