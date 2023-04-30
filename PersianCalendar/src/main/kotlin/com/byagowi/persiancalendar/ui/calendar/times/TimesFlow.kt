package com.byagowi.persiancalendar.ui.calendar.times

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StringRes
import androidx.constraintlayout.helper.widget.Flow
import androidx.core.view.isVisible
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.TimeItemBinding
import com.byagowi.persiancalendar.ui.utils.addViewsToFlow
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getTimeNames
import io.github.persiancalendar.praytimes.PrayTimes

class TimesFlow(context: Context, attrs: AttributeSet? = null) : Flow(context, attrs) {

    private var times = emptyList<Pair<@StringRes Int, TimeItemBinding>>()
    fun setup() {
        times =
            getTimeNames().map { name -> name to TimeItemBinding.inflate(context.layoutInflater) }
        addViewsToFlow(times.map { (timeId: Int, timeItemBinding: TimeItemBinding) ->
            timeItemBinding.name.setText(timeId)
            timeItemBinding.root
        })
        toggle()
    }

    fun update(prayTimes: PrayTimes) = times
        .forEach { (timeId: Int, timeItemBinding: TimeItemBinding) ->
            timeItemBinding.time.text = prayTimes.getFromStringId(timeId).toFormattedString()
        }

    var isExpanded = true
    fun toggle() {
        isExpanded = !isExpanded
        val visibility = isExpanded
        times.forEach { (timeId: Int, timeItemBinding: TimeItemBinding) ->
            when (timeId) {
                R.string.fajr, R.string.dhuhr, R.string.maghrib -> Unit
                else -> timeItemBinding.root.isVisible = visibility
            }
        }
    }
}
