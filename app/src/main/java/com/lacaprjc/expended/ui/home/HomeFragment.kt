package com.lacaprjc.expended.ui.home

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.lacaprjc.expended.R
import com.lacaprjc.expended.databinding.FragmentHomeBinding
import com.lacaprjc.expended.listAdapters.AccountAdapter
import com.lacaprjc.expended.ui.account.AccountViewModel
import com.lacaprjc.expended.ui.accountWithTransactions.AccountWithTransactionsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<MaterialCardView>
    private lateinit var binding: FragmentHomeBinding

    private val homeViewModel
            by navGraphViewModels<AccountWithTransactionsViewModel>(R.id.mobile_navigation) { defaultViewModelProviderFactory }
    private val accountViewModel
            by navGraphViewModels<AccountViewModel>(R.id.mobile_navigation) { defaultViewModelProviderFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.colorPrimary)

        binding = FragmentHomeBinding.bind(view)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.accountFragmentBottomSheetCard)

        binding.settingsButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionNavigationHomeToNavigationSettings())
        }

        val accountAdapter = AccountAdapter(
            onClickListener = {
                findNavController().navigate(
                    HomeFragmentDirections.actionNavigationHomeToAccountDetailsFragment(
                        it.accountId
                    )
                )
            },
            onLongClickListener = { account, balance ->
                accountViewModel.setWorkingAccount(account, balance)
                accountViewModel.setState(AccountViewModel.State.EDIT)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        )
        val recyclerView = binding.accountsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = accountAdapter
        recyclerView.setHasFixedSize(true)

        lifecycleScope.launchWhenResumed {
            binding.accountFragment.findViewById<TextInputEditText>(R.id.accountNameInputEditText)
                .setOnClickListener {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            when (bottomSheetBehavior.state) {
                BottomSheetBehavior.STATE_COLLAPSED,
                BottomSheetBehavior.STATE_HIDDEN,
                BottomSheetBehavior.STATE_SETTLING -> isEnabled = true
                else -> {
                    isEnabled = false
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {
        homeViewModel.getAllAccountsWithTransactions()
            .observe(viewLifecycleOwner) { allAccountsWithTransactions ->
                val accounts = allAccountsWithTransactions.map {
                    it.account
                }

                val balances = allAccountsWithTransactions.map { accountWithTransactions ->
                    accountWithTransactions.transactions.sumOf { transaction ->
                        transaction.amount
                    }
                }

                (binding.accountsRecyclerView.adapter as AccountAdapter).submitAccounts(
                    accounts,
                    balances
                )

                binding.noAccountsText.visibility =
                    if (allAccountsWithTransactions.isNotEmpty()) View.INVISIBLE else View.VISIBLE
            }

        accountViewModel.getState().observe(viewLifecycleOwner) { state ->
            when (state) {
                AccountViewModel.State.DELETED,
                AccountViewModel.State.ADDED,
                AccountViewModel.State.UPDATED -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                else -> {
                }
            }
        }
    }
}