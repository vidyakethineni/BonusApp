package com.example.bonusapp.database

import androidx.room.TypeConverter
import java.util.Date

//I need typeConverters to convert the date objects to something easily read by the database, like
//a Long
class ReminderTypeConverters {
    @TypeConverter
    fun fromDate(date: Date): Long = date.time

    @TypeConverter
    fun toDate(time: Long): Date = Date(time)
}