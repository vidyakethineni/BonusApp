package com.example.bonusapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bonusapp.Reminder
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ReminderDao {
    //This returns a Flow because I want it to work within a coroutine in my ViewModel
    //Getting data from the database is a blocking function, so the Flow object makes it easy
    //to do on a background thread
    @Query("SELECT * FROM reminder")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminder WHERE id=(:id)")
    suspend fun getReminder(id: UUID): Reminder

    @Update
    suspend fun updateReminder(reminder: Reminder)

    //Although unlikely, the onConflictStrategy replaces a Reminder if there is an existing copy already
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)
}