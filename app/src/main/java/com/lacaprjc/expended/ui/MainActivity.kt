package com.lacaprjc.expended.ui

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.lacaprjc.expended.R
import com.lacaprjc.expended.databinding.ActivityMainBinding
import com.lacaprjc.expended.ui.account.AccountDetailsFragment
import com.lacaprjc.expended.ui.account.AccountViewModel
import com.lacaprjc.expended.ui.account.AccountViewModel.State.ADDED_ACCOUNT
import com.lacaprjc.expended.ui.account.AccountViewModel.State.DELETED_ACCOUNT
import com.lacaprjc.expended.ui.account.AccountViewModel.State.IDLE
import com.lacaprjc.expended.ui.account.AccountViewModel.State.UPDATED_ACCOUNT
import com.lacaprjc.expended.ui.accountWithTransactions.AccountWithTransactionsFragmentArgs
import com.lacaprjc.expended.ui.transaction.TransactionDetailsFragment
import com.lacaprjc.expended.ui.transaction.TransactionViewModel
import com.lacaprjc.expended.ui.transaction.TransactionViewModel.State
import com.lacaprjc.expended.ui.transaction.TransactionViewModel.State.ADDED_TRANSACTION
import com.lacaprjc.expended.ui.transaction.TransactionViewModel.State.DELETED_TRANSACTION
import com.lacaprjc.expended.ui.transaction.TransactionViewModel.State.UPDATED_TRANSACTION
import com.lacaprjc.expended.util.getAssociatedColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FragmentContainerView>
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val accountViewModel: AccountViewModel by viewModels()
    private val transactionViewModel: TransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController.apply {
            addOnDestinationChangedListener { _, destination, args ->
                window.statusBarColor = ContextCompat.getColor(
                    this@MainActivity, when (destination.id) {
                        R.id.navigation_account_with_transactions -> AccountWithTransactionsFragmentArgs.fromBundle(
                            args!!
                        ).accountColorResId
                        else -> R.color.colorPrimary
                    }
                )
            }
        }

        val closeBottomSheetCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        onBackPressedDispatcher.addCallback(this@MainActivity, closeBottomSheetCallback)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetFragmentContainer).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            skipCollapsed = true
            addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    closeBottomSheetCallback.isEnabled = when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED, BottomSheetBehavior.STATE_HIDDEN -> false
                        else -> true
                    }

                    if (!closeBottomSheetCallback.isEnabled) {
                        accountViewModel.setToIdleState()
                        transactionViewModel.setToIdleState()
                        // close keyboard if it was open
                        getSystemService(InputMethodManager::class.java)
                            .hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) { /* no override */
                }
            })
        }

        observeData()
    }

    private fun observeData() {
        accountViewModel.getAccountType().observe(this) {
            when (navController.currentDestination!!.id) {
                R.id.navigation_account_with_transactions, R.id.navigation_account_details_fragment, R.id.navigation_transaction_details_fragment -> {
                    window.statusBarColor = ContextCompat.getColor(
                        this, it.getAssociatedColor()
                    )
                }
                else -> {
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            launch {
                accountViewModel.getState().collect { accountState ->
                    if (accountState != IDLE) {
                        supportFragmentManager.beginTransaction()
                            .replace(
                                binding.bottomSheetFragmentContainer.id,
                                AccountDetailsFragment()
                            )
                            .commit()
                    }

                    when (accountState) {
                        IDLE, UPDATED_ACCOUNT, ADDED_ACCOUNT, DELETED_ACCOUNT -> bottomSheetBehavior.state =
                            BottomSheetBehavior.STATE_HIDDEN
                        else -> {
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                }
            }

            launch {
                transactionViewModel.getState().collect { transactionState ->
                    if (transactionState != State.IDLE) {
                        supportFragmentManager.beginTransaction().replace(
                            binding.bottomSheetFragmentContainer.id,
                            TransactionDetailsFragment()
                        ).commit()
                    }

                    when (transactionState) {
                        State.IDLE, UPDATED_TRANSACTION, ADDED_TRANSACTION, DELETED_TRANSACTION -> bottomSheetBehavior.state =
                            BottomSheetBehavior.STATE_HIDDEN
                        else -> {
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                }
            }
        }
    }
}