package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import android.widget.NumberPicker
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DayPickerViewBinding
import com.byagowi.persiancalendar.utils.*
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

class DayPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        FrameLayout(context, attrs) {

    private var jdn: Long = -1

    var selectedDayListener = fun(_: Long) {}

    var selectedCalendarType: CalendarType = CalendarType.SHAMSI

    private val inflater = context.layoutInflater
    val binding: DayPickerViewBinding = DayPickerViewBinding.inflate(inflater, this, true).apply {
        val calendarTypes = getOrderedCalendarEntities(getContext())
        val chips = calendarTypes.map { calendarTypeItem ->
            (inflater.inflate(
                    R.layout.single_chip_layout, calendarTypesBox, false
            ) as Chip).apply { text = calendarTypeItem.toString() }
        }
        chips.forEachIndexed { i, chip ->
            chip.setOnClickListener {
                selectedCalendarType = calendarTypes[i].type
                setDayJdnOnView(jdn)
                selectedDayListener(jdn)
                chips.forEachIndexed { j, chipView ->
                    chipView.isClickable = i != j
                    chipView.isSelected = i == j
                }
            }
            chip.isClickable = i != 0
            chip.isSelected = i == 0
            chip.isCheckable = false
            selectedCalendarType = calendarTypes[0].type
            calendarTypesBox.addView(chip)
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
            if (day > getMonthLength(selectedCalendarType, year, month))
                throw Exception("Not a valid day")

            getDateOfCalendar(selectedCalendarType, year, month, day).toJdn()
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(rootView, R.string.date_exception, Snackbar.LENGTH_SHORT).show()
            Log.e("SelectDayDialog", "", e)
            -1
        }

    fun setDayJdnOnView(jdn: Long) {
        this.jdn = if (jdn == -1L) getTodayJdn() else jdn
        val date = getDateFromJdnOfCalendar(selectedCalendarType, this.jdn)
        binding.yearPicker.apply {
            minValue = date.year - 100
            maxValue = date.year + 100
            value = date.year
            setFormatter { formatNumber(it) }
        }
        binding.monthPicker.apply {
            minValue = 1
            maxValue = 12
            value = date.month
            val months = monthsNamesOfCalendar(date)
            setFormatter { months[it - 1] + " / " + formatNumber(it) }
        }
        binding.dayPicker.apply {
            minValue = 1
            maxValue = 31
            value = date.dayOfMonth
            setFormatter { formatNumber(it) }
        }
        selectedDayListener(jdn)
    }
}
