package com.lacaprjc.expended.model

import androidx.room.Embedded
import androidx.room.Relation
import org.json.JSONObject

data class AccountWithTransactions(
    @Embedded
    val account: Account,
    @Relation(
        parentColumn = "accountId",
        entityColumn = "forAccountId"
    )
    val transactions: List<Transaction>
) {
    companion object {
        fun fromJson(jsonObject: JSONObject, copyIds: Boolean = true): AccountWithTransactions {
            val account = Account.fromJson(jsonObject.getJSONObject("account"), copyAccountId = copyIds)
            val transactionsAsJson = jsonObject.getJSONArray("transactions")

            val transactions = mutableListOf<Transaction>()
            for (i in 0 until transactionsAsJson.length()) {
                transactions.add(Transaction.fromJson(transactionsAsJson.getJSONObject(i), copyTransactionId = copyIds))
            }

            return AccountWithTransactions(account, transactions)
        }
    }
}