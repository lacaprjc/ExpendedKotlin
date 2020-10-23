package com.lacaprjc.expended.data

import androidx.room.TypeConverter
import java.time.LocalDateTime

class TransactionTypeConverter {

    @TypeConverter
    fun restoreDateTime(dateTime: String): LocalDateTime = LocalDateTime.parse(dateTime)

    @TypeConverter
    fun dateTimeToString(dateTime: LocalDateTime) = dateTime.toString()
}