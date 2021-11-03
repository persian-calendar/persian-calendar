package com.byagowi.persiancalendar.entities

import android.content.res.Resources
import androidx.annotation.PluralsRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.amString
import com.byagowi.persiancalendar.global.clockIn24
import com.byagowi.persiancalendar.global.pmString
import com.byagowi.persiancalendar.global.spacedAnd
import com.byagowi.persiancalendar.utils.formatNumber
import java.util.*

data class Clock(val hours: Int, val minutes: Int) {
    constructor(calendar: Calendar) :
            this(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

    fun toMinutes() = hours * 60 + minutes

    fun toBasicFormatString(hours: Int = this.hours): String =
        formatNumber("%d:%02d".format(Locale.ENGLISH, hours, minutes))

    fun toFormattedString(forcedIn12: Boolean = false, printAmPm: Boolean = true) =
        if (clockIn24 && !forcedIn12) toBasicFormatString()
        else toBasicFormatString((hours % 12).takeIf { it != 0 } ?: 12) + if (printAmPm) {
            " " + if (hours >= 12) pmString else amString
        } else ""

    fun asRemainingTime(resources: Resources, short: Boolean = false): String {
        val pairs = listOf(R.plurals.n_hours to hours, R.plurals.n_minutes to minutes)
            .filter { (_, n) -> n != 0 }
        // if both present special casing the short form makes sense
        return if (pairs.size == 2 && short) resources.getString(
            R.string.n_hours_minutes, formatNumber(hours), formatNumber(minutes)
        ) else pairs.joinToString(spacedAnd) { (@PluralsRes pluralId: Int, n: Int) ->
            resources.getQuantityString(pluralId, n, formatNumber(n))
        }
    }

    companion object {
        fun fromDouble(input: Double): Clock {
            val value = (input + 0.5 / 60) % 24 // add 0.5 minutes to round
            val hours = value.toInt()
            val minutes = ((value - hours) * 60.0).toInt()
            return Clock(hours, minutes)
        }

        fun fromInt(minutes: Int) = Clock(minutes / 60, minutes % 60)
    }
}
