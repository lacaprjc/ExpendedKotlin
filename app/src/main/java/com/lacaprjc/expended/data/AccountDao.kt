package com.lacaprjc.expended.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.lacaprjc.expended.ui.model.Account
import com.lacaprjc.expended.ui.model.AccountWithTransactions
import com.lacaprjc.expended.ui.model.Transaction
import androidx.room.Transaction as RoomTransaction


@Dao
interface AccountDao {
    @Query("SELECT * FROM Account")
    fun getAllAccounts(): LiveData<List<Account>>

    @Query("SELECT * FROM `Transaction`")
    suspend fun getAllTransactions(): List<Transaction>

    @RoomTransaction
    @Query("SELECT * FROM Account")
    fun getAllAccountsWithTransactions(): LiveData<List<AccountWithTransactions>>

    @RoomTransaction
    @Query("SELECT * FROM Account")
    fun getAllAccountsWithTransactionsSync(): List<AccountWithTransactions>

    @RoomTransaction
    @Query("SELECT * FROM Account WHERE accountId = :id")
    fun getAccountWithTransactions(id: Long): LiveData<AccountWithTransactions>

//    @Query("SELECT * FROM `Transaction` WHERE forAccountId = :accountID")
//    suspend fun getAllTransactionsForAccount(accountID: Long): LiveData<List<Transaction>>

    @Insert
    suspend fun addAccount(account: Account): Long

    @Delete
    suspend fun deleteAccount(account: Account)

    @Update
    suspend fun updateAccount(account: Account)

    @Insert
    suspend fun addTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @RawQuery
    suspend fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int

    @Query("DELETE FROM Account")
    suspend fun deleteAllAccounts()

    @Query("DELETE FROM `Transaction`")
    suspend fun deleteAllTransactions()

    suspend fun deleteAllAccountsAndTransactions() {
        deleteAllAccounts()
        deleteAllTransactions()
    }
}