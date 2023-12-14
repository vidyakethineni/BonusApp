package com.example.bonusapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bonusapp.Reminder

@Database(entities = [Reminder::class], version=1)
@TypeConverters(ReminderTypeConverters::class)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
}