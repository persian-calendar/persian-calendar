package com.byagowi.persiancalendar.utils

import android.content.Context
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isAstronomicalFeaturesEnabled
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate

// Based on Mehdi's work
@StringRes
private val YEARS_NAME = listOf12Items(
    R.string.year10, R.string.year11, R.string.year12, R.string.year1, R.string.year2,
    R.string.year3, R.string.year4, R.string.year5, R.string.year6, R.string.year7, R.string.year8,
    R.string.year9
)

@StringRes
private val ZODIAC_MONTHS = listOf12Items(
    R.string.aries, R.string.taurus, R.string.gemini, R.string.cancer, R.string.leo, R.string.virgo,
    R.string.libra, R.string.scorpio, R.string.sagittarius, R.string.capricorn, R.string.aquarius,
    R.string.pisces
)

@StringRes
private val ZODIAC_MONTHS_EMOJI = listOf12Items(
    R.string.aries_emoji, R.string.taurus_emoji, R.string.gemini_emoji, R.string.cancer_emoji,
    R.string.leo_emoji, R.string.virgo_emoji, R.string.libra_emoji, R.string.scorpio_emoji,
    R.string.sagittarius_emoji, R.string.capricorn_emoji, R.string.aquarius_emoji,
    R.string.pisces_emoji
)

fun isMoonInScorpio(persianDate: PersianDate, islamicDate: IslamicDate) =
    (((islamicDate.dayOfMonth + 1) * 12.2f + (persianDate.dayOfMonth + 1)) / 30f +
            persianDate.month).toInt() % 12 == 8

fun getZodiacInfo(context: Context, jdn: Jdn, withEmoji: Boolean, short: Boolean): String {
    if (!isAstronomicalFeaturesEnabled) return ""
    val persianDate = jdn.toPersianCalendar()
    val islamicDate = jdn.toIslamicCalendar()
    val moonInScorpioText = if (isMoonInScorpio(persianDate, islamicDate))
        context.getString(R.string.moonInScorpio) else ""

    if (short) return moonInScorpioText
    return "%s: %s\n%s: %s %s\n%s".format(
        context.getString(R.string.year_name),
        // TODO: Check how a negative year can be passed here
        context.getString(YEARS_NAME.getOrNull(persianDate.year % 12) ?: R.string.empty),
        context.getString(R.string.zodiac),
        if (withEmoji) context.getString(
            ZODIAC_MONTHS_EMOJI.getOrNull(persianDate.month - 1) ?: R.string.empty
        ) else "",
        context.getString(ZODIAC_MONTHS.getOrNull(persianDate.month - 1) ?: R.string.empty),
        moonInScorpioText
    ).trim()
}
