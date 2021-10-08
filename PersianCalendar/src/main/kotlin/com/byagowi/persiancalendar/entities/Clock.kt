package com.byagowi.persiancalendar.entities

import android.content.Context
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.amString
import com.byagowi.persiancalendar.utils.clockIn24
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.pmString
import com.byagowi.persiancalendar.utils.spacedAnd
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

    fun asRemainingTime(context: Context, short: Boolean = false): String {
        val pairs = listOf(R.string.n_hours to hours, R.string.n_minutes to minutes)
            .filter { (_, n) -> n != 0 }
        if (pairs.size == 2 && short) // if both present special casing the short form makes sense
            return context.getString(
                R.string.n_hours_minutes,
                formatNumber(hours),
                formatNumber(minutes)
            )
        return pairs.joinToString(spacedAnd) { (stringId, n) ->
            context.getString(stringId, formatNumber(n))
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
