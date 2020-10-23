package com.lacaprjc.expended.data

import androidx.sqlite.db.SimpleSQLiteQuery
import com.lacaprjc.expended.model.Account
import com.lacaprjc.expended.model.AccountWithTransactions
import com.lacaprjc.expended.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking

class AccountsWithTransactionsRepository constructor(private val dao: AccountsWithTransactionsDao) {
    fun getAllAccounts() = dao.getAllAccounts()

    fun getAccountWithTransactions(accountId: Long) = dao.getAccountWithTransactions(accountId)

    fun getAllAccountsWithTransactions(): Flow<List<AccountWithTransactions>> =
        dao.getAllAccountsWithTransactions()

    suspend fun getAllAccountsWithTransactionsSync() = dao.getAllAccountsWithTransactionsSync()

    suspend fun getAllTransactions() = dao.getAllTransactions()

    suspend fun addAccount(account: Account): Long = dao.addAccount(account)

    suspend fun updateAccount(account: Account) = dao.updateAccount(account)

    suspend fun deleteAccount(account: Account) = dao.deleteAccount(account)

    suspend fun addTransaction(transaction: Transaction) = dao.addTransaction(transaction)

    suspend fun updateTransaction(transaction: Transaction) = dao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: Transaction) = dao.deleteTransaction(transaction)

    suspend fun deleteAll() = dao.deleteAllAccountsAndTransactions()

    fun checkpoint() = runBlocking {
        dao.checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))
    }
}
