package com.example.bonusapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.bonusapp.databinding.FragmentReminderBinding
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class ReminderFragment : Fragment() {
    //retrieve the arguments using SafeArgs
    private val args: ReminderFragmentArgs by navArgs()
    private val viewModel: ReminderViewModel by viewModels {
        ReminderViewModelFactory(args.reminderId)
    }
    private var _binding: FragmentReminderBinding? = null
    private val binding: FragmentReminderBinding
        get() = checkNotNull(_binding) {
            "FragmentReminderBinding should not be null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            reminderTitle.doOnTextChanged { text, _, _, _ ->
                viewModel.updateReminder { oldReminder ->
                    oldReminder.copy(title = text.toString())
                }
            }
            reminderDescription.doOnTextChanged { text, _, _, _ ->
                viewModel.updateReminder { it.copy(description = text.toString()) }
            }
        }

        //Just like in the MainFragment, I want to launch and update the viewmodel data every time
        //it goes through a config change, to persist the data
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reminder.collect { reminder ->
                    reminder?.let{ updateUI(it) }
                }
            }
        }

        //Result listener for DatePicker
        setFragmentResultListener(DatePickerFragment.REQUEST_KEY_DATE) { _, bundle ->
            //the updated version of this deprecated class only works on API 34+, so
            //I left this here for now. It deprecated at API 33 so it should be fine for
            //most devices.
            val resultDate = bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date
            viewModel.updateReminder { it.copy(dateAndTime = resultDate) }
        }

        //Result listener for TimePicker
        setFragmentResultListener(TimePickerFragment.REQUEST_KEY_TIME) { _, bundle ->
            val resultTime = bundle.getSerializable(TimePickerFragment.BUNDLE_KEY_TIME) as Date
            viewModel.updateReminder { it.copy(dateAndTime = resultTime) }
        }

        //Set up the options menu
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_reminder_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.delete_reminder -> {
                        deleteReminder()
                        true
                    }
                    else ->
                        //Return true here to exit this function; otherwise the FragmentManager
                        //will try to execute onMenuItemSelected from the host Activity or other
                        //Fragments.
                        true
                }
            }

        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    private fun updateUI(reminder: Reminder) {
        binding.apply {
            if (reminder.title != reminderTitle.text.toString()) {
                reminderTitle.setText(reminder.title)
            }
            if (reminder.description != reminderDescription.text.toString()) {
                reminderDescription.setText(reminder.description)
            }
            pickDate.text = reminder.dateToString()
            pickDate.setOnClickListener {
                findNavController().navigate(
                    ReminderFragmentDirections.selectDate(reminder.dateAndTime)
                )
            }
            pickTime.text = reminder.timeToString()
            pickTime.setOnClickListener {
                findNavController().navigate(
                    ReminderFragmentDirections.selectTime(reminder.dateAndTime)
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun deleteReminder() {
        //show the reminder dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(getString(R.string.delete_reminder_question))
            .setMessage(getString(R.string.delete_reminder_warning))
            .setPositiveButton(
                "Yes"
            ) { _, _ ->
                //the reminder will be deleted.
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.deleteReminder()
                    findNavController().navigate(
                        ReminderFragmentDirections.deleteReminder()
                    )
                }
            }
            .setNegativeButton(
                "No") { _, _ ->
                //dismiss
            }
            .show()
    }
}