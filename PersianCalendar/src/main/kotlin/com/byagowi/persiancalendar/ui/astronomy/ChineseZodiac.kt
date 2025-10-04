package com.byagowi.persiancalendar.ui.astronomy

import android.content.res.Resources
import android.icu.util.ChineseCalendar
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.FixedElement.EARTH
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.FixedElement.FIRE
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.FixedElement.METAL
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.FixedElement.WATER
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.FixedElement.WOOD
import io.github.persiancalendar.calendar.PersianDate

/**
 * موش و بقر و پلنگ و خرگوش شمار - زان چار چو بگذری نهنگ آید و مار
 *آنگاه به اسب و گوسفند است حساب - حمدونه و مرغ و سگ و خوک آخر کار
 *
 * From https://fa.wikipedia.org/wiki/گاه‌شماری_حیوانی
 *
 * See also: https://en.wikipedia.org/wiki/Chinese_zodiac#Signs
 */
enum class ChineseZodiac(
    @get:StringRes private val title: Int,
    private val emoji: String,
    private val arabicNameToUseInPersian: String,
    private val oldEraPersianName: String, // e.g. used in https://rc.majlis.ir/fa/law/show/91137
    private val persianSpecificEmoji: String? = null,
    private val persianSpecificTitle: String? = null,
) {
    RAT(R.string.animal_year_name_rat, "🐀", "فاره", "سیچقان ئیل"),
    OX(R.string.animal_year_name_ox, "🐂", "بقر", "اود ئیل"),
    TIGER(R.string.animal_year_name_tiger, "🐅", "نمر", "بارس ئیل", "🐆", "پلنگ"),
    RABBIT(R.string.animal_year_name_rabbit, "🐇", "ارنب", "تَوِشقان ئیل"),
    DRAGON(R.string.animal_year_name_dragon, "🐲", "تمساح\n(ثعبان)", "لوی ئیل", "🐊", "نهنگ"),
    SNAKE(R.string.animal_year_name_snake, "🐍", "حیه", "ئیلان ئیل"),
    HORSE(R.string.animal_year_name_horse, "🐎", "فرس", "یونت ئیل"),
    GOAT(R.string.animal_year_name_goat, "🐐", "غنم", "قُوی ئیل", "🐑", "گوسفند"),
    MONKEY(R.string.animal_year_name_monkey, "🐒", "حمدونه\n(قرده)", "پیچی ئیل"),
    ROOSTER(R.string.animal_year_name_rooster, "🐓", "دجاجه"/*?داقوی*/, "تُخاقوی ئیل", "🐔", "مرغ"),
    DOG(R.string.animal_year_name_dog, "🐕", "کلب", "ایت ئیل"),
    PIG(R.string.animal_year_name_pig, "🐖", "خنزیر", "تُنگوز ئیل");

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
        val resolvedName = resolveTitle(isPersian, resources)
        if (isPersian) {
            appendLine(if (this@ChineseZodiac == MONKEY) "$resolvedName (شادی)" else resolvedName)
            appendLine(oldEraPersianName)
            append(arabicNameToUseInPersian)
        } else append(resolvedName)
    }

    fun resolveEmoji(isPersian: Boolean): String =
        persianSpecificEmoji.takeIf { isPersian } ?: emoji

    private fun resolveTitle(isPersian: Boolean, resources: Resources): String =
        persianSpecificTitle.takeIf { isPersian } ?: resources.getString(title)

    // https://en.wikipedia.org/wiki/Chinese_zodiac#Compatibility
    infix fun compatibilityWith(other: ChineseZodiac): Compatibility {
        return when ((ordinal - other.ordinal + 12) % 12) {
            4, 8 -> Compatibility.BEST
            6 -> Compatibility.WORSE
            else -> when ((ordinal + other.ordinal) % 12) {
                1 -> Compatibility.BETTER
                7 -> Compatibility.WORST
                else -> Compatibility.NEUTRAL
            }
        }
    }

    enum class Compatibility { BEST, BETTER, NEUTRAL, WORSE, WORST }

    // https://en.wikipedia.org/wiki/Chinese_zodiac#Signs
    val yinYang: YinYang get() = YinYang.entries[(ordinal + 1) % 2]

    enum class YinYang { YIN, YANG }

    val fixedElement: FixedElement get() = zodiacToElement[ordinal]

    // https://en.wikipedia.org/wiki/Wuxing_(Chinese_philosophy)
    enum class FixedElement { FIRE, WATER, WOOD, METAL, EARTH }

    val trin get() = (ordinal % 4) + 1

    companion object {
        private val zodiacToElement = listOf(
            WATER, EARTH, WOOD, WOOD, EARTH, FIRE, FIRE, EARTH, METAL, METAL, EARTH, WATER
        )

        fun fromPersianCalendar(persianDate: PersianDate): ChineseZodiac =
            entries[(persianDate.year + 5).mod(12)]

        @RequiresApi(Build.VERSION_CODES.N)
        fun fromChineseCalendar(chineseDate: ChineseCalendar): ChineseZodiac =
            entries[(chineseDate[ChineseCalendar.YEAR] - 1) % 12]
    }
}
