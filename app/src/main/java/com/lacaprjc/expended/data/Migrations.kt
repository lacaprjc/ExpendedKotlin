package com.lacaprjc.expended.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val migrateFrom1To2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE Account ADD COLUMN orderPosition INTEGER NOT NULL DEFAULT -1")
        }
    }

    val migrateFrom2To3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE Orders (id INTEGER NOT NULL, pos INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL DEFAULT 0)")
            database.execSQL("INSERT INTO Orders (id) SELECT accountId FROM Account")
            database.execSQL("CREATE TABLE NewAccount (name TEXT NOT NULL, accountType TEXT NOT NULL, notes TEXT NOT NULL, accountId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, orderPosition INTEGER NOT NULL)")
            database.execSQL("INSERT INTO NewAccount (name, accountType, notes, accountId, orderPosition) SELECT name, accountType, notes, accountId, (SELECT POS FROM Orders WHERE id=accountId) FROM Account")
            database.execSQL("DROP TABLE Orders")
            // for backup
            database.execSQL("ALTER TABLE Account RENAME TO AccountBackup")
            database.execSQL("ALTER TABLE NewAccount RENAME TO Account")
        }
    }
}