package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.NumberPicker
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DayPickerViewBinding
import com.byagowi.persiancalendar.entities.CalendarTypeItem
import com.byagowi.persiancalendar.utils.*
import com.google.android.material.snackbar.Snackbar

class DayPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    private var jdn: Long = -1

    var selectedDayListener: ((jdn: Long) -> Unit) = fun(_) {}

    val binding: DayPickerViewBinding =
        DayPickerViewBinding.inflate(LayoutInflater.from(context), this, true).apply {
            calendarTypeSpinner.adapter = ArrayAdapter(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                getOrderedCalendarEntities(getContext())
            )

            calendarTypeSpinner.setSelection(0)
            calendarTypeSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        setDayJdnOnView(jdn)
                        selectedDayListener(jdn)
                    }
                }
            val onDaySelected = NumberPicker.OnValueChangeListener { _, _, _ ->
                jdn = dayJdnFromView
                selectedDayListener(jdn)
            }
            yearPicker.setOnValueChangedListener(onDaySelected)
            monthPicker.setOnValueChangedListener(onDaySelected)
            dayPicker.setOnValueChangedListener(onDaySelected)
        }

    val dayJdnFromView: Long
        get() = try {
            val year = binding.yearPicker.value
            val month = binding.monthPicker.value
            val day = binding.dayPicker.value
            val selectedCalendarType = selectedCalendarType
            if (day > getMonthLength(selectedCalendarType, year, month))
                throw Exception("Not a valid day")

            getDateOfCalendar(selectedCalendarType, year, month, day).toJdn()
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(rootView, R.string.date_exception, Snackbar.LENGTH_SHORT).show()
            Log.e("SelectDayDialog", "", e)
            -1
        }

    val selectedCalendarType: CalendarType
        get() = (binding.calendarTypeSpinner.selectedItem as CalendarTypeItem).type

    fun setDayJdnOnView(jdn: Long) {
        var jdn = jdn
        this.jdn = jdn

        if (jdn == -1L) jdn = getTodayJdn()

        val date = getDateFromJdnOfCalendar(selectedCalendarType, jdn)

        // years spinner init.
        val YEARS_RANGE = 100
        binding.yearPicker.apply {
            minValue = date.year - YEARS_RANGE
            maxValue = date.year + YEARS_RANGE
            value = date.year
            setFormatter { formatNumber(it) }
        }
        //

        // month spinner init
        binding.monthPicker.apply {
            minValue = 1
            maxValue = 12
            value = date.month
            val months = monthsNamesOfCalendar(date)
            setFormatter { months[it - 1] + " / " + formatNumber(it) }
        }
        //

        // days spinner init
        binding.dayPicker.apply {
            minValue = 1
            maxValue = 31
            value = date.dayOfMonth
            setFormatter { formatNumber(it) }
        }
        //
    }
}
