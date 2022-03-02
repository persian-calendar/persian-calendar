package com.byagowi.persiancalendar.ui.astronomy

import android.content.Context
import android.icu.util.ChineseCalendar
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import io.github.persiancalendar.calendar.PersianDate

enum class ChineseZodiac(@StringRes private val title: Int, private val emoji: String) {
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

    val bestMatches get() = bestMatchesRaw[ordinal]
    val averageMatches get() = averageMatchesRaw[ordinal]
    val poorMatch get() = poorMatchRaw[ordinal]
    val harmfulMatch get() = harmfulMatchRaw[ordinal]

    companion object {
        /**
         * See also:
         * * The Chinese-Uighur Animal Calendar in Persian Historiography of the Mongol Period
         * * https://en.wikipedia.org/wiki/Chinese_zodiac
         * * https://fa.wikipedia.org/wiki/گاه‌شماری_حیوانی
         */
        fun fromPersianCalendar(persianDate: PersianDate): ChineseZodiac =
            values().getOrNull((persianDate.year + 9) % 12) ?: MONKEY

        @RequiresApi(Build.VERSION_CODES.N)
        fun fromChineseCalendar(date: ChineseCalendar): ChineseZodiac =
            values().getOrNull((date.get(ChineseCalendar.YEAR) + 3) % 12) ?: MONKEY

        // Compatibilities, https://en.wikipedia.org/wiki/Chinese_zodiac#Compatibility
        // They should be turned into formula eventually
        private val bestMatchesRaw = listOf(
            setOf(DRAGON, MONKEY, RAT),
            setOf(SNAKE, ROOSTER, OX),
            setOf(HORSE, DOG, TIGER),
            setOf(PIG, GOAT, RABBIT),
            setOf(RAT, MONKEY, DRAGON),
            setOf(OX, ROOSTER, SNAKE),
            setOf(DOG, TIGER, HORSE),
            setOf(RABBIT, PIG, GOAT),
            setOf(DRAGON, RAT, MONKEY),
            setOf(OX, SNAKE, ROOSTER),
            setOf(TIGER, HORSE, DOG),
            setOf(RABBIT, GOAT, PIG)
        )
        private val averageMatchesRaw = listOf(
            setOf(PIG, TIGER, DOG, SNAKE, RABBIT, ROOSTER, OX),
            setOf(MONKEY, DOG, RABBIT, TIGER, DRAGON, PIG, RAT),
            setOf(RABBIT, DRAGON, ROOSTER, RAT, GOAT, OX, PIG),
            setOf(TIGER, MONKEY, DOG, OX, HORSE, RAT, SNAKE),
            setOf(TIGER, SNAKE, HORSE, GOAT, PIG, OX, ROOSTER),
            setOf(HORSE, DRAGON, GOAT, DOG, RABBIT, RAT, MONKEY),
            setOf(SNAKE, RABBIT, DRAGON, ROOSTER, PIG, MONKEY, GOAT),
            setOf(SNAKE, RABBIT, DRAGON, MONKEY, ROOSTER, DOG, TIGER),
            setOf(DRAGON, DOG, OX, GOAT, RABBIT, ROOSTER, HORSE),
            setOf(HORSE, SNAKE, GOAT, PIG, TIGER, MONKEY, RAT),
            setOf(MONKEY, PIG, RAT, OX, SNAKE, GOAT, RABBIT),
            setOf(RAT, ROOSTER, DOG, DRAGON, HORSE, OX, TIGER)
        )
        private val poorMatchRaw = listOf(
            HORSE, GOAT, MONKEY, ROOSTER, DOG, PIG, RAT, OX, TIGER, RABBIT, DRAGON, SNAKE
        )
        private val harmfulMatchRaw = listOf(
            GOAT, HORSE, SNAKE, DRAGON, RABBIT, TIGER, OX, RAT, PIG, DOG, ROOSTER, MONKEY
        )
    }
}
