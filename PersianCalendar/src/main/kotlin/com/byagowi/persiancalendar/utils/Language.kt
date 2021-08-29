package com.byagowi.persiancalendar.utils

import java.util.*

enum class Language(val code: String, val nativeName: String) {
    // The following order is used for language change dialog also
    FA("fa", "فارسی"),
    FA_AF("fa-AF", "دری"),
    PS("ps", "پښتو"),
    CKB("ckb", "کوردی"),
    AR("ar", "العربية"),
    GLK("glk", "گيلکي"),
    AZB("azb", "تۆرکجه"),
    UR("ur", "اردو"),
    EN_IR("en", "English (Iran)"),
    EN_US("en-US", "English"),
    ES("es", "Español"),
    FR("fr", "Français"),
    JA("ja", "日本語");

    val isArabic get() = this == AR
    val isDari get() = this == FA_AF
    val isKurdish get() = this == CKB
    val isPersian get() = this == FA
    private val isJapanese get() = this == JA

    val language get() = code.replace(Regex("-(IR|AF|US)"), "")

    // en-IR and fa-AF aren't recognized by system, that's handled by #language
    fun asSystemLocale() = Locale(language)

    // Formatting "Day Month Year" considerations
    val dmy: String get() = if (isKurdish) "%sی %sی %s" else "%s %s %s"
    val dm: String get() = if (isKurdish) "%sی %s" else "%s %s"

    val isLessKnownRtl: Boolean
        get() = when (this) {
            AZB, GLK -> true
            else -> false
        }

    val betterToUseShortCalendarName: Boolean
        get() = when (this) {
            EN_US, JA, FR, ES, AR -> true
            else -> false
        }

    val mightPreferNonLocalIslamicCalendar: Boolean
        get() = when (this) {
            FA_AF, PS, UR, AR, CKB, EN_US, JA, FR, ES -> true
            else -> false
        }

    // Based on locale, we can presume user is able to read Persian
    val isUserAbleToReadPersian: Boolean
        get() = when (this) {
            FA, GLK, AZB, FA_AF, EN_IR -> true
            else -> false
        }

    // Whether locale uses الفبا or not
    val isArabicScript: Boolean
        get() = when (this) {
            EN_US, JA, FR, ES, EN_IR -> false
            else -> true
        }

    // Whether locale would prefer local digits like ۱۲۳ over the global ones, 123, initially at least
    val prefersLocalDigits: Boolean
        get() = when (this) {
            UR, EN_IR, EN_US, JA, FR, ES -> false
            else -> true
        }

    // Prefers ٤٥٦ over ۴۵۶
    val prefersArabicIndicDigits: Boolean
        get() = when (this) {
            AR, CKB -> true
            else -> false
        }

    // We can presume user is from Afghanistan
    val isAfghanistanExclusive: Boolean
        get() = when (this) {
            FA_AF, PS -> true
            else -> false
        }

    // We can presume user is from Iran
    val isIranExclusive: Boolean
        get() = when (this) {
            AZB, GLK, FA, EN_IR -> true
            else -> false
        }

    // We can presume user would prefer Gregorian calendar at least initially
    val prefersGregorianCalendar: Boolean
        get() = when (this) {
            EN_US, JA, FR, ES, UR -> true
            else -> false
        }

    // We can presume user would prefer Gregorian calendar at least initially
    val prefersIslamicCalendar: Boolean
        get() = when (this) {
            AR -> true
            else -> false
        }

    // We can presume user would prefer Gregorian calendar at least initially
    val prefersPersianCalendar: Boolean
        get() = when (this) {
            AZB, GLK, FA, FA_AF, PS, EN_IR -> true
            else -> false
        }

    // Languages use same string from AM/PM
    val isDerivedFromPersian: Boolean
        get() = when (this) {
            FA, FA_AF, EN_IR -> true
            else -> false
        }
}
