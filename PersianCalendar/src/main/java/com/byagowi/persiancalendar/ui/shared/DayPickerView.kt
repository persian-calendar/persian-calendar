package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.NumberPicker
import com.byagowi.persiancalendar.LANG_AR
import com.byagowi.persiancalendar.LANG_EN_US
import com.byagowi.persiancalendar.LANG_JA
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DayPickerViewBinding
import com.byagowi.persiancalendar.utils.*
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

class DayPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    private var mJdn: Long = -1

    var selectedDayListener = fun(_: Long) {}

    var selectedCalendarType: CalendarType = CalendarType.SHAMSI

    var anchorView: View? = null

    private val inflater = context.layoutInflater
    val binding = DayPickerViewBinding.inflate(inflater, this, true).also { binding ->
        val calendarTypes = getOrderedCalendarEntities(
            context, abbreviation = when (language) {
                LANG_EN_US, LANG_JA, LANG_AR -> true
                else -> false
            }
        )
        val chips = calendarTypes.map { calendarTypeItem ->
            (inflater.inflate(
                R.layout.single_chip_layout, binding.calendarTypesBox, false
            ) as Chip).also {
                it.text = calendarTypeItem.toString()
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                        it.elevation = it.resources.getDimension(R.dimen.chip_elevation)
                    }
                }
            }
        }
        chips.forEachIndexed { i, chip ->
            chip.setOnClickListener {
                selectedCalendarType = calendarTypes[i].type
                jdn = mJdn
                selectedDayListener(mJdn)
                chips.forEachIndexed { j, chipView ->
                    chipView.isClickable = i != j
                    chipView.isSelected = i == j
                }
            }
            chip.isClickable = i != 0
            chip.isSelected = i == 0
            chip.isCheckable = false
            selectedCalendarType = calendarTypes[0].type
            binding.calendarTypesBox.addView(chip)
        }

        val onDaySelected = NumberPicker.OnValueChangeListener { _, _, _ ->
            mJdn = jdn
            selectedDayListener(mJdn)
        }
        binding.yearPicker.setOnValueChangedListener(onDaySelected)
        binding.monthPicker.setOnValueChangedListener(onDaySelected)
        binding.dayPicker.setOnValueChangedListener(onDaySelected)
    }

    var jdn: Long
        get() {
            val year = binding.yearPicker.value
            val month = binding.monthPicker.value
            val day = binding.dayPicker.value
            return if (day > getMonthLength(selectedCalendarType, year, month)) {
                Snackbar.make(rootView, R.string.date_exception, Snackbar.LENGTH_SHORT)
                    .setAnchorView(anchorView)
                    .show()
                -1
            } else getDateOfCalendar(selectedCalendarType, year, month, day).toJdn()
        }
        set(value) {
            mJdn = if (value == -1L) getTodayJdn() else value
            val date = getDateFromJdnOfCalendar(selectedCalendarType, mJdn)
            binding.yearPicker.also {
                it.minValue = date.year - 100
                it.maxValue = date.year + 100
                it.value = date.year
                it.setFormatter(::formatNumber)
                it.isVerticalScrollBarEnabled = false
            }
            binding.monthPicker.also {
                it.minValue = 1
                it.maxValue = 12
                it.value = date.month
                val months = monthsNamesOfCalendar(date)
                it.setFormatter { x -> months[x - 1] + " / " + formatNumber(x) }
                it.isVerticalScrollBarEnabled = false
            }
            binding.dayPicker.also {
                it.minValue = 1
                it.maxValue = 31
                it.value = date.dayOfMonth
                it.setFormatter(::formatNumber)
                it.isVerticalScrollBarEnabled = false
            }
            selectedDayListener(value)
        }
}
