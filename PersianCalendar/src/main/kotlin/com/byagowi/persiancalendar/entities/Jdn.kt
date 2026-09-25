package com.byagowi.persiancalendar.entities

import androidx.compose.runtime.annotation.RememberInComposition
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.cosinekitty.astronomy.Time
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone

fun Jdn.toAstronomyTime(hourOfDay: Int, setIranTime: Boolean = false): Time {
    val date = toGregorianCalendar()
    if (setIranTime) date.timeZone = TimeZone.getTimeZone(IRAN_TIMEZONE_ID)
    date[GregorianCalendar.HOUR_OF_DAY] = hourOfDay
    date[GregorianCalendar.MINUTE] = 0
    date[GregorianCalendar.SECOND] = 0
    date[GregorianCalendar.MILLISECOND] = 0
    return Time.fromMillisecondsSince1970(date.timeInMillis)
}

fun Jdn.toGregorianCalendar(): GregorianCalendar = GregorianCalendar().also {
    val gregorian = this.toCivilDate()
    it.set(gregorian.year, gregorian.month - 1, gregorian.dayOfMonth)
}

// Better to use App provided today() where possible
@RememberInComposition
fun Jdn.Companion.today() = Jdn(Date().toGregorianCalendar().toCivilDate())
