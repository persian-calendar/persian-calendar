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

// the returned value is a zero indexed number
@VisibleForTesting
fun persianDayOfYear(persianDate: PersianDate, jdn: Jdn): Int {
    return jdn - Jdn(PersianDate(persianDate.year, 1, 1)) + 1
}

// region Fasli
private val fasliDaysNames = listOf(
    "اورمزد", "وهمن", "اردیبهشت", "شهریور", "سپندارمزد", "خورداد", "امرداد", "دی‌بآذر",
    "آذر", "آبان", "خور (خیر)", "ماه", "تیر", "گوش", "دی‌بمهر", "مهر",
    "سروش", "رشن", "فروردین", "ورهرام", "رام", "باد", "دی‌بدین",
    "دین", "ارد", "اشتاد", "آسمان", "زامیاد", "مانتره‌سپند", "انارام"
)

private const val leapYearDayName = "اورداد"
private val lastDayOfYearNames =
    listOf("اهنود", "اشتود", "سپنتمد", "وهوخشتر", "وهوشتواش", leapYearDayName)

private val fasliMonthNames = listOf(
    "فروردین", "اردیبهشت", "خورداد", "تیر", "امرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

@VisibleForTesting
fun fasliDayName(dayOfYear: Int): String {
    val dayOfMonth = (dayOfYear - 1) % 30
    val month = (dayOfYear - 1) / 30
    return ((if (month == 12) lastDayOfYearNames else fasliDaysNames)[dayOfMonth]) +
            if (month == 12) "" else " و ${fasliMonthNames[month]} ماه"
}
// endregion

// region Jalali
private val jalaliMonthNames = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

@VisibleForTesting
fun jalaliName(persianYear: Int?, dayOfYear: Int): String {
    val dayOfMonth = (dayOfYear - 1) % 30
    return when (val month = (dayOfYear - 1) / 30) {
        12 -> "روز " + numeral.value.format(dayOfMonth + 1) + " خمسهٔ جلالی"
        else -> numeral.value.format(dayOfMonth + 1) + " " + jalaliMonthNames[month] + " جلالی"
    } + (if (persianYear != null) " " + numeral.value.format(persianYear - 457) else "")
}
// endregion

// Looks better with Persian as Roozbeh
val persianDelimiter = " ــــ "

fun jalaliAndHistoricalName(persianDate: PersianDate, jdn: Jdn): String {
    val dayOfYear = persianDayOfYear(persianDate, jdn)
    return jalaliName(persianDate.year, dayOfYear) + persianDelimiter + fasliDayName(dayOfYear)
}

fun jalaliDayOfYear(date: PersianDate): String {
    val dayOfYear = modernPersianMonthDays[date.month - 1] + date.dayOfMonth
    return jalaliName(null, dayOfYear)
}

private val modernPersianMonthDays =
    listOf(0, 31, 62, 93, 124, 155, 186, 216, 246, 276, 306, 336, 366)
