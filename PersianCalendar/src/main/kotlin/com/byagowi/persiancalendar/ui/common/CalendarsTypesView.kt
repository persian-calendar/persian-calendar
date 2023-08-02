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
    val setCalendarType: (CalendarType) -> Unit

    init {
        val calendarTypes = enabledCalendars.map { calendarType ->
            calendarType to context.getString(
                if (language.betterToUseShortCalendarName) calendarType.shortTitle
                else calendarType.title
            )
        }

        val binding = CalendarsTypesBinding.inflate(context.layoutInflater, this, true)
        binding.calendarsToggleGroup.isSingleSelection = true
        val buttons = calendarTypes.map { (_, title) ->
            CalendarTypeBinding.inflate(context.layoutInflater).also {
                it.root.id = View.generateViewId()
                it.root.text = title
            }.root
        }
        setCalendarType = { calendarType ->
            buttons.forEachIndexed { i, button ->
                if (calendarType == calendarTypes[i].first)
                    binding.calendarsToggleGroup.check(button.id)
            }
        }
        buttons.forEachIndexed { i, button ->
            button.setOnClickListener {
                val (calendarType) = calendarTypes[i]
                onCalendarTypeChange(calendarType)
                setCalendarType(calendarType)
            }
            binding.calendarsToggleGroup.addView(button)
        }
        setCalendarType(calendarTypes[0].first)
    }
}
