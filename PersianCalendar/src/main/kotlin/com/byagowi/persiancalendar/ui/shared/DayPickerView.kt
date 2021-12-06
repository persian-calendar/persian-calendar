package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.NumberPicker
import com.byagowi.persiancalendar.databinding.DayPickerViewBinding
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.formatNumber

class DayPickerView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    var selectedDayListener = fun(_: Jdn) {}
    var selectedCalendarType = CalendarType.SHAMSI
        private set
    var jdn: Jdn
        get() {
            val year = binding.yearPicker.value
            val month = binding.monthPicker.value
            val day = binding.dayPicker.value
            return Jdn(selectedCalendarType, year, month, day)
        }
        set(value) {
            currentJdn = value
            val date = value.toCalendar(selectedCalendarType)
            binding.yearPicker.also {
                val today = todayJdn.toCalendar(selectedCalendarType)
                it.minValue = today.year - 200
                it.maxValue = today.year + 200
                it.value = date.year
                it.setFormatter(::formatNumber)
                it.isVerticalScrollBarEnabled = false
            }
            binding.monthPicker.also {
                it.minValue = 1
                it.maxValue = selectedCalendarType.getYearMonths(date.year)
                it.value = date.month
                val months = date.calendarType.monthsNames
                it.setFormatter { x -> months[x - 1] + " / " + formatNumber(x) }
                it.isVerticalScrollBarEnabled = false
            }
            binding.dayPicker.also {
                it.minValue = 1
                it.maxValue = selectedCalendarType.getMonthLength(date.year, date.month)
                it.value = date.dayOfMonth
                it.setFormatter(::formatNumber)
                it.isVerticalScrollBarEnabled = false
            }
            selectedDayListener(value)
        }

    private val todayJdn = Jdn.today()
    private var currentJdn = todayJdn
    private val binding = DayPickerViewBinding.inflate(
        context.layoutInflater, this, true
    ).also { binding ->
        val calendarTypes = enabledCalendars.map { calendarType ->
            calendarType to context.getString(
                if (language.betterToUseShortCalendarName) calendarType.shortTitle
                else calendarType.title
            )
        }.also { selectedCalendarType = it[0].first }
        binding.calendarsFlow.setup(calendarTypes) {
            selectedCalendarType = it
            jdn = currentJdn
            selectedDayListener(currentJdn)
        }

        val onDaySelected = NumberPicker.OnValueChangeListener { _, _, _ ->
            val year = binding.yearPicker.value
            val month = binding.monthPicker.value
            binding.dayPicker.maxValue = selectedCalendarType.getMonthLength(year, month)
            binding.monthPicker.maxValue = selectedCalendarType.getYearMonths(year)

            currentJdn = jdn
            selectedDayListener(currentJdn)
        }
        binding.yearPicker.setOnValueChangedListener(onDaySelected)
        binding.monthPicker.setOnValueChangedListener(onDaySelected)
        binding.dayPicker.setOnValueChangedListener(onDaySelected)
    }
}
