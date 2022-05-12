package com.byagowi.persiancalendar.ui.common

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.NumberPicker
import androidx.core.view.isVisible
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.databinding.DayPickerViewBinding
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.formatNumber

class DayPickerView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    var selectedDayListener = fun(_: Jdn) {}
    var selectedCalendarListener = fun(_: CalendarType) {}
    private var selectedCalendarType = CalendarType.SHAMSI
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
                it.minValue = today.year - 200 * if (BuildConfig.DEVELOPMENT) 10 else 1
                it.maxValue = today.year + 200 * if (BuildConfig.DEVELOPMENT) 10 else 1
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
                reinitializeDayPicker(it, date.year, date.month)
                it.value = date.dayOfMonth // important to happen _after_ the reinitialization
                it.isVerticalScrollBarEnabled = false
            }
            selectedDayListener(value)
        }

    private fun reinitializeDayPicker(dayPicker: NumberPicker, year: Int, month: Int) {
        dayPicker.maxValue = selectedCalendarType.getMonthLength(year, month)
        val monthStart = Jdn(selectedCalendarType, year, month, 1)
        binding.dayPicker.setFormatter {
            (monthStart + it - 1).dayOfWeekName + " / " + formatNumber(it)
        }
        binding.dayPicker.invalidate()
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
        }.also {
            selectedCalendarType = it[0].first
            selectedCalendarListener(selectedCalendarType)
        }
        binding.calendars.setup(calendarTypes) {
            selectedCalendarType = it
            selectedCalendarListener(selectedCalendarType)
            jdn = currentJdn
            selectedDayListener(currentJdn)
        }

        val onDaySelected = NumberPicker.OnValueChangeListener { _, _, _ ->
            val year = binding.yearPicker.value
            val month = binding.monthPicker.value
            reinitializeDayPicker(binding.dayPicker, year, month)
            binding.monthPicker.maxValue = selectedCalendarType.getYearMonths(year)

            currentJdn = jdn
            selectedDayListener(currentJdn)
        }
        binding.yearPicker.setOnValueChangedListener(onDaySelected)
        binding.monthPicker.setOnValueChangedListener(onDaySelected)
        binding.dayPicker.setOnValueChangedListener(onDaySelected)
    }

    fun turnToSecondaryDatePicker() {
        binding.calendars.isVisible = false
        binding.dayTitle.isVisible = false
        binding.monthTitle.isVisible = false
        binding.yearTitle.isVisible = false
    }

    // To use in init or when the picker is secondary, selectedCalendarListener isn't called
    fun changeCalendarType(calendarType: CalendarType) {
        selectedCalendarType = calendarType
        binding.calendars.changeSelection(calendarType)
        jdn = currentJdn
    }
}
