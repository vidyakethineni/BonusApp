package com.example.bonusapp

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import java.util.Calendar
import java.util.GregorianCalendar

//Same as DatePickerFragment
//I made this into a Fragment so that it can exist through config changes, and gives a bit more
//functionality.
class TimePickerFragment : DialogFragment() {
    private val args: TimePickerFragmentArgs by navArgs()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        calendar.time = args.reminderTime

        //I'm keeping the year, month, and day as well to have a consistent Date object
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val listener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val resultTime =
                GregorianCalendar(initialYear, initialMonth, initialDay, hourOfDay, minute)
                    .time
            setFragmentResult(REQUEST_KEY_TIME, bundleOf(BUNDLE_KEY_TIME to resultTime))
        }
        return TimePickerDialog(
            requireContext(),
            listener,
            hour,
            minute,
            false
        )
    }

    companion object {
        const val REQUEST_KEY_TIME = "REQUEST_KEY_TIME"
        const val BUNDLE_KEY_TIME = "BUNDLE_KEY_TIME"
    }
}