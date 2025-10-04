package com.byagowi.persiancalendar.utils

import androidx.annotation.VisibleForTesting
import com.byagowi.persiancalendar.entities.Jdn
import io.github.persiancalendar.calendar.util.julianFromJdn

/**
 * Seleucid (Roman/Assyrian) and Yazdegerd date helpers.
 *
 * This file provides safer, testable helpers that return structured data
 * (instead of only formatted strings) and a set of convenience formatters.
 */

// region Seleucid
// The month names come from https://w.wiki/ENrQ ماه‌های آسوری
// ابونصر فراهی در کتاب نصاب الصبیان اسامی ماه‌های رومی:
//  دو تشرین و دو کانون و پس آنگه - شباط و آذر و نیسان، ایار است
//  حزیران و تموز و آب و ایلول - نگه دارش که از من یادگار است
private val seleucidMonths = listOf(
    "تشرین اول", "تشرین آخر", "کانون اول", "کانون آخر", "شباط", "آذر",
    "نیسان", "ایار", "حزیران", "تموز", "آب", "ایلول",
)

/** Structured representation of a Seleucid date. */
data class SeleucidDate(val year: Int, val monthIndex: Int, val monthName: String, val day: Int)

/**
 * Convert JDN to a structured Seleucid date.
 * Uses julianFromJdn(...) and applies the same epoch adjustments as the previous
 * implementation but does so in a clearer, testable way.
 */
fun toSeleucidDate(jdn: Jdn): SeleucidDate {
    val (julianYear, julianMonth, dayOfMonth) = julianFromJdn(jdn.value)
    // julianMonth is 1-based (1..12). We convert to 0-based, apply the +2 shift used
    // historically and then map back to a month name index.
    val monthIndex = ((julianMonth - 1) + 2) % 12
    val monthName = seleucidMonths[monthIndex]
    // The published formula used +311 and increments year when julianMonth > 9 (i.e. Oct..Dec)
    val year = julianYear + 311 + if (julianMonth > 9) 1 else 0
    return SeleucidDate(year = year, monthIndex = monthIndex, monthName = monthName, day = dayOfMonth)
}

@VisibleForTesting
fun formatAsSeleucidDate(jdn: Jdn): String {
    val sd = toSeleucidDate(jdn)
    return "${formatNumber(sd.day)} ${sd.monthName} ${formatNumber(sd.year)}"
}

// Additional: Get Seleucid year only
fun getSeleucidYear(jdn: Jdn): Int = toSeleucidDate(jdn).year

// Additional: Get Seleucid month name only
fun getSeleucidMonthName(jdn: Jdn): String = toSeleucidDate(jdn).monthName

// Additional: Get Seleucid day only
fun getSeleucidDay(jdn: Jdn): Int = toSeleucidDate(jdn).day
// endregion

// region Yazdegerd
// It's only one variant of Yazdegerd
// For the other variant have a look at the source:
// https://www.aoi.uzh.ch/de/islamwissenschaft/studium/tools/kalenderumrechnung/yazdigird.html
// It's called فرس قدیم or فرسی یزدگردی by the different sources
// Doesn't match what indicated on traditional calendars thus only year part is shown with tilda
// to indicate inaccuracy.

/** Structured representation of a Yazdegerd date (approximate). */
data class YazdegerdDate(val year: Int, val dayOfYear: Int)

/** Convert JDN to YazdegerdDate (approximate variant used by the project).
 * Note: this conversion is approximate and uses an epoch offset; the original
 * project shows only the year with a tilde to indicate inaccuracy.
 */
fun toYazdegerdDate(jdn: Jdn): YazdegerdDate {
    val daysSinceEpoch = (jdn.value - 1952063).toInt() // offset used in original code
    val year = daysSinceEpoch / 365 + 1
    val dayOfYear = (daysSinceEpoch % 365) + 1
    return YazdegerdDate(year = year, dayOfYear = dayOfYear)
}

@VisibleForTesting
fun formatAsYazdegerdDate(jdn: Jdn): String {
    val yd = toYazdegerdDate(jdn)
    return formatNumber(yd.year) + "~ یزدگردی"
}

// Additional: Get Yazdegerd year only
fun getYazdegerdYear(jdn: Jdn): Int = toYazdegerdDate(jdn).year

// Additional: Get Yazdegerd day offset
fun getYazdegerdDayOfYear(jdn: Jdn): Int = toYazdegerdDate(jdn).dayOfYear
// endregion

fun formatAsSeleucidAndYazdegerdDate(jdn: Jdn): String =
    formatAsSeleucidDate(jdn) + " رومی" + persianDelimiter + formatAsYazdegerdDate(jdn)

// Additional: Combined year info
fun formatCombinedYearInfo(jdn: Jdn): String {
    val seleucidYear = getSeleucidYear(jdn)
    val yazdegerdYear = getYazdegerdYear(jdn)
    return "Seleucid Year: ${formatNumber(seleucidYear)} | Yazdegerd Year: ${formatNumber(yazdegerdYear)}"
}

// Additional: Detailed breakdown of both calendars
fun formatDetailedDualCalendar(jdn: Jdn): String {
    val sd = toSeleucidDate(jdn)
    val yd = toYazdegerdDate(jdn)
    return "Seleucid: ${formatNumber(sd.day)} ${sd.monthName} ${formatNumber(sd.year)} | " +
           "Yazdegerd: Year ${formatNumber(yd.year)}, Day ${formatNumber(yd.dayOfYear)}"
} 
