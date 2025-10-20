package com.byagowi.persiancalendar.utils

import androidx.annotation.VisibleForTesting
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.numeral
import io.github.persiancalendar.calendar.util.julianFromJdn

// region Seleucid
// The month names come from https://w.wiki/ENrQ ماه‌های آسوری
// ابونصر فراهی در کتاب نصاب الصبیان اسامی ماه‌های رومی:
//  دو تشرین و دو کانون و پس آنگه - شباط و آذر و نیسان، ایار است
//  حزیران و تموز و آب و ایلول - نگه دارش که از من یادگار است
private val seleucidMonths = listOf(
    "تشرین اول", "تشرین آخر", "کانون اول", "کانون آخر", "شباط", "آذر",
    "نیسان", "ایار", "حزیران", "تموز", "آب", "ایلول",
)

// It's called رومی or اسکندری رومی by the different sources
@VisibleForTesting
fun formatAsSeleucidDate(jdn: Jdn): String {
    val (julianYear, julianMonth, dayOfMonth) = julianFromJdn(jdn.value)
    // This 311 matches https://en.wikipedia.org/wiki/Seleucid_era
    // while one other source uses 321
    val year = julianYear + 311 + if (julianMonth > 9) 1 else 0
    val month = seleucidMonths[(julianMonth + 2) % 12]
    return "${numeral.value.format(dayOfMonth)} $month ${numeral.value.format(year)}"
}
// endregion

// region Yazdegerd
// It's only one variant of Yazdegerd
// For the other variant have a look at the source:
// https://www.aoi.uzh.ch/de/islamwissenschaft/studium/tools/kalenderumrechnung/yazdigird.html
// It's called فرس قدیم or فرسی یزدگردی by the different sources
// Doesn't match what indicated on traditional calendars thus only year part is shown with tilda
// to indicate inaccuracy.
@VisibleForTesting
fun formatAsYazdegerdDate(jdn: Jdn): String {
    // It needs one day offset to match with traditionally published calendars
    val daysSinceEpoch = (jdn.value - 1952063).toInt()
    return numeral.value.format(daysSinceEpoch / 365 + 1) + "~ یزدگردی"
}
// endregion

fun formatAsSeleucidAndYazdegerdDate(jdn: Jdn): String =
    formatAsSeleucidDate(jdn) + " رومی" + persianDelimiter + formatAsYazdegerdDate(jdn)
