package com.byagowi.persiancalendar.utils

import androidx.annotation.VisibleForTesting
import com.byagowi.persiancalendar.entities.Jdn
import io.github.persiancalendar.calendar.util.julianFromJdn

// region Roman
// The month names come from https://w.wiki/ENrQ ماه‌های آسوری
// ابونصر فراهی در کتاب نصاب الصبیان اسامی ماه‌های رومی:
//  دو تشرین و دو کانون و پس آنگه - شباط و آذر و نیسان، ایار است
//  حزیران و تموز و آب و ایلول - نگه دارش که از من یادگار است
private val romanMonths = listOf(
    "تشرین اول", "تشرین آخر", "کانون اول", "کانون آخر", "شباط", "آذر",
    "نیسان", "ایار", "حزیران", "تموز", "آب", "ایلول",
)

@VisibleForTesting
fun formatAsRomanDate(jdn: Jdn): String {
    val (julianYear, julianMonth, dayOfMonth) = julianFromJdn(jdn.value)
    val year = julianYear + 311 + if (julianMonth > 9) 1 else 0
    val month = romanMonths[(julianMonth + 2) % 12]
    return "${formatNumber(dayOfMonth)} $month ${formatNumber(year)}"
}
// endregion

// region Yazdigird
// Same months as current Persian calendar it seems
private val yazdigirdMonths = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

// It's one variant of Yazdigird
// for the other variant have a look at the source:
// https://www.aoi.uzh.ch/de/islamwissenschaft/studium/tools/kalenderumrechnung/yazdigird.html
@VisibleForTesting
fun formatAsYazdigird(jdn: Jdn): String {
    // It needs one day offset to match with traditionally published calendars
    val daysDifference = ((jdn.value + 1) - 1952063).toInt()
    val year = (daysDifference / 365) + 1
    val yearDifference = daysDifference - (year - 1) * 365
    val month = ((yearDifference - (yearDifference / 360) * 5) / 30) + 1
    val day = yearDifference - (month - 1) * 30 - (yearDifference / (360 + 5)) * 5 + 1
    return "${formatNumber(day)} ${yazdigirdMonths[month - 1]} ${formatNumber(year)}"
}
// endregion

fun romanAndYazdigirdName(jdn: Jdn): String =
    formatAsRomanDate(jdn) + " رومی" // + " ــــ " + formatAsYazdigird(jdn) + " یزدگردی"
