package com.lacaprjc.expended.data

import androidx.room.TypeConverter
import com.lacaprjc.expended.ui.model.Account

class AccountTypeConverter {
    @TypeConverter
    fun restoreAccountType(type: String) = Account.AccountType.valueOf(type)

    @TypeConverter
    fun accountTypeToString(type: Account.AccountType) = type.name
}