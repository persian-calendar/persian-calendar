package com.byagowi.persiancalendar.ui.calendar.times

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.StringRes
import androidx.constraintlayout.helper.widget.Flow
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.TimeItemBinding
import com.byagowi.persiancalendar.utils.addViewsToFlow
import com.byagowi.persiancalendar.utils.layoutInflater
import com.byagowi.persiancalendar.utils.toFormattedString
import io.github.persiancalendar.praytimes.PrayTimes

class TimesFlow(context: Context, attrs: AttributeSet?) : Flow(context, attrs) {

    private var times = emptyList<Pair<@StringRes Int, TimeItemBinding>>()
    fun setup() {
        times = timeNames.map { name -> name to TimeItemBinding.inflate(context.layoutInflater) }
        addViewsToFlow(times.map { (timeId: Int, timeItemBinding: TimeItemBinding) ->
            timeItemBinding.name.setText(timeId)
            timeItemBinding.root
        })
        toggle()
    }

    private val timeNames = listOf<@StringRes Int>(
        R.string.imsak, R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.asr,
        R.string.sunset, R.string.maghrib, R.string.isha, R.string.midnight
    )

    private fun stringIdToOwghat(@StringRes timeName: Int, prayTime: PrayTimes) = when (timeName) {
        R.string.imsak -> prayTime.imsakClock
        R.string.fajr -> prayTime.fajrClock
        R.string.sunrise -> prayTime.sunriseClock
        R.string.dhuhr -> prayTime.dhuhrClock
        R.string.asr -> prayTime.asrClock
        R.string.sunset -> prayTime.sunsetClock
        R.string.maghrib -> prayTime.maghribClock
        R.string.isha -> prayTime.ishaClock
        R.string.midnight -> prayTime.midnightClock
        else -> prayTime.midnightClock
    }

    fun update(prayTimes: PrayTimes) = times
        .forEach { (timeId: Int, timeItemBinding: TimeItemBinding) ->
            timeItemBinding.time.text = stringIdToOwghat(timeId, prayTimes).toFormattedString()
        }

    var isExpanded = true
    fun toggle() {
        isExpanded = !isExpanded
        val visibility = if (isExpanded) View.VISIBLE else View.GONE
        times.forEach { (timeId: Int, timeItemBinding: TimeItemBinding) ->
            when (timeId) {
                R.string.fajr, R.string.dhuhr, R.string.maghrib -> Unit
                else -> timeItemBinding.root.visibility = visibility
            }
        }
    }
}