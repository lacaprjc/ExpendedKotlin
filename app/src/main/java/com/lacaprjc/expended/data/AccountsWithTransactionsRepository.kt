package com.lacaprjc.expended.data

import com.lacaprjc.expended.ui.model.Account
import com.lacaprjc.expended.ui.model.Transaction

class AccountsWithTransactionsRepository constructor(private val dao: AccountDao) {
    fun getAllAccounts() = dao.getAllAccounts()

    fun getAccountWithTransactions(accountId: Long) = dao.getAccountWithTransactions(accountId)

    fun getAllAccountsWithTransactions() = dao.getAllAccountsWithTransactions()

    suspend fun getAllAccountsWithTransactionsSync() = dao.getAllAccountsWithTransactionsSync()

    suspend fun getAllTransactions() = dao.getAllTransactions()

    suspend fun addAccount(account: Account): Long = dao.addAccount(account)

    suspend fun updateAccount(account: Account) = dao.updateAccount(account)

    suspend fun deleteAccount(account: Account) = dao.deleteAccount(account)

    suspend fun addTransaction(transaction: Transaction) = dao.addTransaction(transaction)

    suspend fun updateTransaction(transaction: Transaction) = dao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: Transaction) = dao.deleteTransaction(transaction)
}