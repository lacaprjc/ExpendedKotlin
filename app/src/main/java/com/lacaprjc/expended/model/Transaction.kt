package com.lacaprjc.expended.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Entity
data class Transaction(
    val name: String,
    val forAccountId: Long = 0,
    val amount: Double,
    val date: LocalDateTime,
    val notes: String = "",
    val media: String = "",
    @PrimaryKey(autoGenerate = true)
    val transactionId: Long = 0
) {
    companion object {
        private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:m:s")

        fun fromJson(jsonObject: JSONObject, copyTransactionId: Boolean = true): Transaction {
            return Transaction(
                name = jsonObject.getString("name"),
                amount = jsonObject.getDouble("amount"),
                notes = jsonObject.optString("notes", ""),
                media = jsonObject.optString("media", ""),
                forAccountId = jsonObject.getLong("forAccountId"),
                date = LocalDateTime.parse(jsonObject.getString("date")),
                transactionId = if (copyTransactionId) jsonObject.getLong("transactionId") else 0L
            )
        }

        fun fromJsonSembast(jsonObject: JSONObject, accountId: Long = 0): Transaction {
            val dateString = jsonObject.getString("date")
            val timeString = jsonObject.getString("time")

            return Transaction(
                name = jsonObject.getString("name"),
                amount = jsonObject.getDouble("amount"),
                notes = jsonObject.optString("notes", ""),
                media = jsonObject.getJSONArray("image").optString(0, ""),
                forAccountId = accountId,
                date = LocalDateTime.of(
                    LocalDate.parse(dateString),
                    LocalTime.parse(timeString, timeFormatter)
                )
            )
        }

//        fun fromCsv(csvRow: String): Transaction {
//            return Transaction(
//                name = ,
//                forAccountId = ,
//                amount = ,
//                date = ,
//                notes = ,
//                media = ,
//                transactionId =
//            )
//        }
    }
}