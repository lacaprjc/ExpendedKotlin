package com.lacaprjc.expended.ui.accountWithTransactions

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.lacaprjc.expended.R
import com.lacaprjc.expended.databinding.FragmentAccountWithTransactionsBinding
import com.lacaprjc.expended.listAdapters.TransactionAdapter
import com.lacaprjc.expended.ui.transaction.TransactionViewModel
import com.lacaprjc.expended.util.getAssociatedColor
import com.lacaprjc.expended.util.getAssociatedIcon
import com.lacaprjc.expended.util.toStringWithDecimalPlaces
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_account_with_transactions.view.*

@AndroidEntryPoint
class AccountWithTransactionsFragment : Fragment(R.layout.fragment_account_with_transactions) {
    private lateinit var binding: FragmentAccountWithTransactionsBinding

    private val transactionViewModel
            by navGraphViewModels<TransactionViewModel>(R.id.mobile_navigation) { defaultViewModelProviderFactory }
    private val accountWithTransactionsViewModel
            by navGraphViewModels<AccountWithTransactionsViewModel>(R.id.mobile_navigation) { defaultViewModelProviderFactory }
    private val args
            by navArgs<AccountWithTransactionsFragmentArgs>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentAccountWithTransactionsBinding.bind(view)

        val bottomSheetBehavior: BottomSheetBehavior<MaterialCardView> =
            BottomSheetBehavior.from(binding.transactionFragmentBottomSheetCard)

        val transactionAdapter = TransactionAdapter(
            onClickListener = {
                transactionViewModel.setWorkingTransaction(it)
                transactionViewModel.setState(TransactionViewModel.State.EDIT)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        )
        val recyclerView = binding.transactionsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = transactionAdapter
        recyclerView.setHasFixedSize(true)

        transactionViewModel.setForAccountId(args.accountId)

        transactionViewModel.getState().observe(viewLifecycleOwner) {
            when (it) {
                TransactionViewModel.State.UPDATED,
                TransactionViewModel.State.ADDED,
                TransactionViewModel.State.DELETED -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                else -> {
                }
            }
        }

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
                    accountWithTransactions.transactions.sumOf {
                        it.amount
                    }.toStringWithDecimalPlaces(2)

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