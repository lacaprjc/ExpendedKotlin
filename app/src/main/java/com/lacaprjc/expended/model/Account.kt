package com.lacaprjc.expended.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject
import java.util.*

@Entity
data class Account(
    val name: String,
    val accountType: AccountType,
    val notes: String = "",
    @PrimaryKey(autoGenerate = true)
    val accountId: Long = 0,
    val orderPosition: Int
) {
    enum class AccountType {
        CASH,
        CREDIT,
        CHECKING,
        SAVINGS,
        PERSONAL,
        BUDGET
    }

    companion object {
        fun fromJson(jsonObject: JSONObject, copyAccountId: Boolean = true): Account {
            return Account(
                name = jsonObject.getString("name"),
                accountType = AccountType.valueOf(
                    jsonObject.getString("accountType"),
                ),
                notes = jsonObject.getString("notes"),
                accountId = if (copyAccountId) jsonObject.getLong("accountId") else 0L,
                orderPosition = jsonObject.optInt("orderPosition", -1)
            )
        }

        fun fromJsonSembast(jsonObject: JSONObject): Pair<Account, Double> {
            return Account(
                name = jsonObject.getString("name"),
                accountType = AccountType.valueOf(
                    jsonObject.getJSONObject("accountType").getString("type")
                        .toUpperCase(Locale.getDefault())
                ),
                notes = jsonObject.optString("notes", ""),
                orderPosition = jsonObject.optInt("orderPosition", -1)
            ) to jsonObject.getDouble("balance")
        }
    }
}