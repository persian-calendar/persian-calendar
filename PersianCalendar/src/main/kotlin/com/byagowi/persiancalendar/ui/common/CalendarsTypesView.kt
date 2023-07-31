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

    var changeSelection = fun(_: CalendarType) {}
        private set

    var onItemClick = fun(_: CalendarType) {}

    init {
        val calendarTypes = enabledCalendars.map { calendarType ->
            calendarType to context.getString(
                if (language.betterToUseShortCalendarName) calendarType.shortTitle
                else calendarType.title
            )
        }

        val binding = CalendarsTypesBinding.inflate(context.layoutInflater, this, true)
        binding.buttonGroup.isSingleSelection = true
        val buttons = calendarTypes.map { (_, title) ->
            CalendarTypeBinding.inflate(context.layoutInflater).also {
                it.root.id = View.generateViewId()
                it.root.text = title
            }.root
        }
        changeSelection = { calendarType ->
            buttons.forEachIndexed { i, button ->
                if (calendarType == calendarTypes[i].first) binding.buttonGroup.check(button.id)
            }
        }
        buttons.forEachIndexed { i, button ->
            button.setOnClickListener {
                val (calendarType) = calendarTypes[i]
                onItemClick(calendarType)
                changeSelection(calendarType)
            }
            binding.buttonGroup.addView(button)
        }
        changeSelection(calendarTypes[0].first)
    }
}
