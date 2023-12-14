package com.example.bonusapp

import android.icu.text.SimpleDateFormat
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.Locale
import java.util.UUID

@Entity
data class Reminder(
    //I set a UUID to this data class to make each item unique in a database
    @PrimaryKey val id: UUID,
    val title: String = "",
    val description: String = "",
    //I'm making the date and time one variable since both are stored in the same Date object anyway
    val dateAndTime: Date,
) {
    fun dateAndTimeToString(): String {
        val sdf = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault())
        return sdf.format(dateAndTime)
    }

    fun dateToString(): String {
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return sdf.format(dateAndTime)
    }

    fun timeToString(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(dateAndTime)
    }
}
