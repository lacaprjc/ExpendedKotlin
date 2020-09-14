package com.lacaprjc.expended.util

import com.lacaprjc.expended.ui.model.AccountWithTransactions
import org.json.JSONArray
import org.json.JSONObject

fun AccountWithTransactions.toJson(): JSONObject {
    val transactionsAsJsonArray = JSONArray()
    val transactionsAsJson = this.transactions.map {
        it.toJson()
    }

    transactionsAsJson.forEach { transaction ->
        transactionsAsJsonArray.put(transaction)
    }

    return JSONObject()
        .put("account", this.account.toJson())
        .put("transactions", transactionsAsJsonArray)
}