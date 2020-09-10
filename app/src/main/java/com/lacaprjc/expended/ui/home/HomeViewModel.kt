package com.lacaprjc.expended.ui.home

import androidx.lifecycle.ViewModel
import com.lacaprjc.expended.data.AccountsWithTransactionsRepository

class HomeViewModel(private val accountsWithTransactionsRepository: AccountsWithTransactionsRepository) : ViewModel() {
    // TODO: 9/9/20 get this in sync with the AccountWithTransacationsViewModel
    fun getAccounts() = accountsWithTransactionsRepository.getAllAccounts()
}