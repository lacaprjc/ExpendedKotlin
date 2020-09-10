package com.lacaprjc.expended.ui.accountWithTransactions

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.lacaprjc.expended.MainApplication
import com.lacaprjc.expended.R
import com.lacaprjc.expended.data.AccountsWithTransactionsRepository
import com.lacaprjc.expended.databinding.FragmentAccountWithTransactionsBinding
import com.lacaprjc.expended.listAdapters.TransactionAdapter
import com.lacaprjc.expended.ui.transaction.TransactionViewModel
import com.lacaprjc.expended.util.getAssociatedColor
import com.lacaprjc.expended.util.getAssociatedIcon
import kotlinx.android.synthetic.main.fragment_account_with_transactions.view.*

class AccountWithTransactionsFragment : Fragment(R.layout.fragment_account_with_transactions) {

    private val args by navArgs<AccountWithTransactionsFragmentArgs>()
    private val accountWithTransactionsViewModel by navGraphViewModels<AccountWithTransactionsViewModel>(
        R.id.mobile_navigation
    ) {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val dao =
                    (requireActivity().application as MainApplication).getDatabase().accountDao()
                return AccountWithTransactionsViewModel(AccountsWithTransactionsRepository(dao)) as T
            }
        }
    }

    private val transactionViewModel by navGraphViewModels<TransactionViewModel>(R.id.mobile_navigation) {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val dao =
                    (requireActivity().application as MainApplication).getDatabase().accountDao()
                return TransactionViewModel(AccountsWithTransactionsRepository(dao)) as T
            }
        }
    }

    private lateinit var binding: FragmentAccountWithTransactionsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentAccountWithTransactionsBinding.bind(view)

        val bottomSheetBehavior: BottomSheetBehavior<MaterialCardView> =
            BottomSheetBehavior.from(binding.transactionFragmentBottomSheetCard)

        val transactionAdapter = TransactionAdapter()
        val recyclerView = binding.transactionsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = transactionAdapter
        recyclerView.setHasFixedSize(true)

        transactionViewModel.setForAccountId(args.accountId)

        accountWithTransactionsViewModel.getAccountWithTransactions(args.accountId)
            .observe(viewLifecycleOwner) { accountWithTransactions ->
                val accountColor = ContextCompat.getColor(
                    requireContext(),
                    accountWithTransactions.account.accountType.getAssociatedColor()
                )

                requireActivity().window.statusBarColor = accountColor
                binding.accountName.setCompoundDrawablesWithIntrinsicBounds(
                    accountWithTransactions.account.accountType.getAssociatedIcon(),
                    0,
                    0,
                    0
                )
                binding.accountBalanceCard.setCardBackgroundColor(accountColor)
                binding.accountBalanceCard.accountName.text = accountWithTransactions.account.name
                binding.accountBalanceCard.accountBalance.text =
                    accountWithTransactions.account.balance.toString()
                transactionAdapter.submitTransactions(accountWithTransactions.transactions)
            }

        lifecycleScope.launchWhenResumed {
            binding.transactionFragment.findViewById<TextInputEditText>(R.id.transactionNameInputEditText)
                .setOnClickListener {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
        }

//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//            when (bottomSheetBehavior.state) {
//                BottomSheetBehavior.STATE_COLLAPSED,
//                BottomSheetBehavior.STATE_HIDDEN,
//                BottomSheetBehavior.STATE_SETTLING -> isEnabled = true
//                else -> {
//                    isEnabled = false
//                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
//                }
//            }
//        }
    }
}