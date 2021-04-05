package com.byagowi.persiancalendar.ui.calendar.times

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.constraintlayout.helper.widget.Flow
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.TimeItemBinding
import com.byagowi.persiancalendar.utils.layoutInflater
import com.byagowi.persiancalendar.utils.toFormattedString
import io.github.persiancalendar.praytimes.PrayTimes

class TimesFlow(context: Context, attrs: AttributeSet) : Flow(context, attrs) {

    var times = emptyList<Pair<@StringRes Int, TimeItemBinding>>()
    fun setup(parentView: ViewGroup) {
        if (times.isNotEmpty()) return
        times = timeNames.map { name ->
            name to TimeItemBinding.inflate(context.layoutInflater, parentView, false)
        }
        referencedIds = times.map {
            val id = View.generateViewId()
            it.second.root.id = id
            parentView.addView(it.second.root)
            it.second.name.setText(it.first)
            id
        }.toIntArray()
        toggle()
    }

    @StringRes
    private val timeNames = listOf(
        R.string.imsak, R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.asr,
        R.string.sunset, R.string.maghrib, R.string.isha, R.string.midnight
    )

    private fun stringIdToOwghat(timeName: Int, prayTime: PrayTimes) = when (timeName) {
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

    fun update(prayTimes: PrayTimes) = times.forEach {
        it.second.time.text = stringIdToOwghat(it.first, prayTimes).toFormattedString()
    }

    var isExpanded = true
    fun toggle() {
        val visibility = if (isExpanded) View.GONE else View.VISIBLE
        times.forEach {
            when (it.first) {
                R.string.fajr, R.string.dhuhr, R.string.maghrib -> Unit
                else -> it.second.root.visibility = visibility
            }
        }
        isExpanded = !isExpanded
    }
}