package com.lacaprjc.expended.ui.accountWithTransactions

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lacaprjc.expended.data.AccountsWithTransactionsRepository
import com.lacaprjc.expended.ui.model.AccountWithTransactions

class AccountWithTransactionsViewModel @ViewModelInject constructor(
    private val repository: AccountsWithTransactionsRepository,
    @Assisted savedStateHandle: SavedStateHandle
) :
    ViewModel() {
    fun getAccountWithTransactions(accountId: Long): LiveData<AccountWithTransactions> =
        repository.getAccountWithTransactions(accountId)

    fun getAllAccountsWithTransactions(): LiveData<List<AccountWithTransactions>> =
        repository.getAllAccountsWithTransactions()

}