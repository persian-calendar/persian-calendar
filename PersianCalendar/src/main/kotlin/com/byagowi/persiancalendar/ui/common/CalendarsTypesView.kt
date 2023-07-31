package com.byagowi.persiancalendar.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.HorizontalScrollView
import com.byagowi.persiancalendar.databinding.CalendarTypeBinding
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.google.android.material.button.MaterialButtonToggleGroup

class CalendarsTypesView(context: Context, attrs: AttributeSet? = null) :
    HorizontalScrollView(context, attrs) {

    var changeSelection = fun(_: CalendarType) {}
        private set

    fun setup(
        calendarTypes: List<Pair<CalendarType, String>>,
        onItemClick: (CalendarType) -> Unit
    ) {
        val buttonGroup = MaterialButtonToggleGroup(context)
        buttonGroup.isSingleSelection = true
        val buttons = calendarTypes.map { (_, title) ->
            CalendarTypeBinding.inflate(context.layoutInflater).also {
                it.root.id = View.generateViewId()
                it.root.text = title
            }.root
        }
        changeSelection = { calendarType ->
            buttons.forEachIndexed { i, button ->
                if (calendarType == calendarTypes[i].first) buttonGroup.check(button.id)
            }
        }
        buttons.forEachIndexed { i, button ->
            button.setOnClickListener {
                onItemClick(calendarTypes[i].first)
                changeSelection(calendarTypes[i].first)
            }
            buttonGroup.addView(button)
        }
        changeSelection(calendarTypes[0].first)
        addView(buttonGroup)
    }
}
