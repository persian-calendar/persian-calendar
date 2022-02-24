package com.byagowi.persiancalendar.ui.astronomy

import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import io.github.persiancalendar.calendar.PersianDate

// https://en.wikipedia.org/wiki/Chinese_zodiac
// TODO: Implement 'Chinese Zodiac Compatibility-Conflict-Harm' maybe
enum class ChineseZodiac(@StringRes val title: Int, val emoji: String) {
    MONKEY(R.string.year1, "🐒"),
    ROOSTER(R.string.year2, "🐔"),
    DOG(R.string.year3, "🐕"),
    PIG(R.string.year4, "🐖"),
    RAT(R.string.year5, "🐀"),
    OX(R.string.year6, "🐂"),
    TIGER(R.string.year7, "🐅"),
    RABBIT(R.string.year8, "🐇"),
    DRAGON(R.string.year9, "🐲"),
    SNAKE(R.string.year10, "🐍"),
    HORSE(R.string.year11, "🐎"),
    GOAT(R.string.year12, "🐐");

    fun format(context: Context, withEmoji: Boolean) = buildString {
        if (withEmoji && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) append("$emoji ")
        append(context.getString(title))
    }

    companion object {
        // e.g. see 'The Chinese-Uighur Animal Calendar in Persian Historiography of the Mongol Period'
        fun fromPersianCalendar(persianDate: PersianDate): ChineseZodiac =
            values().getOrNull((persianDate.year + 9) % 12) ?: MONKEY
    }
}
