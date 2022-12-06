package com.byagowi.persiancalendar.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.helper.widget.Flow
import com.byagowi.persiancalendar.databinding.CalendarItemBinding
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.utils.addViewsToFlow
import com.byagowi.persiancalendar.ui.utils.copyToClipboard
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.setupLayoutTransition
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.toLinearDate

class CalendarsFlow(context: Context, attrs: AttributeSet? = null) : Flow(context, attrs),
    View.OnClickListener {

    private var bindings = emptyList<CalendarItemBinding>()

    fun update(calendarsToShow: List<CalendarType>, jdn: Jdn) {
        // It implicitly expects the number of calendarsToShow items to not be changed during
        // the view lifecycle
        if (bindings.isEmpty()) {
            bindings = calendarsToShow.map {
                CalendarItemBinding.inflate(context.layoutInflater).also {
                    it.root.setupLayoutTransition()
                }
            }
            addViewsToFlow(bindings.map {
                it.day.setOnClickListener(this)
                it.month.setOnClickListener(this)
                it.linear.setOnClickListener(this)
                it.root
            })
        }
        bindings.zip(calendarsToShow) { binding, calendarType ->
            val date = jdn.toCalendar(calendarType)
            val firstCalendarString = formatDate(date)
            binding.linear.text = date.toLinearDate()
            binding.linear.contentDescription = date.toLinearDate()
            binding.day.contentDescription = firstCalendarString
            binding.month.contentDescription = firstCalendarString
            binding.day.text = formatNumber(date.dayOfMonth)
            binding.month.text = date.monthName
        }
    }

    override fun onClick(view: View?) = context.copyToClipboard(view?.contentDescription)
}
