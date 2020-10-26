package com.lacaprjc.expended.ui.transaction

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lacaprjc.expended.data.AccountsWithTransactionsRepository
import com.lacaprjc.expended.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class TransactionViewModel @ViewModelInject constructor(
    private val repository: AccountsWithTransactionsRepository,
    @Assisted savedStateHandle: SavedStateHandle
) :
    ViewModel() {
    enum class State {
        IDLE,
        EDIT_TRANSACTION,
        NEW_TRANSACTION,
        UPDATED_TRANSACTION,
        ADDED_TRANSACTION,
        DELETED_TRANSACTION
    }

    private val state = MutableStateFlow(State.IDLE)
    private val forAccountId: MutableLiveData<Long> = MutableLiveData(0L)
    private val transaction: MutableLiveData<Transaction> =
        MutableLiveData(Transaction("", 0L, 0.0, LocalDateTime.now(), ""))

    fun getState(): StateFlow<State> = state

    fun getWorkingTransaction(): LiveData<Transaction> = transaction

    private fun setWorkingTransaction(workingTransaction: Transaction) {
        transaction.value = workingTransaction
    }

    fun getForAccountId(): LiveData<Long> = forAccountId

    fun setForAccountId(accountId: Long) {
        forAccountId.value = accountId
    }

    fun addTransaction(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.addTransaction(transaction)
        setState(State.ADDED_TRANSACTION)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTransaction(transaction)
        setState(State.UPDATED_TRANSACTION)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTransaction(transaction)
        setState(State.DELETED_TRANSACTION)
    }

    private fun setState(newState: State) {
        state.value = newState
    }

    fun startNewTransaction() {
        state.value = State.NEW_TRANSACTION
        setWorkingTransaction(
            Transaction(
                "",
                forAccountId.value!!,
                amount = 0.0,
                date = LocalDateTime.now()
            )
        )
    }

    fun startEditingTransaction(transaction: Transaction) {
        state.value = State.EDIT_TRANSACTION
        setWorkingTransaction(transaction)
    }

    fun setToIdleState() {
        setState(State.IDLE)
    }
}