package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.helper.widget.Flow
import com.byagowi.persiancalendar.databinding.CalendarItemBinding
import com.byagowi.persiancalendar.utils.*

class CalendarsFlow(context: Context, attrs: AttributeSet?) : Flow(context, attrs),
    View.OnClickListener {

    private val calendarFont = getCalendarFragmentFont(context)

    var bindings = emptyList<CalendarItemBinding>()
    fun update(parentView: ViewGroup, calendarsToShow: List<CalendarType>, jdn: Long) {
        // It implicitly expects the number of calendarsToShow items to not be changed during
        // the view lifecycle
        if (bindings.isEmpty()) {
            bindings = calendarsToShow.map {
                CalendarItemBinding.inflate(context.layoutInflater, parentView, false)
            }
            val applyLineMultiplier = !isCustomFontEnabled
            referencedIds = bindings.map {
                val id = View.generateViewId()
                it.root.id = id
                parentView.addView(it.root)
                it.monthYear.typeface = calendarFont
                it.day.typeface = calendarFont
                if (applyLineMultiplier) it.monthYear.setLineSpacing(0f, .6f)
                it.container.setOnClickListener(this)
                it.linear.setOnClickListener(this)
                id
            }.toIntArray()
        }
        bindings.zip(calendarsToShow) { binding, calendarType ->
            val date = getDateFromJdnOfCalendar(calendarType, jdn)
            val firstCalendarString = formatDate(date)
            binding.also {
                it.linear.text = toLinearDate(date)
                it.linear.contentDescription = toLinearDate(date)
                it.container.contentDescription = firstCalendarString
                it.day.contentDescription = ""
                it.day.text = formatNumber(date.dayOfMonth)
                it.monthYear.contentDescription = ""
                it.monthYear.text =
                    listOf(getMonthName(date), formatNumber(date.year)).joinToString("\n")
            }
        }
    }

    override fun onClick(view: View?) =
        copyToClipboard(view, "converted date", view?.contentDescription)
}