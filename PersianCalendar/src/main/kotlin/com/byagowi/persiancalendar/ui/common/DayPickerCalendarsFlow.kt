package com.byagowi.persiancalendar.ui.common

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.constraintlayout.helper.widget.Flow
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.SingleChipLayoutBinding
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.ui.utils.addViewsToFlow
import com.byagowi.persiancalendar.ui.utils.layoutInflater

class DayPickerCalendarsFlow(context: Context, attrs: AttributeSet? = null) : Flow(context, attrs) {

    var changeSelection = fun(_: CalendarType) {}
        private set

    fun setup(
        calendarTypes: List<Pair<CalendarType, String>>,
        onItemClick: (CalendarType) -> Unit
    ) {
        val chips = calendarTypes.map { (_, title) ->
            SingleChipLayoutBinding.inflate(context.layoutInflater).also {
                it.root.text = title
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    it.root.elevation = resources.getDimension(R.dimen.chip_elevation)
            }.root
        }
        changeSelection = fun(calendarType: CalendarType) {
            chips.forEachIndexed { i, chipView ->
                chipView.isClickable = calendarType != calendarTypes[i].first
                chipView.isSelected = calendarType == calendarTypes[i].first
            }
        }
        addViewsToFlow(chips.mapIndexed { i, chip ->
            chip.setOnClickListener {
                onItemClick(calendarTypes[i].first)
                changeSelection(calendarTypes[i].first)
            }
            chip.isCheckable = false
            chip
        })
        changeSelection(calendarTypes[0].first)
    }
}
