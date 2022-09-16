package com.example.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_TIME = "time"

class TimePickerFragment: DialogFragment() {

    private var selectedDateCalendar: Calendar = Calendar.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val listener = TimePickerDialog.OnTimeSetListener {_, hour, minute ->

            val selectedDateAndTime: Date = GregorianCalendar(
                selectedDateCalendar.get(Calendar.YEAR),
                selectedDateCalendar.get(Calendar.MONTH),
                selectedDateCalendar.get(Calendar.DAY_OF_MONTH),
                hour,
                minute).time
            parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(TIME_SELECTED_KEY to selectedDateAndTime))
        }

        selectedDateCalendar.time = arguments?.getSerializable(ARG_TIME) as Date
        val initialHour: Int = selectedDateCalendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute: Int = selectedDateCalendar.get(Calendar.MINUTE)

        return TimePickerDialog(
            requireContext(),
            listener,
            initialHour,
            initialMinute,
            true
        )
    }

    companion object{
        const val REQUEST_KEY = "time_picker_fragment"
        const val TIME_SELECTED_KEY = "time_selected"

        fun getInstance(date: Date): TimePickerFragment{
            val args: Bundle = Bundle().apply {
                putSerializable(ARG_TIME, date)
            }
            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }
}