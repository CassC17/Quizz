package com.supdevinci.quizz.data

import androidx.room.TypeConverter
import java.util.Date

class Converter {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

}