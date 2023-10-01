package com.byagowi.persiancalendar.ui.common

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.NumberPicker
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.databinding.DayPickerViewBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackTick
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.formatNumber

class DayPickerView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    var onValueChangeListener = fun(_: Jdn) {}
    var calendarType = mainCalendar
        set(value) {
            field = value
            this.value = currentValue
        }
    var value: Jdn
        get() {
            val year = binding.yearPicker.value
            val month = binding.monthPicker.value
            val day = binding.dayPicker.value
            return Jdn(calendarType, year, month, day)
        }
        set(value) {
            currentValue = value
            val date = value.toCalendar(calendarType)
            binding.yearPicker.also {
                val today = todayJdn.toCalendar(calendarType)
                it.minValue = if (BuildConfig.DEVELOPMENT) 1 else today.year - 200
                it.maxValue = today.year + 200
                it.value = date.year
                it.setFormatter(::formatNumber)
                it.isVerticalScrollBarEnabled = false
            }
            binding.monthPicker.also {
                it.minValue = 1
                val maxValue = calendarType.getYearMonths(date.year)
                val months = date.calendarType.monthsNames
                val displayedValues =
                    (1..maxValue).map { x -> months[x - 1] + " / " + formatNumber(x) }
                it.setMaxValueAndDisplayedValues(maxValue, displayedValues)
                it.value = date.month
                it.isVerticalScrollBarEnabled = false
            }
            binding.dayPicker.also {
                it.minValue = 1
                reinitializeDayPicker(it, date.year, date.month)
                it.value = date.dayOfMonth // important to happen _after_ the reinitialization
                it.isVerticalScrollBarEnabled = false
            }
            onValueChangeListener(value)
        }

    private fun reinitializeDayPicker(dayPicker: NumberPicker, year: Int, month: Int) {
        dayPicker.maxValue = calendarType.getMonthLength(year, month)
        val monthStart = Jdn(calendarType, year, month, 1)
        binding.dayPicker.setFormatter {
            (monthStart + it - 1).dayOfWeekName + " / " + formatNumber(it)
        }
        binding.dayPicker.invalidate()
    }

    // Order of setting maxValue vs displayedValues depends on whether current maxValue is
    // less than the going to be one or not, otherwise, it can crash so this applies maxValue
    // and displayedValues in correct order.
    private fun NumberPicker.setMaxValueAndDisplayedValues(
        maxValue: Int, displayedValues: List<String>
    ) {
        if (this.maxValue > maxValue) this.maxValue = maxValue
        this.displayedValues = displayedValues.toTypedArray()
        if (this.maxValue < maxValue) this.maxValue = maxValue
    }

    private val todayJdn = Jdn.today()
    private var currentValue = todayJdn
    private val binding = DayPickerViewBinding.inflate(
        context.layoutInflater, this, true
    ).also { binding ->
        val onDaySelected = NumberPicker.OnValueChangeListener { _, _, _ ->
            performHapticFeedbackTick()
            val year = binding.yearPicker.value
            val month = binding.monthPicker.value
            reinitializeDayPicker(binding.dayPicker, year, month)
            binding.monthPicker.maxValue = calendarType.getYearMonths(year)

            currentValue = value
            onValueChangeListener(currentValue)
        }
        binding.yearPicker.setOnValueChangedListener(onDaySelected)
        binding.monthPicker.setOnValueChangedListener(onDaySelected)
        binding.dayPicker.setOnValueChangedListener(onDaySelected)
    }
}
