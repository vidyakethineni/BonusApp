package com.example.bonusapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.bonusapp.databinding.ListItemReminderBinding
import java.util.UUID

class ReminderViewHolder(private val binding: ListItemReminderBinding): ViewHolder(binding.root) {
    //I want to pass an onClickListener here so that the fragment can decide what to do with it.
    fun bind(reminder: Reminder,
             onReminderDeleted: (reminder: Reminder) -> Unit,
             onReminderClicked: (reminderID: UUID) -> Unit) {
        binding.apply {
            reminderTitle.text = reminder.title
            reminderDescription.text = reminder.description
            reminderDateAndTime.text = reminder.dateAndTimeToString()
            root.setOnClickListener {
                onReminderClicked(reminder.id)
            }
            deleteIcon.setOnClickListener {
                onReminderDeleted(reminder)
            }
        }
    }
}

//This implements a ListAdapter instead of a RecyclerView adapter because ListAdapter efficiently
//updates the view as soon as an item is changed.
//This is especially useful for when I delete or add Reminders
class ReminderListAdapter(
    private val onReminderDeleted: (reminder: Reminder) -> Unit,
    private val onReminderClicked: (reminderID: UUID) -> Unit
) : ListAdapter<Reminder, ReminderViewHolder>(ReminderDiffUtil) {
    private lateinit var reminders : List<Reminder>
    override fun submitList(list: List<Reminder>?) {
        super.submitList(list)
        reminders = list ?: emptyList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemReminderBinding.inflate(layoutInflater, parent, false)
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.bind(reminder, onReminderDeleted, onReminderClicked)
    }
}

//This determines if an item has changed or not, and is used by the ListAdapter
object ReminderDiffUtil : DiffUtil.ItemCallback<Reminder>() {
    override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean =
        oldItem.id == newItem.id && oldItem == newItem

}