// The data and ideas in the following file is released under public domain/CC0
// They are currently unused on the project but can be used later for ancient events
// There are different variants of calendars used by Zoroaster people but this one is the specific
// one used by Iranian, called "تقویم فصلی", the variant used by Parsi people in India is more of
// less the same but doesn't have leap day thus has become out of sync with the Iranian variant
// The information here is provided by Roozbeh Pournader
package com.byagowi.persiancalendar.utils

import androidx.annotation.VisibleForTesting
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import io.github.persiancalendar.calendar.PersianDate

private val ancientPersianNames = listOf(
    "اورمزد", "وهمن", "اردیبهشت", "شهریور", "سپندارمزد", "خورداد", "امرداد", "دی‌بآذر",
    "آذر", "آبان", "خور (خیر)", "ماه", "تیر", "گوش", "دی‌بمهر", "مهر",
    "سروش", "رشن", "فروردین", "ورهرام", "رام", "باد", "دی‌بدین",
    "دین", "ارد", "اشتاد", "آسمان", "زامیاد", "مانتره‌سپند", "انارام"
)

private const val leapYearDayName = "اورداد"
private val lastDayOfYearNames =
    listOf("اهنود", "اشتود", "سپنتمد", "وهوخشتر", "وهوشتواش", leapYearDayName)

private val persianMonthNames = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

// dayOfYear is a zero indexed number
@VisibleForTesting
fun ancientDayName(dayOfYear: Int): String {
    val dayOfMonth = (dayOfYear - 1) % 30
    val month = (dayOfYear - 1) / 30
    return ((if (month == 12) lastDayOfYearNames else ancientPersianNames)
        .getOrNull(dayOfMonth).debugAssertNotNull ?: "") + " و " +
            (persianMonthNames.getOrNull(if (month == 12) 11 else month).debugAssertNotNull ?: "") +
            " ماه"
}

private val daysToMonth = listOf(0, 31, 62, 93, 124, 155, 186, 216, 246, 276, 306, 336, 366)
fun ancientDayNameFromModernDayMonth(day: Int, month: Int): String {
    val dayOfYear = (daysToMonth.getOrNull(month - 1).debugAssertNotNull ?: 0) + day
    return ancientDayName(dayOfYear)
}

val PersianDate.ancientName get() = ancientDayNameFromModernDayMonth(dayOfMonth, month)
