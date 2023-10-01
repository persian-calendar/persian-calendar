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
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackPress

class CalendarsTypesView(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    var onValueChangeListener = fun(_: CalendarType) {}
    var value: CalendarType = enabledCalendars[0]
        set(value) {
            field = value
            buttons.forEachIndexed { i, button ->
                if (value == enabledCalendars[i]) binding.toggleGroup.check(button.id)
            }
        }
    private val binding = CalendarsTypesBinding.inflate(context.layoutInflater, this, true).also {
        it.toggleGroup.isSingleSelection = true
    }
    private val buttons = enabledCalendars.map { calendarType ->
        CalendarTypeBinding.inflate(context.layoutInflater, binding.toggleGroup, true).also {
            it.root.id = View.generateViewId()
            it.root.text = context.getString(
                if (language.betterToUseShortCalendarName) calendarType.shortTitle
                else calendarType.title
            )
            it.root.setOnClickListener {
                performHapticFeedbackPress()
                onValueChangeListener(calendarType)
                value = calendarType
            }
        }.root
    }

    init {
        value = enabledCalendars[0] // to make one button checked at least
    }
}
