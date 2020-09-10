package com.lacaprjc.expended.ui.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Account(
    val name: String,
    val balance: Double,
    val accountType: AccountType,
    val notes: String = "",
    @PrimaryKey(autoGenerate = true)
    val accountId: Long = 0
) {
    enum class AccountType {
        CASH,
        CREDIT,
        CHECKING,
        SAVINGS,
        PERSONAL,
        BUDGET
    }
}