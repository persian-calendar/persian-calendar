// The data and ideas in the following file is released under public domain/CC0
// There are different variants of calendars used by Zoroaster people but this one is the specific
// one used by Iranian, called "تقویم فصلی", the variant used by Parsi people in India is more of
// less the same but doesn't have leap day thus has become out of sync with the Iranian variant
// The information here is provided by Roozbeh Pournader
package com.byagowi.persiancalendar.utils

import androidx.annotation.VisibleForTesting
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.numeral
import io.github.persiancalendar.calendar.PersianDate

class HistoricalPersianDate(persianDate: PersianDate) {
    private val year = persianDate.year
    private val dayOfYear = Jdn(persianDate) - Jdn(PersianDate(year, 1, 1))
    private val dayOfMonth = dayOfYear % 30
    private val month = dayOfYear / 30

    // Nabor Days روزهای نَبُر - پرهیز از کشتن حیوانات سودمند
    // وهمن، ماه، گوش، رام
    val isAbstinenceDays
        get() = month < 12 && when (dayOfMonth) {
            1, 11, 13, 20 -> true
            else -> false
        }

    // Rest / Prayer Days روزهای نیایش همگانی و استراحت
    // اورمزد، دی‌بآذر، دی‌بمهر، دی‌بدین
    val isRestDays
        get() = month < 12 && when (dayOfMonth) {
            0, 7, 14, 22 -> true
            else -> false
        }

    val extraTitle: String?
        get() = when {
            isAbstinenceDays -> "نَبُر"
            isRestDays -> "𝄞"
            else -> null
        }

    @VisibleForTesting
    val zoroastrianismYear get() = PersianDateEpoch.Zoroastrianism.format(year)
    val fasliDayName: String
        get() {
            return when (month) {
                12 -> lastDayOfYearNames
                else -> fasliDaysNames
            }[dayOfMonth] + when (month) {
                12 -> ""
                else -> " و ${fasliMonthNames[month]} ماه"
            }
        }
    val jalaliName get() = jalaliDayOfYearName + " " + PersianDateEpoch.Jalali.format(year)
    val jalaliDayOfYearName
        get() = when (month) {
            12 -> "روز " + numeral.format(dayOfMonth + 1) + " خمسهٔ"
            else -> numeral.format(dayOfMonth + 1) + " " + jalaliMonthNames[month]
        }

    companion object {

        // region Fasli
        private val fasliDaysNames = listOf(
            "اورمزد", "وهمن", "اردیبهشت", "شهریور", "سپندارمزد", "خورداد", "امرداد", "دی‌بآذر",
            "آذر", "آبان", "خور (خیر)", "ماه", "تیر", "گوش", "دی‌بمهر", "مهر",
            "سروش", "رشن", "فروردین", "ورهرام", "رام", "باد", "دی‌بدین",
            "دین", "ارد", "اشتاد", "آسمان", "زامیاد", "مانتره‌سپند", "انارام",
        )

        private const val leapYearDayName = "اورداد"
        private val lastDayOfYearNames =
            listOf("اهنود", "اشتود", "سپنتمد", "وهوخشتر", "وهوشتواش", leapYearDayName)

        private val fasliMonthNames = listOf(
            "فروردین", "اردیبهشت", "خورداد", "تیر", "امرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
        )

        private val fasliAlternativeMonthNames = listOf(
            "فَرَوَشی", "اشاوهیشتا", "هه‌اورتات", "تِشترَیا", "اَمرتات", "خشتره‌ویریه",
            "میترا", "آبان", "آترا", "دتوشو", "وهومن", "سپنتاآرمیتی",
        )
        // endregion

        // region Jalali
        private val jalaliMonthNames = listOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
        )
        // endregion
    }
}
