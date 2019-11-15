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
import com.byagowi.persiancalendar.databinding.SimpleDayPickerViewBinding
import com.byagowi.persiancalendar.entities.CalendarTypeItem
import com.byagowi.persiancalendar.utils.*
import com.google.android.material.snackbar.Snackbar

class SimpleDayPickerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), AdapterView.OnItemSelectedListener, DayPickerView,
    NumberPicker.OnValueChangeListener {

    private var jdn: Long = -1
    private var selectedDayListener: DayPickerView.OnSelectedDayChangedListener? = null

    val binding: SimpleDayPickerViewBinding =
        SimpleDayPickerViewBinding.inflate(LayoutInflater.from(context), this, true).apply {
            calendarTypeSpinner.adapter = ArrayAdapter(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                getOrderedCalendarEntities(getContext())
            )

            calendarTypeSpinner.setSelection(0)
            calendarTypeSpinner.onItemSelectedListener = this@SimpleDayPickerView

            yearPicker.setOnValueChangedListener(this@SimpleDayPickerView)
            monthPicker.setOnValueChangedListener(this@SimpleDayPickerView)
            dayPicker.setOnValueChangedListener(this@SimpleDayPickerView)
        }

    override val dayJdnFromView: Long
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

    override val selectedCalendarType: CalendarType
        get() = (binding.calendarTypeSpinner.selectedItem as CalendarTypeItem).type

    override fun setDayJdnOnView(jdn: Long) {
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

    override fun onValueChange(p0: NumberPicker?, p1: Int, p2: Int) {
        jdn = dayJdnFromView
        selectedDayListener?.onSelectedDayChanged(jdn)
    }

    override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
        setDayJdnOnView(jdn)
        selectedDayListener?.onSelectedDayChanged(jdn)
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {}

    override fun setOnSelectedDayChangedListener(listener: (Long) -> Unit) {
        selectedDayListener = object : DayPickerView.OnSelectedDayChangedListener {
            override fun onSelectedDayChanged(jdn: Long) = listener(jdn)
        }
    }
}
