package com.byagowi.persiancalendar.ui.astronomy

import android.content.Context
import android.icu.util.ChineseCalendar
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import io.github.persiancalendar.calendar.PersianDate

/**
 * The follow table is copied from https://en.wikipedia.org/wiki/Chinese_zodiac
 *
 * | Number |  Animal | Yin/Yang | Trine | Fixed Element |
 * |:------:|:-------:|:--------:|:-----:|:-------------:|
 * | 1      | Rat     | Yang     | 1st   | Water         |
 * | 2      | Ox      | Yin      | 2nd   | Earth         |
 * | 3      | Tiger   | Yang     | 3rd   | Wood          |
 * | 4      | Rabbit  | Yin      | 4th   | Wood          |
 * | 5      | Dragon  | Yang     | 1st   | Earth         |
 * | 6      | Snake   | Yin      | 2nd   | Fire          |
 * | 7      | Horse   | Yang     | 3rd   | Fire          |
 * | 8      | Goat    | Yin      | 4th   | Earth         |
 * | 9      | Monkey  | Yang     | 1st   | Metal         |
 * | 10     | Rooster | Yin      | 2nd   | Metal         |
 * | 11     | Dog     | Yang     | 3rd   | Earth         |
 * | 12     | Pig     | Yin      | 4th   | Water         |
 *
 * The follow poem is copied from https://fa.wikipedia.org/wiki/گاه‌شماری_حیوانی
 *
 * موش و بقر و پلنگ و خرگوش شمار - زان چار چو بگذری نهنگ آید و مار
 *آنگاه به اسب و گوسفند است حساب - حمدونه و مرغ و سگ و خوک آخر کار
 */
enum class ChineseZodiac(@StringRes private val title: Int, private val emoji: String) {
    RAT(R.string.animal_year_name_rat, "🐀"),
    OX(R.string.animal_year_name_ox, "🐂"),
    TIGER(R.string.animal_year_name_tiger, "🐅"),
    RABBIT(R.string.animal_year_name_rabbit, "🐇"),
    DRAGON(R.string.animal_year_name_dragon, "🐲"),
    SNAKE(R.string.animal_year_name_snake, "🐍"),
    HORSE(R.string.animal_year_name_horse, "🐎"),
    GOAT(R.string.animal_year_name_goat, "🐐"),
    MONKEY(R.string.animal_year_name_monkey, "🐒"),
    ROOSTER(R.string.animal_year_name_rooster, "🐔"),
    DOG(R.string.animal_year_name_dog, "🐕"),
    PIG(R.string.animal_year_name_pig, "🐖");

    fun format(context: Context, withEmoji: Boolean) = buildString {
        if (withEmoji && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) append("$emoji ")
        append(context.getString(title))
    }

    val bestMatches get() = bestMatchesRaw[ordinal]
    val averageMatches get() = averageMatchesRaw[ordinal]
    val poorMatch get() = poorMatchRaw[ordinal]
    val harmfulMatch get() = harmfulMatchRaw[ordinal]

    companion object {
        fun fromPersianCalendar(persianDate: PersianDate): ChineseZodiac =
            values().getOrNull((persianDate.year + 5) % 12) ?: RAT

        @RequiresApi(Build.VERSION_CODES.N)
        fun fromChineseCalendar(date: ChineseCalendar): ChineseZodiac =
            values().getOrNull((date[ChineseCalendar.YEAR] - 1) % 12) ?: RAT

        /*
         * Compatibilities, they should be turned into formula eventually.
         *
         * The follow table is copied from https://en.wikipedia.org/wiki/Chinese_zodiac#Compatibility
         *
         * |   Sign  |      Best Match     |                    Average Match                   | Super Bad | Harmful |
         * |:-------:|:-------------------:|:--------------------------------------------------:|:---------:|---------|
         * | Rat     | Dragon, Monkey, Rat | Pig, Tiger, Dog, Snake, Rabbit, Rooster, Ox        | Horse     | Goat    |
         * | Ox      | Snake, Rooster, Ox  | Monkey, Dog, Rabbit, Tiger, Dragon, Pig, Rat       | Goat      | Horse   |
         * | Tiger   | Horse, Dog, Tiger   | Rabbit, Dragon, Rooster, Rat, Goat, Ox, Pig        | Monkey    | Snake   |
         * | Rabbit  | Pig, Goat, Rabbit   | Tiger, Monkey, Goat, Ox, Horse, Rat, Snake         | Rooster   | Dragon  |
         * | Dragon  | Rat, Monkey, Dragon | Tiger, Snake, Horse, Goat, Pig, Ox, Rooster        | Dog       | Rabbit  |
         * | Snake   | Ox, Rooster, Snake  | Horse, Dragon, Goat, Dog, Rabbit, Rat, Monkey      | Pig       | Tiger   |
         * | Horse   | Dog, Tiger, Horse   | Snake, Rabbit, Dragon, Rooster, Pig, Monkey, Goat  | Rat       | Ox      |
         * | Goat    | Rabbit, Pig, Goat   | Snake, Rabbit, Dragon, Monkey, Rooster, Dog, Tiger | Ox        | Rat     |
         * | Monkey  | Dragon, Rat, Monkey | Dragon, Dog, Ox, Goat, Rabbit, Rooster, Horse      | Tiger     | Pig     |
         * | Rooster | Ox, Snake, Rooster  | Horse, Snake, Goat, Pig, Tiger, Monkey, Rat        | Rabbit    | Dog     |
         * | Dog     | Tiger, Horse, Dog   | Monkey, Pig, Rat, Ox, Snake, Goat, Rabbit          | Dragon    | Rooster |
         * | Pig     | Rabbit, Goat, Pig   | Rat, Rooster, Dog, Dragon, Horse, Ox, Tiger        | Snake     | Monkey  |
         */

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
