package com.example.activityshare.modules.addActivitySharePost

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.activityshare.R
import java.util.*

class addPost : Fragment() {

    private lateinit var date: TextView
    private lateinit var time: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_post, container, false)

        // Get references to the TextViews
        date = view.findViewById(R.id.fragment_add_post_date)
        time = view.findViewById(R.id.fragment_add_post_time)

        // Set onClickListeners to open Date and Time pickers
        date.setOnClickListener {
            openDatePicker()
        }

        time.setOnClickListener {
            openTimePicker()
        }

        return view
    }

    // Function to open the DatePickerDialog with restriction to future dates
    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Set the date picker to allow only future dates
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                date.text = selectedDate
            },
            year, month, day
        )

        // Prevent selecting past dates
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    // Function to open the TimePickerDialog with restriction to future times
    private fun openTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create a TimePickerDialog with current time as the minimum time
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                time.text = selectedTime
            },
            hour, minute, true
        )

        // Prevent selecting past times
        timePickerDialog.updateTime(hour, minute)
        timePickerDialog.show()
    }
}
