package com.byagowi.persiancalendar.ui.shared

import android.animation.LayoutTransition
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

    var calendars = emptyList<Pair<CalendarType, CalendarItemBinding>>()
    fun update(parentView: ViewGroup, calendarsToShow: List<CalendarType>, jdn: Long) {
        // It implicitly expects calendarsToShow to not be changed during the view lifecycle
        if (calendars.isEmpty()) {
            calendars = calendarsToShow.map {
                it to CalendarItemBinding.inflate(context.layoutInflater, parentView, false)
            }
            val applyLineMultiplier = !isCustomFontEnabled
            referencedIds = calendars.map {
                val id = View.generateViewId()
                it.second.root.id = id
                parentView.addView(it.second.root)
                it.second.apply {
                    monthYear.typeface = calendarFont
                    day.typeface = calendarFont
                    if (applyLineMultiplier) monthYear.setLineSpacing(0f, .6f)
                    container.setOnClickListener(this@CalendarsFlow)
                    linear.setOnClickListener(this@CalendarsFlow)
                }
                id
            }.toIntArray()
        }
        calendars.map {
            val date = getDateFromJdnOfCalendar(it.first, jdn)
            val firstCalendarString = formatDate(date)
            it.second.apply {
                linear.text = toLinearDate(date)
                linear.contentDescription = toLinearDate(date)
                container.contentDescription = firstCalendarString
                day.contentDescription = ""
                day.text = formatNumber(date.dayOfMonth)
                monthYear.contentDescription = ""
                monthYear.text =
                    listOf(getMonthName(date), formatNumber(date.year)).joinToString("\n")
            }
        }
    }

    override fun onClick(view: View?) =
        copyToClipboard(view, "converted date", view?.contentDescription)
}