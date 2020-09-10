package com.lacaprjc.expended.ui.home

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputEditText
import com.lacaprjc.expended.MainApplication
import com.lacaprjc.expended.R
import com.lacaprjc.expended.data.AccountsWithTransactionsRepository
import com.lacaprjc.expended.databinding.FragmentHomeBinding
import com.lacaprjc.expended.listAdapters.AccountAdapter
import com.lacaprjc.expended.ui.account.AccountViewModel

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val homeViewModel by navGraphViewModels<HomeViewModel>(R.id.mobile_navigation) {
        // TODO: 9/9/20 use HILT to inject into view models
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val dao =
                    (requireActivity().application as MainApplication).getDatabase().accountDao()
                return HomeViewModel(AccountsWithTransactionsRepository(dao)) as T
            }
        }
    }

    private val accountViewModel by navGraphViewModels<AccountViewModel>(R.id.mobile_navigation) {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val dao =
                    (requireActivity().application as MainApplication).getDatabase().accountDao()
                return AccountViewModel(AccountsWithTransactionsRepository(dao)) as T
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentHomeBinding.bind(view)
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.accountFragmentBottomSheetCard)

        val accountAdapter = AccountAdapter(
            onClickListener = {
                findNavController().navigate(
                    HomeFragmentDirections.actionNavigationHomeToAccountDetailsFragment(
                        it.accountId
                    )
                )
            },
            onLongClickListener = {
                accountViewModel.setWorkingAccount(it)
                accountViewModel.setState(AccountViewModel.State.EDIT)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        )
        val recyclerView = binding.accountsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = accountAdapter
        recyclerView.setHasFixedSize(true)

        homeViewModel.getAccounts().observe(viewLifecycleOwner) {
            accountAdapter.submitAccounts(it)

            binding.noAccountsText.visibility =
                if (it.isNotEmpty()) View.INVISIBLE else View.VISIBLE
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
    }
}