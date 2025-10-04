// The data and ideas in the following file is released under public domain/CC0
// There are different variants of calendars used by Zoroaster people but this one is the specific
// one used by Iranian, called "تقویم فصلی", the variant used by Parsi people in India is more or
// less the same but doesn't have leap day thus has become out of sync with the Iranian variant
// The information here is provided by Roozbeh Pournader
package com.byagowi.persiancalendar.utils

import androidx.annotation.VisibleForTesting
import com.byagowi.persiancalendar.entities.Jdn
import io.github.persiancalendar.calendar.PersianDate

// Note: functions in this file return 1-based day-of-year values (1..365/366)
@VisibleForTesting
fun persianDayOfYear(persianDate: PersianDate, jdn: Jdn): Int {
    return (jdn - Jdn(PersianDate(persianDate.year, 1, 1)) + 1).coerceAtLeast(1)
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
    val safeDay = dayOfYear.coerceAtLeast(1)
    val dayOfMonth = (safeDay - 1) % 30
    val month = (safeDay - 1) / 30

    return if (month == 12) {
        val idx = dayOfMonth.coerceIn(0, lastDayOfYearNames.lastIndex)
        lastDayOfYearNames[idx]
    } else {
        val idx = dayOfMonth.coerceIn(0, fasliDaysNames.lastIndex)
        val mIdx = month.coerceIn(0, fasliMonthNames.lastIndex)
        "${fasliDaysNames[idx]} و ${fasliMonthNames[mIdx]} ماه"
    }
}

fun getFasliMonthName(dayOfYear: Int): String {
    val safeDay = dayOfYear.coerceAtLeast(1)
    val month = (safeDay - 1) / 30
    return if (month == 12) "خمسه" else fasliMonthNames[month.coerceIn(0, fasliMonthNames.lastIndex)]
}

// endregion

// region Jalali
private val jalaliMonthNames = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

@VisibleForTesting
fun jalaliName(persianDate: PersianDate, dayOfYear: Int): String {
    val safeDay = dayOfYear.coerceAtLeast(1)
    val dayOfMonth = (safeDay - 1) % 30
    val month = ((safeDay - 1) / 30).coerceAtMost(12)

    val namePart = when (month) {
        12 -> "روز ${formatNumber(dayOfMonth + 1)} خمسهٔ جلالی"
        else -> "${formatNumber(dayOfMonth + 1)} ${jalaliMonthNames[month]} جلالی"
    }
    return "$namePart ${formatNumber(persianDate.year - 457)}"
}

fun getJalaliMonthName(dayOfYear: Int): String {
    val safeDay = dayOfYear.coerceAtLeast(1)
    val month = ((safeDay - 1) / 30).coerceAtMost(12)
    return if (month == 12) "خمسهٔ جلالی" else jalaliMonthNames[month]
}
// endregion

val persianDelimiter = " ــــ "

fun jalaliAndHistoricalName(persianDate: PersianDate, jdn: Jdn): String {
    val dayOfYear = persianDayOfYear(persianDate, jdn)
    return jalaliName(persianDate, dayOfYear) + persianDelimiter + fasliDayName(dayOfYear)
}

fun jalaliAndHistoricalCardData(persianDate: PersianDate, jdn: Jdn): Pair<String, String> {
    val dayOfYear = persianDayOfYear(persianDate, jdn)
    val title = jalaliName(persianDate, dayOfYear)
    val subtitle = fasliDayName(dayOfYear)
    return title to subtitle
}

fun formatCombinedMonthInfo(dayOfYear: Int, persianDate: PersianDate): String {
    val fasliMonth = getFasliMonthName(dayOfYear)
    val jalaliMonth = getJalaliMonthName(dayOfYear)
    return "Fasli Month: $fasliMonth | Jalali Month: $jalaliMonth (${formatNumber(persianDate.year - 457)})"
}

// Additional utility: check if given dayOfYear is leap epagomenal day
fun isLeapEpagomenalDay(dayOfYear: Int): Boolean {
    val safeDay = dayOfYear.coerceAtLeast(1)
    val month = (safeDay - 1) / 30
    val dayOfMonth = (safeDay - 1) % 30
    return month == 12 && dayOfMonth == 5
}

// Additional utility: return both Jalali and Fasli month names in a pair
fun getMonthNamesPair(dayOfYear: Int): Pair<String, String> {
    return getJalaliMonthName(dayOfYear) to getFasliMonthName(dayOfYear)
}

// Additional utility: return full description for given day
fun fullDayDescription(persianDate: PersianDate, jdn: Jdn): String {
    val dayOfYear = persianDayOfYear(persianDate, jdn)
    return "${jalaliName(persianDate, dayOfYear)}$persianDelimiter${fasliDayName(dayOfYear)}"
}
 
