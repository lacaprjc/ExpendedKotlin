package com.lacaprjc.expended.ui.accountWithTransactions

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lacaprjc.expended.R
import com.lacaprjc.expended.databinding.FragmentAccountWithTransactionsBinding
import com.lacaprjc.expended.listAdapters.TransactionAdapter
import com.lacaprjc.expended.model.Account
import com.lacaprjc.expended.model.AccountWithBalance
import com.lacaprjc.expended.ui.account.AccountViewModel
import com.lacaprjc.expended.ui.transaction.TransactionViewModel
import com.lacaprjc.expended.util.getAssociatedColor
import com.lacaprjc.expended.util.getAssociatedIcon
import com.lacaprjc.expended.util.toStringWithDecimalPlaces
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountWithTransactionsFragment : Fragment(R.layout.fragment_account_with_transactions) {
    private lateinit var transactionAdapter: TransactionAdapter

    private var _binding: FragmentAccountWithTransactionsBinding? = null
    private val binding get() = _binding!!

    private val transactionViewModel: TransactionViewModel by activityViewModels()
    private val accountViewModel: AccountViewModel by activityViewModels()
    private val accountWithTransactionsViewModel: AccountWithTransactionsViewModel by activityViewModels()
    private val args: AccountWithTransactionsFragmentArgs by navArgs()

    private lateinit var currentAccountWithBalance: AccountWithBalance

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
            accountViewModel.setToIdleState()
            transactionViewModel.startNewTransaction()
        }

        binding.editAccountButton.setOnClickListener {
            transactionViewModel.setToIdleState()
            accountViewModel.startEditingAccount(
                currentAccountWithBalance.account,
                currentAccountWithBalance.balance
            )
        }

        binding.deleteAccountButton.setOnClickListener {
            showDeleteDialog(currentAccountWithBalance.account)
        }
    }

    private fun setupTransactionList() {
        transactionAdapter = TransactionAdapter(
            onClickListener = {
                accountViewModel.setToIdleState()
                transactionViewModel.startEditingTransaction(it)
            }
        )

        val recyclerView = binding.transactionsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = transactionAdapter
        recyclerView.setHasFixedSize(true)
    }

    private fun observeData() {
        accountViewModel.getAccountType().observe(viewLifecycleOwner) {
            val accountColor = ContextCompat.getColor(
                requireContext(),
                it.getAssociatedColor()
            )

            binding.accountDetailsGroup.backgroundTintList =
                ColorStateList.valueOf(accountColor)
        }

        accountWithTransactionsViewModel.getAccountWithTransactions(args.accountId)
            .observe(viewLifecycleOwner) { accountWithTransactions ->
                currentAccountWithBalance = AccountWithBalance(
                    accountWithTransactions.account,
                    accountWithTransactions.transactions.sumOf { it.amount })

                binding.accountName.setCompoundDrawablesWithIntrinsicBounds(
                    accountWithTransactions.account.accountType.getAssociatedIcon(),
                    0,
                    0,
                    0
                )

                binding.accountName.text = accountWithTransactions.account.name
                binding.accountBalance.text =
                    currentAccountWithBalance.balance.toStringWithDecimalPlaces(2)

                transactionAdapter.submitTransactions(accountWithTransactions.transactions)
                accountViewModel.setAccountType(accountWithTransactions.account.accountType)
            }
    }

    private fun showDeleteDialog(account: Account) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove Account?")
            .setMessage(
                "Are you sure you want to remove the account ${account.name}? " +
                        "This will also remove all associated transactions and is irreversible."
            )
            .setPositiveButton("Delete") { dialog, _ ->
                accountViewModel.deleteAccount(account)
                dialog.dismiss()
                findNavController().navigateUp()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
}