package com.lacaprjc.expended.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lacaprjc.expended.data.AccountsWithTransactionsRepository
import com.lacaprjc.expended.ui.model.Account
import com.lacaprjc.expended.ui.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

// TODO: 9/9/20 HANDLE UPDATED STATE
class AccountViewModel(private val repository: AccountsWithTransactionsRepository) : ViewModel() {
    enum class State {
        EDIT,
        ADD,
        UPDATED,
        ADDED,
        DELETED
    }

    private val accountType = MutableLiveData(Account.AccountType.CASH)
    private val state = MutableLiveData(State.ADD)
    private val account = MutableLiveData(Account("", Account.AccountType.CASH) to 0.0)

    fun getState(): LiveData<State> = state

    fun getWorkingAccount(): LiveData<Pair<Account, Double>> = account

    fun getAccountType(): LiveData<Account.AccountType> = accountType

    fun setAccountType(type: Account.AccountType) = viewModelScope.launch {
        accountType.value = type
    }

    fun setWorkingAccount(account: Account, balance: Double) = viewModelScope.launch {
        this@AccountViewModel.account.value = account to balance
        setAccountType(account.accountType)
    }

    fun addAccount(account: Account, startingBalance: Double) =
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.addAccount(account)
            val initialTransaction = Transaction(
                id,
                startingBalance,
                LocalDateTime.now(),
                "Starting Balance",
                "Initial Transaction",
                ""
            )
            repository.addTransaction(initialTransaction)
            setState(State.ADDED)
        }

    fun updateAccount(account: Account) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateAccount(account)
        setState(State.UPDATED)
    }

    fun deleteAccount(account: Account) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAccount(account)
        setState(State.DELETED)
    }

    fun setState(state: State) = viewModelScope.launch {
        this@AccountViewModel.state.value = state
    }
}