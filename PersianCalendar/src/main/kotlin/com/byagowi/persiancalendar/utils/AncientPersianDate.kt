// The data and ideas in the following file is released under public domain/CC0
// They are currently unused on the project but can be used later for ancient events
// There are different variants of calendars used by Zoroaster people but this one is the specific
// one used by Iranian, called "تقویم فصلی", the variant used by Parsi people in India is more of
// less the same but doesn't have leap day thus has become out of sync with the Iranian variant
// The information here is provided by Roozbeh Pournader
package com.byagowi.persiancalendar.utils

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

val PersianDate.ancientDayName: String
    get() {
        val dayOfYear = toJdn() - PersianDate(year, 1, 1).toJdn()
        val dayOfMonth = (dayOfYear % 30).toInt()
        val month = (dayOfYear / 30).toInt()
        return if (month == 12) lastDayOfYearNames[dayOfMonth] else ancientPersianNames[dayOfMonth]
    }
