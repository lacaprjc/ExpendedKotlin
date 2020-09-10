package com.lacaprjc.expended.ui.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class Transaction(
    val forAccountId: Long = 0,
    val amount: Double,
    val date: LocalDateTime,
    val name: String,
    val notes: String = "",
    val media: String = "",
    @PrimaryKey(autoGenerate = true)
    val transactionId: Long = 0
)