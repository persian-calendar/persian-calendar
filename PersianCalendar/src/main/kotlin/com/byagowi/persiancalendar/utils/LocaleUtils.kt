package com.byagowi.persiancalendar.utils

import android.content.Context
import android.content.res.Resources
import android.view.View
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.CityItem
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.otherCalendars
import com.byagowi.persiancalendar.global.preferredDigits
import java.util.*

// Context preferably should be activity context not application
fun applyAppLanguage(context: Context) {
    val locale = language.asSystemLocale()
    Locale.setDefault(locale)
    val resources = context.resources
    val config = resources.configuration
    config.setLocale(locale)
    config.setLayoutDirection(if (language.isLessKnownRtl) Language.FA.asSystemLocale() else locale)
    resources.updateConfiguration(config, resources.displayMetrics)
}

//fun Context.withLocale(): Context {
//    val config = resources.configuration
//    val locale = language.asSystemLocale()
//    Locale.setDefault(locale)
//    config.setLocale(locale)
//    config.setLayoutDirection(if (language.isLessKnownRtl) Language.fa.asSystemLocale() else locale)
//    return createConfigurationContext(config)
//}

val Resources.isRtl get() = configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

fun formatNumber(number: Double): String {
    if (isArabicDigitSelected) return number.toString()
    return formatNumber(number.toString()).replace(".", "٫") // U+066B, Arabic Decimal Separator
}

fun formatNumber(number: Int, digits: CharArray = preferredDigits): String =
    formatNumber(number.toString(), digits)

fun formatNumber(number: String, digits: CharArray = preferredDigits): String {
    if (isArabicDigitSelected) return number
    return number.map { digits.getOrNull(Character.getNumericValue(it)) ?: it }
        .joinToString("")
}

val isArabicDigitSelected: Boolean get() = preferredDigits === Language.ARABIC_DIGITS

fun getEnabledCalendarTypes() = listOf(mainCalendar) + otherCalendars

val Collection<CityItem>.sortCityNames: List<CityItem>
    get() = this.map { city ->
        city to language.getCityName(city).let { language.prepareForSort(it) }
    }.sortedWith { (leftCity, leftSortName), (rightCity, rightSortName) ->
        language.countriesOrder.indexOf(leftCity.countryCode).compareTo(
            language.countriesOrder.indexOf(rightCity.countryCode)
        ).takeIf { it != 0 } ?: leftSortName.compareTo(rightSortName)
    }.map { (city, _) -> city }

fun <T> listOf31Items(
    x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T,
    x13: T, x14: T, x15: T, x16: T, x17: T, x18: T, x19: T, x20: T, x21: T, x22: T,
    x23: T, x24: T, x25: T, x26: T, x27: T, x28: T, x29: T, x30: T, x31: T
) = listOf(
    x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12,
    x13, x14, x15, x16, x17, x18, x19, x20, x21, x22,
    x23, x24, x25, x26, x27, x28, x29, x30, x31
)

fun <T> listOf12Items(
    x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T
) = listOf(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12)

fun <T> listOf7Items(
    x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T
) = listOf(x1, x2, x3, x4, x5, x6, x7)
