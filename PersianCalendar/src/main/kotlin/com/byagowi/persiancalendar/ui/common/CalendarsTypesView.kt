package com.byagowi.persiancalendar.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.byagowi.persiancalendar.databinding.CalendarTypeBinding
import com.byagowi.persiancalendar.databinding.CalendarsTypesBinding
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.utils.layoutInflater

class CalendarsTypesView(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    var onCalendarTypeChange = fun(_: CalendarType) {}
    var calendarType: CalendarType = enabledCalendars[0]
        set(value) {
            field = value
            buttons.forEachIndexed { i, button ->
                if (value == calendarTypes[i].first) binding.toggleGroup.check(button.id)
            }
        }
    private val calendarTypes = enabledCalendars.map { calendarType ->
        calendarType to context.getString(
            if (language.betterToUseShortCalendarName) calendarType.shortTitle
            else calendarType.title
        )
    }
    private val binding = CalendarsTypesBinding.inflate(context.layoutInflater, this, true).also {
        it.toggleGroup.isSingleSelection = true
    }
    private val buttons = calendarTypes.map { (calendarType, title) ->
        CalendarTypeBinding.inflate(context.layoutInflater, binding.toggleGroup, true).also {
            it.root.id = View.generateViewId()
            it.root.text = title
            it.root.setOnClickListener { onCalendarTypeChange(calendarType) }
        }.root
    }

    init {
        calendarType = calendarTypes[0].first // to make one button checked at least
    }
}
