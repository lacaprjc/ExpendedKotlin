package com.lacaprjc.expended.ui.transaction

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lacaprjc.expended.data.AccountsWithTransactionsRepository
import com.lacaprjc.expended.ui.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class TransactionViewModel @ViewModelInject constructor(
    private val repository: AccountsWithTransactionsRepository,
    @Assisted savedStateHandle: SavedStateHandle
) :
    ViewModel() {
    enum class State {
        EDIT,
        ADD,
        UPDATED,
        ADDED,
        DELETED
    }

    private val state: MutableLiveData<State> = MutableLiveData(State.ADD)
    private val forAccountId: MutableLiveData<Long> = MutableLiveData(0L)
    private val transaction: MutableLiveData<Transaction> =
        MutableLiveData(Transaction("", 0L, 0.0, LocalDateTime.now(), ""))

    fun getState(): LiveData<State> = state

    fun getWorkingTransaction(): LiveData<Transaction> = transaction

    fun setWorkingTransaction(transaction: Transaction) = viewModelScope.launch {
        this@TransactionViewModel.transaction.value = transaction
    }

    fun getForAccountId(): LiveData<Long> = forAccountId

    fun setForAccountId(accountId: Long) = viewModelScope.launch {
        this@TransactionViewModel.forAccountId.value = accountId
    }

    fun addTransaction(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.addTransaction(transaction)
        setState(State.ADDED)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTransaction(transaction)
        setState(State.UPDATED)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTransaction(transaction)
        setState(State.DELETED)
    }

    fun setState(state: State) = viewModelScope.launch {
        this@TransactionViewModel.state.value = state
    }
}