package com.byagowi.persiancalendar.utils

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate

// Based on Mehdi's work
@StringRes
private val YEARS_NAME = listOf(
        R.string.year10, R.string.year11, R.string.year12, R.string.year1, R.string.year2,
        R.string.year3, R.string.year4, R.string.year5, R.string.year6, R.string.year7, R.string.year8,
        R.string.year9
)

@StringRes
private val ZODIAC_MONTHS = listOf(
        R.string.empty,
        R.string.aries, R.string.taurus, R.string.gemini, R.string.cancer, R.string.leo, R.string.virgo,
        R.string.libra, R.string.scorpio, R.string.sagittarius, R.string.capricorn, R.string.aquarius,
        R.string.pisces
)

@StringRes
private val ZODIAC_MONTHS_EMOJI = listOf(
        R.string.empty,
        R.string.aries_emoji, R.string.taurus_emoji, R.string.gemini_emoji, R.string.cancer_emoji,
        R.string.leo_emoji, R.string.virgo_emoji, R.string.libra_emoji, R.string.scorpio_emoji,
        R.string.sagittarius_emoji, R.string.capricorn_emoji, R.string.aquarius_emoji,
        R.string.pisces_emoji
)

fun isMoonInScorpio(persianDate: PersianDate, islamicDate: IslamicDate): Boolean {
    var res =
            (((islamicDate.dayOfMonth + 1).toFloat() * 12.2f + (persianDate.dayOfMonth + 1)) / 30f + persianDate.month).toInt()
    if (res > 12) res -= 12
    return res == 8
}

fun getZodiacInfo(context: Context, jdn: Long, withEmoji: Boolean): String = try {
    if (isAstronomicalFeaturesEnabled) {
        val persianDate = PersianDate(jdn)
        val islamicDate = IslamicDate(jdn)
        "%s: %s\n%s: %s %s\n%s".format(
                context.getString(R.string.year_name),
                context.getString(YEARS_NAME[persianDate.year % 12]),
                context.getString(R.string.zodiac),
                if (withEmoji) context.getString(
                        ZODIAC_MONTHS_EMOJI.getOrNull(persianDate.month) ?: R.string.empty
                ) else "",
                context.getString(ZODIAC_MONTHS.getOrNull(persianDate.month) ?: R.string.empty),
                if (isMoonInScorpio(persianDate, islamicDate))
                    context.getString(R.string.moonInScorpio)
                else ""
        ).trim()
    } else ""
} catch (e: Exception) {
    // This shouldn't raise out-of-bound in any sense, I am really tired of any exceptions this raises
    Log.e("Utils", "getZodiacInfo", e)
    ""
}
