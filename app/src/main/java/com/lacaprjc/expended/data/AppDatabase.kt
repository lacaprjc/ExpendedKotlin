package com.lacaprjc.expended.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lacaprjc.expended.model.Account
import com.lacaprjc.expended.model.Transaction

@Database(entities = [Account::class, Transaction::class], version = 3)
@TypeConverters(AccountTypeConverter::class, TransactionTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountsWithTransactionsDao
}