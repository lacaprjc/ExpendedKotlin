package com.lacaprjc.expended.ui.accountWithTransactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lacaprjc.expended.data.AccountsWithTransactionsRepository
import com.lacaprjc.expended.ui.model.AccountWithTransactions
import com.lacaprjc.expended.ui.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountWithTransactionsViewModel(private val repository: AccountsWithTransactionsRepository) :
    ViewModel() {
    fun getAccountWithTransactions(accountId: Long): LiveData<AccountWithTransactions> =
        repository.getAccountWithTransactions(accountId)

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTransaction(transaction)
    }
}