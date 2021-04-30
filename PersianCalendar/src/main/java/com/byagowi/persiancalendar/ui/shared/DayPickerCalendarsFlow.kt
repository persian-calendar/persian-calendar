package com.byagowi.persiancalendar.ui.shared

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.constraintlayout.helper.widget.Flow
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.SingleChipLayoutBinding
import com.byagowi.persiancalendar.entities.CalendarTypeItem
import com.byagowi.persiancalendar.utils.CalendarType
import com.byagowi.persiancalendar.utils.addViewsToFlow
import com.byagowi.persiancalendar.utils.layoutInflater

class DayPickerCalendarsFlow(context: Context, attrs: AttributeSet?) : Flow(context, attrs) {
    fun setup(calendarTypes: List<CalendarTypeItem>, onItemClick: (CalendarType) -> Unit) {
        val chips = calendarTypes.map { calendarTypeItem ->
            SingleChipLayoutBinding.inflate(context.layoutInflater).also {
                it.chip.text = calendarTypeItem.toString()
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                        it.chip.elevation = resources.getDimension(R.dimen.chip_elevation)
                    }
                }
            }.root
        }
        addViewsToFlow(chips.mapIndexed { i, chip ->
            chip.setOnClickListener {
                onItemClick(calendarTypes[i].type)
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