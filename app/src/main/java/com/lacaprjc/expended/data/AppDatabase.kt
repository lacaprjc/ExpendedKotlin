package com.lacaprjc.expended.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lacaprjc.expended.ui.model.Account
import com.lacaprjc.expended.ui.model.Transaction

@Database(entities = [Account::class, Transaction::class], version = 1)
@TypeConverters(AccountTypeConverter::class, TransactionTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}