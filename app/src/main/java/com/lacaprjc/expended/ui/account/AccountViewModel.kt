package com.lacaprjc.expended.ui.account

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lacaprjc.expended.data.AccountsWithTransactionsRepository
import com.lacaprjc.expended.model.Account
import com.lacaprjc.expended.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class AccountViewModel @ViewModelInject constructor(
    private val repository: AccountsWithTransactionsRepository,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {
    enum class State {
        IDLE,
        EDIT_ACCOUNT,
        NEW_ACCOUNT,
        UPDATED_ACCOUNT,
        ADDED_ACCOUNT,
        DELETE_ACCOUNT
    }

    private val state: MutableStateFlow<State> = MutableStateFlow(State.IDLE)
    private val accountType = MutableLiveData(Account.AccountType.CASH)
    private val account: MutableLiveData<Pair<Account, Double>> = MutableLiveData(Account("", Account.AccountType.CASH, orderPosition = 0) to 0.0)

    fun getState(): StateFlow<State> = state

    fun getWorkingAccount(): LiveData<Pair<Account, Double>> = account

    fun getWorkingAccountOrderPosition(): Int = account.value!!.first.orderPosition

    fun getAccountType(): LiveData<Account.AccountType> = accountType

    fun setAccountType(type: Account.AccountType) = viewModelScope.launch {
        accountType.value = type
    }

    private fun setWorkingAccount(account: Account, balance: Double) = viewModelScope.launch {
        this@AccountViewModel.account.value = account to balance
        setAccountType(account.accountType)
    }

    fun addAccount(account: Account, startingBalance: Double) =
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.addAccount(account)
            val initialTransaction = Transaction(
                "Starting Balance",
                id,
                startingBalance,
                LocalDateTime.now(),
                "Initial Transaction",
                ""
            )
            repository.addTransaction(initialTransaction)
            setState(State.ADDED_ACCOUNT)
        }

    fun updateAccount(account: Account) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateAccount(account)
        setState(State.UPDATED_ACCOUNT)
    }

    fun deleteAccount(account: Account) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAccount(account)
        setState(State.DELETE_ACCOUNT)
    }

    fun startNewAccount(orderPosition: Int) {
        state.value = State.NEW_ACCOUNT
        setWorkingAccount(Account("", Account.AccountType.CASH, orderPosition = orderPosition), 0.0)
    }

    fun startEditingAccount(account: Account, balance: Double) {
        state.value = State.EDIT_ACCOUNT
        setWorkingAccount(account, balance)
    }

    fun setToIdleState() {
        setState(State.IDLE)
    }

    private fun setState(newState: State) {
        state.value = newState
    }
}