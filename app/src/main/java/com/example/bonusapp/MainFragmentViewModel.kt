package com.example.bonusapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainFragmentViewModel : ViewModel() {
    private val repository = ReminderRepository.get()
    //Making this a mutable StateFlow because:
    //  1.) StateFlow retains data throughout config changes and retrieves the last updated data
    //  2.) Needs to be mutated within the viewModel
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    //This is the read-only version exposed to the UI, so it should not be mutable
    val reminders : StateFlow<List<Reminder>>
        get() = _reminders.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllReminders().collect {
                //set the reminders here on a background thread
                _reminders.value = it
            }
        }
    }

    suspend fun addReminder(reminder: Reminder) {
        repository.addReminder(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        repository.deleteReminder(reminder)
    }
}