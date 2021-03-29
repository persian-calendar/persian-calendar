package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
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

    var anchorView: View? = null

    private val inflater = context.layoutInflater
    val binding: DayPickerViewBinding =
        DayPickerViewBinding.inflate(inflater, this, true).also { dayPickerViewBinding ->
            val calendarTypes = getOrderedCalendarEntities(getContext())
            val chips = calendarTypes.map { calendarTypeItem ->
                (inflater.inflate(
                    R.layout.single_chip_layout, dayPickerViewBinding.calendarTypesBox, false
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
                dayPickerViewBinding.calendarTypesBox.addView(chip)
            }

            val onDaySelected = NumberPicker.OnValueChangeListener { _, _, _ ->
                jdn = dayJdnFromView
                selectedDayListener(jdn)
            }
            dayPickerViewBinding.yearPicker.setOnValueChangedListener(onDaySelected)
            dayPickerViewBinding.monthPicker.setOnValueChangedListener(onDaySelected)
            dayPickerViewBinding.dayPicker.setOnValueChangedListener(onDaySelected)
        }

    val dayJdnFromView: Long
        get() {
            val year = binding.yearPicker.value
            val month = binding.monthPicker.value
            val day = binding.dayPicker.value
            return when {
                day > getMonthLength(selectedCalendarType, year, month) -> {
                    Snackbar.make(rootView, R.string.date_exception, Snackbar.LENGTH_SHORT)
                        .setAnchorView(anchorView)
                        .show()
                    -1
                }
                else -> {
                    getDateOfCalendar(selectedCalendarType, year, month, day).toJdn()
                }
            }
        }

    fun setDayJdnOnView(jdn: Long) {
        this.jdn = if (jdn == -1L) getTodayJdn() else jdn
        val date = getDateFromJdnOfCalendar(selectedCalendarType, this.jdn)
        binding.yearPicker.also { numberPicker ->
            numberPicker.minValue = date.year - 100
            numberPicker.maxValue = date.year + 100
            numberPicker.value = date.year
            numberPicker.setFormatter { formatNumber(it) }
            numberPicker.isVerticalScrollBarEnabled = false
        }
        binding.monthPicker.also { numberPicker ->
            numberPicker.minValue = 1
            numberPicker.maxValue = 12
            numberPicker.value = date.month
            val months = monthsNamesOfCalendar(date)
            numberPicker.setFormatter { months[it - 1] + " / " + formatNumber(it) }
            numberPicker.isVerticalScrollBarEnabled = false
        }
        binding.dayPicker.also { numberPicker ->
            numberPicker.minValue = 1
            numberPicker.maxValue = 31
            numberPicker.value = date.dayOfMonth
            numberPicker.setFormatter { formatNumber(it) }
            numberPicker.isVerticalScrollBarEnabled = false
        }
        selectedDayListener(jdn)
    }
}
