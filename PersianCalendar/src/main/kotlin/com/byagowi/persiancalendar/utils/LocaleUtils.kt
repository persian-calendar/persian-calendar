package com.byagowi.persiancalendar.utils

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.entities.CityItem
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.preferredDigits
import java.util.Locale

/**
 * Language & localization helpers.
 *
 * This file centralizes safe locale application, number formatting and a set of
 * lightweight helpers used by UI code. The functions are defensive and avoid
 * throwing when given unexpected input.
 */

/** Apply the selected app language to the application's configuration. */
fun applyAppLanguage(context: Context) {
    try {
        val locale = language.value.asSystemLocale()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService<LocaleManager>()?.applicationLocales = LocaleList(locale)
        } else {
            Locale.setDefault(locale)
            val resources = context.resources
            val config = applyLanguageToConfiguration(resources.configuration, locale)
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    } catch (t: Throwable) {
        // Log and continue; failing to apply language should not crash the app
        runCatching { logException(t) }
    }
}

/** Apply language to a Configuration object for pre-Android 13 devices. */
fun applyLanguageToConfiguration(
    config: Configuration,
    locale: Locale = language.value.asSystemLocale()
): Configuration {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return config
    config.setLocale(locale)
    // setLayoutDirection expects a locale: when language.value.isLessKnownRtl is true we force Farsi locale
    config.setLayoutDirection(if (language.value.isLessKnownRtl) Language.FA.asSystemLocale() else locale)
    return config
}

/**
 * Format a floating point number using the currently selected digit set.
 * Uses Arabic decimal separator for non-Latin digits.
 */
fun formatNumber(number: Double): String {
    // For Arabic digits we keep Java's default conversion which uses western digits in many locales,
    // but the project historically used this branch — keep consistent behaviour.
    if (isArabicDigitSelected) return number.toString()
    return formatNumber(number.toString()).replace(".", "٫") // U+066B
}

/** Format an integer according to the preferred digits. */
fun formatNumber(number: Int, digits: CharArray = preferredDigits): String = formatNumber(number.toString(), digits)

/**
 * Convert a number string to the selected digit set (default uses global preferredDigits).
 * The function is tolerant to non-numeric characters and preserves them.
 */
fun formatNumber(number: String, digits: CharArray = preferredDigits): String {
    if (isArabicDigitSelected) return number
    // Special-case Tamil 'symbols' for common round numbers used previously in the project
    if (digits === Language.TAMIL_DIGITS) when (number) {
        "10" -> return "௰"
        "100" -> return "௱"
        "1000" -> return "௲"
        else -> Unit
    }
    return number.map { ch ->
        val idx = Character.getNumericValue(ch)
        if (idx in 0..9) digits.getOrNull(idx) ?: ch else ch
    }.joinToString("")
}

/** Quick checks for digit sets. */
val isArabicDigitSelected: Boolean get() = preferredDigits === Language.ARABIC_DIGITS
val isTamilDigitSelected: Boolean get() = preferredDigits === Language.TAMIL_DIGITS

/** Sort a collection of CityItem using current language's sort rules and country order. */
val Collection<CityItem>.sortCityNames: List<CityItem>
    get() = this.map { city ->
        city to language.value.getCityName(city).let { language.value.prepareForSort(it) }
    }.sortedWith { (leftCity, leftSortName), (rightCity, rightSortName) ->
        (language.value.countriesOrder.indexOf(leftCity.countryCode) compareTo
                language.value.countriesOrder.indexOf(rightCity.countryCode)
                ).takeIf { it != 0 } ?: (leftSortName compareTo rightSortName)
    }.map { (city, _) -> city }

/** Small helpers to create lists of fixed size (keeps callers compact). */
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

// --- Additional UI-friendly helpers ---

/** Format a percentage string using current digits. */
fun formatPercentage(value: Double, digits: CharArray = preferredDigits): String {
    val formatted = formatNumber(value, digits)
    return "$formatted%"
}

/** Format currency with symbol (defaults to Iranian Rial sign). */
fun formatCurrency(amount: Double, currencySymbol: String = "﷼"): String {
    return "${formatNumber(amount)} $currencySymbol"
}

/** Whether current app language is considered RTL by the project. */
fun isCurrentLanguageRtl(): Boolean = language.value.isLessKnownRtl

/** Localized month names (1..12). */
fun getLocalizedMonthNames(): List<String> = (1..12).map { month -> language.value.getMonthName(month) }

/** Localized weekday names (1..7). */
fun getLocalizedWeekdayNames(): List<String> = (1..7).map { weekday -> language.value.getWeekdayName(weekday) }

/** Switch the app language (updates global state and applies configuration). */
fun switchAppLanguage(context: Context, newLanguage: Language) {
    language.value = newLanguage
    applyAppLanguage(context)
}

// --- Convenience utilities for digits and city lists ---

/** Return localized digits 0..9 as strings. */
fun getLocalizedDigits(): List<String> = (0..9).map { digit -> formatNumber(digit) }

/** Normalize a localized number string back to Latin digits where possible. */
fun normalizeToLatinDigits(number: String): String = number.map { ch ->
    val v = Character.getNumericValue(ch)
    if (v in 0..9) v.toString() else ch.toString()
}.joinToString("")

/** Heuristic: whether a locale typically uses Eastern Arabic numerals. */
fun isEasternArabicLocale(locale: Locale = language.value.asSystemLocale()): Boolean {
    return locale.language in listOf("ar", "fa", "ur")
}

/** Return localized city names (unsorted) — helpful for quick UI previews. */
fun getLocalizedCityNames(cities: Collection<CityItem>): List<String> = cities.map { language.value.getCityName(it) }

/** Detect if a string contains localized (non-western) digits. */
fun containsLocalizedDigits(text: String): Boolean = text.any { ch ->
    val v = Character.getNumericValue(ch)
    v in 0..9 && ch !in '0'..'9'
}

/** Produce a localized date string in ISO-like form (YYYY-MM-DD) using localized digits. */
fun getLocalizedDateString(year: Int, month: Int, day: Int): String {
    return "${formatNumber(year)}-${formatNumber(month)}-${formatNumber(day)}"
}
 
