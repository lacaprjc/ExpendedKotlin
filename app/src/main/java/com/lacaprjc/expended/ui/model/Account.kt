package com.lacaprjc.expended.ui.model

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

    companion object {
        fun fromJson(jsonObject: JSONObject): Account {
            return Account(
                name = jsonObject.getString("name"),
                accountType = Account.AccountType.valueOf(
                    jsonObject.getJSONObject("accountType").getString("type")
                        .toUpperCase(Locale.getDefault())
                ),
                notes = jsonObject.optString("notes", "")
            )
        }
    }
}