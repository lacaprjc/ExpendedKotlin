package com.lacaprjc.expended.ui.model

import androidx.room.Embedded
import androidx.room.Relation

data class AccountWithTransactions(
    @Embedded
    val account: Account,
    @Relation(
        parentColumn = "accountId",
        entityColumn = "forAccountId"
    )
    val transactions: List<Transaction>
)