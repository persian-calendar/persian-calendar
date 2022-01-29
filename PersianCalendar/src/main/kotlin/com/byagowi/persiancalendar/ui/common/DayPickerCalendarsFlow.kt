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

class DayPickerCalendarsFlow(context: Context, attrs: AttributeSet?) : Flow(context, attrs) {
    fun setup(
        calendarTypes: List<Pair<CalendarType, String>>,
        onItemClick: (CalendarType) -> Unit
    ) {
        val chips = calendarTypes.map { (_, title) ->
            SingleChipLayoutBinding.inflate(context.layoutInflater).also {
                it.root.text = title
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                        it.root.elevation = resources.getDimension(R.dimen.chip_elevation)
                    }
                }
            }.root
        }
        addViewsToFlow(chips.mapIndexed { i, chip ->
            chip.setOnClickListener {
                onItemClick(calendarTypes[i].first)
                chips.forEachIndexed { j, chipView ->
                    chipView.isClickable = i != j
                    chipView.isSelected = i == j
                }
            }
            chip.isClickable = i != 0
            chip.isSelected = i == 0
            chip.isCheckable = false
            chip
        })
    }
}
