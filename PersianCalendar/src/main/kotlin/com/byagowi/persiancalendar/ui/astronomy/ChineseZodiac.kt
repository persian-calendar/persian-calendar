package com.byagowi.persiancalendar.ui.astronomy

import android.content.res.Resources
import android.icu.util.ChineseCalendar
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import io.github.persiancalendar.calendar.PersianDate

/**
 * The following table is copied from https://en.wikipedia.org/wiki/Chinese_zodiac
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
 * The following poem is copied from https://fa.wikipedia.org/wiki/گاه‌شماری_حیوانی
 *
 * موش و بقر و پلنگ و خرگوش شمار - زان چار چو بگذری نهنگ آید و مار
 *آنگاه به اسب و گوسفند است حساب - حمدونه و مرغ و سگ و خوک آخر کار
 */
enum class ChineseZodiac(
    @get:StringRes private val title: Int, private val emoji: String,
    // For example used in https://rc.majlis.ir/fa/law/show/91137
    private val oldEraPersianName: String,
    private val persianAlternativeEmoji: String? = null,
    private val persianAlternativeTitle: String? = null,
) {
    RAT(R.string.animal_year_name_rat, "🐀", "سیچقان ئیل"),
    OX(R.string.animal_year_name_ox, "🐂", "اود ئیل"),
    TIGER(R.string.animal_year_name_tiger, "🐅", "بارس ئیل", "🐆", "پلنگ"),
    RABBIT(R.string.animal_year_name_rabbit, "🐇", "توشقان ئیل"),
    DRAGON(R.string.animal_year_name_dragon, "🐲", "لوی ئیل", "🐊", "نهنگ"),
    SNAKE(R.string.animal_year_name_snake, "🐍", "ئیلان ئیل"),
    HORSE(R.string.animal_year_name_horse, "🐎", "یونت ئیل"),
    GOAT(R.string.animal_year_name_goat, "🐐", "قوی ئیل", "🐑", "گوسفند"),
    MONKEY(R.string.animal_year_name_monkey, "🐒", "پیچی ئیل"),
    ROOSTER(R.string.animal_year_name_rooster, "🐓", "تخاقوی ئیل", "🐔", "مرغ"),
    DOG(R.string.animal_year_name_dog, "🐕", "ایت ئیل"),
    PIG(R.string.animal_year_name_pig, "🐖", "تنگوز ئیل");

    fun format(
        resources: Resources,
        withEmoji: Boolean,
        isPersian: Boolean,
        withOldEraName: Boolean = false,
    ): String = buildString {
        if (withEmoji) append("${resolveEmoji(isPersian)} ")
        append(resolveTitle(isPersian, resources))
        if (withOldEraName) append(" «$oldEraPersianName»")
    }

    fun formatForHoroscope(resources: Resources, isPersian: Boolean): String = buildString {
        appendLine(resolveEmoji(isPersian))
        if (isPersian) appendLine(oldEraPersianName)
        append(resolveTitle(isPersian, resources))
    }

    private fun resolveEmoji(isPersian: Boolean): String =
        persianAlternativeEmoji.takeIf { isPersian } ?: emoji

    private fun resolveTitle(isPersian: Boolean, resources: Resources): String =
        persianAlternativeTitle.takeIf { isPersian } ?: resources.getString(title)

    /*
     * https://en.wikipedia.org/wiki/Chinese_zodiac#Compatibility
     *
     * |   Sign  |      Best Match      |                    Average Match                   | Super Bad | Harmful |
     * |:-------:|:--------------------:|:--------------------------------------------------:|:---------:|---------|
     * | Rat     | Dragon, Monkey, Ox   | Pig, Tiger, Dog, Snake, Rabbit, Rooster, Rat       | Horse     | Goat    |
     * | Ox      | Rooster, Snake, Rat  | Monkey, Dog, Rabbit, Tiger, Dragon, Pig, Ox        | Goat      | Horse   |
     * | Tiger   | Horse, Dog, Pig      | Rabbit, Dragon, Rooster, Rat, Goat, Ox, Tiger      | Monkey    | Snake   |
     * | Rabbit  | Pig, Goat, Dog       | Tiger, Monkey, Rabbit, Ox, Horse, Rat, Snake       | Rooster   | Dragon  |
     * | Dragon  | Rat, Monkey, Rooster | Tiger, Snake, Horse, Goat, Pig, Ox, Dragon         | Dog       | Rabbit  |
     * | Snake   | Ox, Rooster, Monkey  | Horse, Dragon, Goat, Dog, Rabbit, Rat, Snake       | Pig       | Tiger   |
     * | Horse   | Dog, Tiger, Goat     | Snake, Rabbit, Dragon, Rooster, Pig, Monkey, Horse | Rat       | Ox      |
     * | Goat    | Rabbit, Pig, Horse   | Snake, Goat, Dragon, Monkey, Rooster, Dog, Tiger   | Ox        | Rat     |
     * | Monkey  | Dragon, Rat, Snake   | Monkey, Dog, Ox, Goat, Rabbit, Rooster, Horse      | Tiger     | Pig     |
     * | Rooster | Snake, Ox, Dragon    | Horse, Rooster, Goat, Pig, Tiger, Monkey, Rat      | Rabbit    | Dog     |
     * | Dog     | Tiger, Horse, Rabbit | Monkey, Pig, Rat, Ox, Snake, Goat, Dog             | Dragon    | Rooster |
     * | Pig     | Rabbit, Goat, Tiger  | Rat, Rooster, Dog, Dragon, Horse, Ox, Pig          | Snake     | Monkey  |
     */
    infix fun compatibilityWith(other: ChineseZodiac): Compatibility {
        return when (other.ordinal) {
            (ordinal + 4) % 12 -> Compatibility.BEST
            (ordinal + 8) % 12 -> Compatibility.BEST
            (13 - ordinal) % 12 -> Compatibility.BETTER
            (ordinal + 6) % 12 -> Compatibility.WORSE
            (19 - ordinal) % 12 -> Compatibility.WORST
            else -> Compatibility.NEUTRAL
        }
    }

    enum class Compatibility { BEST, BETTER, NEUTRAL, WORSE, WORST }

    companion object {
        fun fromPersianCalendar(persianDate: PersianDate): ChineseZodiac =
            entries.getOrNull((persianDate.year + 5) % 12) ?: RAT

        @RequiresApi(Build.VERSION_CODES.N)
        fun fromChineseCalendar(chineseDate: ChineseCalendar): ChineseZodiac =
            entries.getOrNull((chineseDate[ChineseCalendar.YEAR] - 1) % 12) ?: RAT
    }
}
