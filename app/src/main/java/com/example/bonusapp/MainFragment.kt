package com.example.bonusapp

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bonusapp.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID


class MainFragment : Fragment() {
    //making this nullable so that there's no strong reference to it when the Fragment view
    //gets destroyed
    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = checkNotNull(_binding) {
            "MainFragmentBinding should not be null. Is the view visible?"
        }
    private val viewModel: MainFragmentViewModel by viewModels()
    private lateinit var reminderListAdapter: ReminderListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        reminderListAdapter = ReminderListAdapter(::onReminderDeleted) { reminderId ->
            //Listener for when a reminder is clicked.
            //navigates to the Reminder Fragment
            //I imported the SafeArgs library so I can use FragmentDirections here
            findNavController().navigate(
                MainFragmentDirections.showReminder(reminderId)
            )
        }

        binding.reminderRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            adapter = reminderListAdapter
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            //I want to run this every time the Fragment is in the Started Lifecycle so that
            //it updates and persists across configuration changes.
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reminders.collect {
                    reminderListAdapter.submitList(it)
                }
            }
        }
        binding.addReminderFab.setOnClickListener {
            showNewReminder()
        }

        //Set up options menu
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_fragment_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.add_reminder -> {
                        showNewReminder()
                        true
                    }
                    R.id.feedback -> {
                        sendFeedback()
                        true
                    }
                    R.id.about -> {
                        aboutPage()
                        true
                    }
                    else -> true
                }
            }

        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    private fun showNewReminder() {
        viewLifecycleOwner.lifecycleScope.launch {
            val reminder = Reminder(id= UUID.randomUUID(), dateAndTime = Date())
            viewModel.addReminder(reminder)
            findNavController().navigate(
                MainFragmentDirections.showReminder(reminder.id)
            )
        }
    }

    private fun sendFeedback() {
        val feedbackIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            val emailAddress = arrayOf("vkethine@iu.edu")
            putExtra(Intent.EXTRA_EMAIL, emailAddress)
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback))
        }
        if (feedbackIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(feedbackIntent)
        } else {
            // If no email app is available, open Gmail website in a browser
            openGmailWebsite()
        }
    }

    private fun openGmailWebsite() {
        val gmailWebsiteIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://mail.google.com/") // Gmail website URL
        }

        if (gmailWebsiteIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(gmailWebsiteIntent)
        } else {
            showErrorToast()
        }
    }


    private fun aboutPage() {
        val aboutPageIntent = Intent(Intent.ACTION_VIEW).apply {
            //website address
            val websiteAddress = "www.luddy.indiana.edu/"
            setDataAndType(Uri.parse("http:$websiteAddress"), "text/html")
        }

        if (aboutPageIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(aboutPageIntent)
        } else showErrorToast()
    }

    private fun showErrorToast() {
        Toast.makeText(context,
            "Sorry, your device does not have the appropriate app to do that right now.",
            Toast.LENGTH_SHORT
        ).show()
    }

    //Listener for when a reminder is deleted.
    private fun onReminderDeleted(reminder: Reminder) {
        //show the reminder dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(getString(R.string.delete_reminder_question))
            .setMessage(getString(R.string.delete_reminder_warning))
            .setPositiveButton(
                "Yes"
            ) { _, _ ->
                //the reminder will be deleted.
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.deleteReminder(reminder)
                }
            }
            .setNegativeButton(
                "No") { _, _ ->
                //dismiss
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //clear the binding here
        _binding = null
    }
}