package com.lacaprjc.expended.util

import com.lacaprjc.expended.ui.model.Transaction
import org.json.JSONObject

fun Transaction.toJson(): JSONObject = JSONObject()
    .put("name", this.name)
    .put("forAccountId", this.forAccountId)
    .put("amount", this.amount)
    .put("date", this.date.toString())
    .put("notes", this.notes)
    .put("media", this.media)
    .put("transactionId", this.transactionId)