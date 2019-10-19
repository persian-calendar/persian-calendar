package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.SimpleDayPickerViewBinding
import com.byagowi.persiancalendar.entities.CalendarTypeItem
import com.byagowi.persiancalendar.entities.StringWithValueItem
import com.byagowi.persiancalendar.utils.CalendarType
import com.byagowi.persiancalendar.utils.Utils
import com.byagowi.persiancalendar.utils.formatNumber
import java.util.*

class SimpleDayPickerView : FrameLayout, AdapterView.OnItemSelectedListener, DayPickerView {
    lateinit var binding: SimpleDayPickerViewBinding
    private var jdn: Long = -1
    private var selectedDayListener: DayPickerView.OnSelectedDayChangedListener? = null

    override val dayJdnFromView: Long
        get() {
            val year = (binding.yearSpinner.selectedItem as StringWithValueItem).value
            val month = (binding.monthSpinner.selectedItem as StringWithValueItem).value
            val day = (binding.daySpinner.selectedItem as StringWithValueItem).value

            try {
                val selectedCalendarType = selectedCalendarType
                if (day > Utils.getMonthLength(selectedCalendarType, year, month))
                    throw Exception("Not a valid day")

                return Utils.getDateOfCalendar(selectedCalendarType, year, month, day).toJdn()
            } catch (e: Exception) {
                Utils.createAndShowShortSnackbar(rootView, R.string.date_exception)
                Log.e("SelectDayDialog", "", e)
            }

            return -1
        }

    override val selectedCalendarType: CalendarType
        get() = (binding.calendarTypeSpinner.selectedItem as CalendarTypeItem).type

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.simple_day_picker_view, this, true)

        binding.calendarTypeSpinner.adapter = ArrayAdapter(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Utils.getOrderedCalendarEntities(getContext()))

        binding.calendarTypeSpinner.setSelection(0)
        binding.calendarTypeSpinner.onItemSelectedListener = this

        binding.yearSpinner.onItemSelectedListener = this
        binding.monthSpinner.onItemSelectedListener = this
        binding.daySpinner.onItemSelectedListener = this
    }

    override fun setDayJdnOnView(jdn: Long) {
        var jdn = jdn
        this.jdn = jdn

        val context = context ?: return

        if (jdn == -1L) {
            jdn = Utils.getTodayJdn()
        }

        val date = Utils.getDateFromJdnOfCalendar(
                selectedCalendarType,
                jdn)

        // years spinner init.
        val years = ArrayList<StringWithValueItem>()
        val YEARS = 200
        val startingYearOnYearSpinner = date.year - YEARS / 2
        for (i in 0 until YEARS) {
            years.add(StringWithValueItem(i + startingYearOnYearSpinner,
                    formatNumber(i + startingYearOnYearSpinner)))
        }
        binding.yearSpinner.adapter = ArrayAdapter(context,
                android.R.layout.simple_spinner_dropdown_item, years)
        binding.yearSpinner.setSelection(YEARS / 2)
        //

        // month spinner init.
        val months = ArrayList<StringWithValueItem>()
        val monthsTitle = Utils.monthsNamesOfCalendar(date)
        for (i in 1..12) {
            months.add(StringWithValueItem(i,
                    monthsTitle[i - 1] + " / " + formatNumber(i)))
        }
        binding.monthSpinner.adapter = ArrayAdapter(context,
                android.R.layout.simple_spinner_dropdown_item, months)
        binding.monthSpinner.setSelection(date.month - 1)
        //

        // days spinner init.
        val days = ArrayList<StringWithValueItem>()
        for (i in 1..31) {
            days.add(StringWithValueItem(i, formatNumber(i)))
        }
        binding.daySpinner.adapter = ArrayAdapter(context,
                android.R.layout.simple_spinner_dropdown_item, days)
        binding.daySpinner.setSelection(date.dayOfMonth - 1)
    }

    override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
        if (adapterView?.id == R.id.calendarTypeSpinner)
            setDayJdnOnView(jdn)
        else
            jdn = dayJdnFromView

        selectedDayListener?.onSelectedDayChanged(jdn)
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {

    }

    override fun setOnSelectedDayChangedListener(listener: DayPickerView.OnSelectedDayChangedListener) {
        selectedDayListener = listener
    }
}
