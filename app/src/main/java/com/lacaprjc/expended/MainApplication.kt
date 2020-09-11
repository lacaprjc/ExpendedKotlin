package com.lacaprjc.expended

import android.app.Application
import androidx.room.Room
import com.lacaprjc.expended.data.AppDatabase

class MainApplication : Application() {
//    private val migrations = arrayOf(
//        object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE Account ADD COLUMN 'notes' TEXT")
//            }
//        }
//    )

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "accounts"
        )
            .build()
    }

    fun getDatabase() = db
}