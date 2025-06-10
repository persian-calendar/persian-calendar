package com.byagowi.persiancalendar.utils

import com.byagowi.persiancalendar.entities.Jdn
import io.github.persiancalendar.calendar.util.julianFromJdn

// The month names come from https://w.wiki/ENrQ ماه‌های آسوری
// ابونصر فراهی در کتاب نصاب الصبیان اسامی ماه‌های رومی:
//  دو تشرین و دو کانون و پس آنگه - شباط و آذر و نیسان، ایار است
//  حزیران و تموز و آب و ایلول - نگه دارش که از من یادگار است
private val romanMonths = listOf(
    "تشرین اول", "تشرین آخر", "کانون اول", "کانون آخر", "شباط", "آذر",
    "نیسان", "ایار", "حزیران", "تموز", "آب", "ایلول",
)

// It's called رومی or اسکندری رومی by the different sources
fun formatAsRomanDate(jdn: Jdn): String {
    val (julianYear, julianMonth, dayOfMonth) = julianFromJdn(jdn.value)
    val year = julianYear + 311 + if (julianMonth > 9) 1 else 0
    val month = romanMonths[(julianMonth + 2) % 12]
    return "${formatNumber(dayOfMonth)} $month ${formatNumber(year)}"
}
