package com.example.bonusapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

//I want to pass an id to this viewModel so that it can retrieve a Reminder from the database
//using just the id.
class ReminderViewModel(id: UUID) : ViewModel() {
    private val repository = ReminderRepository.get()
    private val _reminder: MutableStateFlow<Reminder?> = MutableStateFlow(null)
    val reminder: StateFlow<Reminder?>
        get() = _reminder.asStateFlow()

    init {
        viewModelScope.launch {
            _reminder.value = repository.getReminder(id)
        }
    }

    //To update the reminder, just use the lambda and copy function when calling this function
    fun updateReminder(onUpdate: (oldReminder: Reminder) -> Reminder) {
        _reminder.update {
            it?.let(onUpdate)
        }
    }

    suspend fun deleteReminder() {
        _reminder.value?.let {
            repository.deleteReminder(it)
        }
    }

    //Here I can save the reminder once the user clicks on the back button, even though it
    //clears the viewModel
    override fun onCleared() {
        super.onCleared()
        _reminder.value?.let {
            //it doesn't disappear because it's done on the global scope
            repository.updateReminder(it)
        }
    }
}

//since this ViewModel takes a parameter not supported by the ViewModelProvider.Factory, I have
//to override it so that the Factory knows how to create this specific ViewModel
class ReminderViewModelFactory(private val id: UUID) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReminderViewModel(id) as T
    }
}