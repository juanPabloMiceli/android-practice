package com.example.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"
private const val DIALOG_TIME = "DialogTime"

class DatePickerFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date: Date = arguments?.getSerializable(ARG_DATE) as Date
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = date
        val initialYear: Int = calendar.get(Calendar.YEAR)
        val initialMonth: Int = calendar.get(Calendar.MONTH)
        val initialDay: Int = calendar.get(Calendar.DAY_OF_MONTH)
        
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val dateSelected: Date = GregorianCalendar(year, month, day).time
            TimePickerFragment.getInstance(dateSelected).apply {
                show(this@DatePickerFragment.parentFragmentManager, DIALOG_TIME)
            }
        }

        return DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDay)

    }

    companion object{

        fun getInstance(date: Date) : DatePickerFragment{
            val args: Bundle = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }
}