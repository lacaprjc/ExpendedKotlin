package com.lacaprjc.expended.ui.accountWithTransactions

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lacaprjc.expended.data.AccountsWithTransactionsRepository
import com.lacaprjc.expended.model.AccountWithTransactions
import kotlinx.coroutines.flow.Flow

class AccountWithTransactionsViewModel @ViewModelInject constructor(
    private val repository: AccountsWithTransactionsRepository,
    @Assisted savedStateHandle: SavedStateHandle
) :
    ViewModel() {
    fun getAccountWithTransactions(accountId: Long): Flow<AccountWithTransactions> =
        repository.getAccountWithTransactions(accountId)

    fun getAllAccountsWithTransactions(): Flow<List<AccountWithTransactions>> =
        repository.getAllAccountsWithTransactions()

}