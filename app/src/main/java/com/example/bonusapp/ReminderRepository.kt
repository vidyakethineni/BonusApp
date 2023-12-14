package com.example.bonusapp

import android.content.Context
import androidx.room.Room
import com.example.bonusapp.database.ReminderDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

private const val DATABASE_NAME = "reminder-database"
//This needs to be a singleton with a private constructor so that there is only one Instance of it
//throughout the app.
class ReminderRepository private constructor(
    context: Context,
    //Making this a GlobalScope so that one specific coroutine can run even when a Viewmodel is
    //cleared.
    //I know this is risky, but this should stay alive for as long as the app is alive anyway, because
    //it's dedicated to only one task.
    private val coroutineScope: CoroutineScope = GlobalScope
){
    private val reminderDatabase = Room
        .databaseBuilder(context, ReminderDatabase::class.java, DATABASE_NAME)
        //no need to fallback to destructive migration because only 1 version exists
        .build()

    fun getAllReminders(): Flow<List<Reminder>> = reminderDatabase.reminderDao().getAllReminders()

    suspend fun getReminder(id: UUID) = reminderDatabase.reminderDao().getReminder(id)

    fun updateReminder(reminder: Reminder) {
        //This is how the Reminder will be saved.
        //Since I don't want this to stop executing as soon as a ViewModel is closed, I'm using
        //GlobalScope here.
        coroutineScope.launch {
            reminderDatabase.reminderDao().updateReminder(reminder)
        }
    }

    suspend fun addReminder(reminder: Reminder) =
        reminderDatabase.reminderDao().addReminder(reminder)

    suspend fun deleteReminder(reminder: Reminder) =
        reminderDatabase.reminderDao().deleteReminder(reminder)

    companion object {
        private var INSTANCE : ReminderRepository? = null

        //initialize this when the app starts.
        fun initialize(context: Context) {
            if (INSTANCE==null) {
                INSTANCE = ReminderRepository(context.applicationContext)
            }
        }

        //Just a friendly Reminder that the repository has to be initialized in the beginning
        //of the app.
        fun get(): ReminderRepository = INSTANCE
            ?: throw IllegalStateException("ReminderRepository must be initialized.")
    }
}