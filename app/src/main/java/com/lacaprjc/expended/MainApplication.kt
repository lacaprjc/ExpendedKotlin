package com.lacaprjc.expended

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lacaprjc.expended.data.AppDatabase

class MainApplication : Application() {
    private val migrations = arrayOf(
        object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Account ADD COLUMN 'notes' TEXT")
            }
        }
    )

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "accounts"
        ).addMigrations(*migrations)
            .fallbackToDestructiveMigration()
            .build()
    }

    fun getDatabase() = db
}