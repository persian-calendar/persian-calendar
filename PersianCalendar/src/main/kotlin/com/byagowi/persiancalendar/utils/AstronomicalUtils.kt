package com.byagowi.persiancalendar.utils

import android.content.Context
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac
import com.byagowi.persiancalendar.ui.astronomy.Zodiac
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate

// Based on Mehdi's work
@StringRes
private val YEARS_NAME = listOf12Items(
    R.string.year10, R.string.year11, R.string.year12, R.string.year1, R.string.year2,
    R.string.year3, R.string.year4, R.string.year5, R.string.year6, R.string.year7, R.string.year8,
    R.string.year9
)

fun isMoonInScorpio(persianDate: PersianDate, islamicDate: IslamicDate) =
    (((islamicDate.dayOfMonth + 1) * 12.2f + (persianDate.dayOfMonth + 1)) / 30f +
            persianDate.month).toInt() % 12 == 8

fun getZodiacInfo(context: Context, jdn: Jdn, withEmoji: Boolean, short: Boolean): String {
    if (!isAstronomicalExtraFeaturesEnabled) return ""
    val persianDate = jdn.toPersianCalendar()
    val islamicDate = jdn.toIslamicCalendar()
    val moonInScorpioText = if (isMoonInScorpio(persianDate, islamicDate))
        context.getString(R.string.moonInScorpio) else ""

    if (short) return moonInScorpioText
    return "%s$spacedColon%s\n%s$spacedColon%s\n%s".format(
        context.getString(R.string.year_name),
        ChineseZodiac.fromPersianCalendar(persianDate).format(context, withEmoji),
        context.getString(R.string.zodiac),
        Zodiac.fromPersianCalendar(persianDate).format(context, withEmoji),
        moonInScorpioText
    ).trim()
}
