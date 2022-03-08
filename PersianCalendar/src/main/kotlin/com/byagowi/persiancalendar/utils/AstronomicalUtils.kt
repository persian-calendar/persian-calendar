package com.byagowi.persiancalendar.utils

import android.content.Context
import android.icu.util.ChineseCalendar
import android.os.Build
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac
import com.byagowi.persiancalendar.ui.astronomy.Eclipse
import com.byagowi.persiancalendar.ui.astronomy.Zodiac
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import java.util.*

// Based on Mehdi's work

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
    return "%s\n%s$spacedColon%s\n%s".format(
        generateYearName(null, context, persianDate, withEmoji),
        context.getString(R.string.zodiac),
        Zodiac.fromPersianCalendar(persianDate).format(context, withEmoji),
        moonInScorpioText
    ).trim()
}

fun generateAstronomyHeaderText(
    time: GregorianCalendar,
    context: Context,
    persianDate: PersianDate
): String {
    return (listOf(
        R.string.solar_eclipse to Eclipse.Category.SOLAR,
        R.string.lunar_eclipse to Eclipse.Category.LUNAR
    ).map { (title, eclipseCategory) ->
        val eclipse = Eclipse(time, eclipseCategory, true)
        val date = eclipse.maxPhaseDate.toJavaCalendar().formatDateAndTime()
        (language.tryTranslateEclipseType(eclipse.type) ?: context.getString(title)) +
                spacedColon + date
    } + listOf(generateYearName(time, context, persianDate, true))).joinToString("\n")
}

private fun generateYearName(
    time: GregorianCalendar?,
    context: Context,
    persianDate: PersianDate,
    withEmoji: Boolean
): String {
    val yearNames = listOfNotNull(
        language.inParentheses.format(
            ChineseZodiac.fromPersianCalendar(persianDate).format(context, withEmoji),
            context.getString(R.string.shamsi_calendar_short)
        ),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val date = ChineseCalendar(time?.time ?: Jdn(persianDate).toJavaCalendar().time)
            val year = date.get(ChineseCalendar.YEAR)
            language.inParentheses.format(
                ChineseZodiac.fromChineseCalendar(date).format(context, withEmoji),
                context.getString(R.string.chinese) + spacedComma + formatNumber(year)
            )
        } else null
    ).let { if (language.isUserAbleToReadPersian) it else it.reversed() }.joinToString(" ")
    return "%s$spacedColon%s".format(context.getString(R.string.year_name), yearNames)
}

