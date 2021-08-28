package com.byagowi.persiancalendar.utils

import java.util.*

@JvmInline
value class Language(val code: String) {

    val isArabic get() = this == ar
    val isDari get() = this == fa_af
    val isKurdish get() = this == ckb
    val isPersian get() = this == fa
    private val isJapanese get() = this == ja

    val language get() = code.replace(Regex("-(IR|AF|US)"), "")

    // en-IR and fa-AF aren't recognized by system, let's handle that here
    fun asSystemLocale() = Locale(language)

    // Formatting "Day Month Year" considerations
    val dmy: String get() = if (isKurdish) "%sی %sی %s" else "%s %s %s"
    val dm: String get() = if (isKurdish) "%sی %s" else "%s %s"

    val isLessKnownRtl: Boolean
        get() = when (this) {
            azb, glk -> true
            else -> false
        }

    val spacedComma: String
        get() = when {
            isJapanese -> "、"
            isArabicScript -> "، "
            else -> ", "
        }

    val betterToUseShortCalendarName: Boolean
        get() = when (this) {
            en_us, ja, fr, es, ar -> true
            else -> false
        }

    val spacedColon: String get() = if (isJapanese) "：" else ": "

    val mightPreferNonLocalIslamicCalendar: Boolean
        get() = when (this) {
            fa_af, ps, ur, ar, ckb, en_us, ja, fr, es -> true
            else -> false
        }

    // Based on locale, we can presume user is able to read Persian
    val isUserAbleToReadPersian: Boolean
        get() = when (this) {
            fa, glk, azb, fa_af, en_ir -> true
            else -> false
        }

    // Whether locale uses الفبا or not
    val isArabicScript: Boolean
        get() = when (this) {
            en_us, ja, fr, es, en_ir -> false
            else -> true
        }

    // Whether locale would prefer local digits like ۱۲۳ over the global ones, 123, initially at least
    val prefersLocalDigits: Boolean
        get() = when (this) {
            ur, en_ir, en_us, ja, fr, es -> false
            else -> true
        }

    // Prefers ٤٥٦ over ۴۵۶
    val prefersArabicIndicDigits: Boolean
        get() = when (this) {
            ar, ckb -> true
            else -> false
        }

    // We can presume user is from Afghanistan
    val isAfghanistanExclusive: Boolean
        get() = when (this) {
            fa_af, ps -> true
            else -> false
        }

    // We can presume user is from Iran
    val isIranExclusive: Boolean
        get() = when (this) {
            azb, glk, fa, en_ir -> true
            else -> false
        }

    // We can presume user would prefer Gregorian calendar at least initially
    val prefersGregorianCalendar: Boolean
        get() = when (this) {
            en_us, ja, fr, es, ur -> true
            else -> false
        }

    // We can presume user would prefer Gregorian calendar at least initially
    val prefersIslamicCalendar: Boolean
        get() = when (this) {
            ar -> true
            else -> false
        }

    // We can presume user would prefer Gregorian calendar at least initially
    val prefersPersianCalendar: Boolean
        get() = when (this) {
            azb, glk, fa, fa_af, ps, en_ir -> true
            else -> false
        }

    // Languages use same string from AM/PM/
    val isDerivedFromPersian: Boolean
        get() = when (this) {
            fa, fa_af, en_ir -> true
            else -> false
        }

    companion object {
        val fa = Language("fa")
        val fa_af = Language("fa-AF")
        val ps = Language("ps")
        val glk = Language("glk")
        val ar = Language("ar")
        val en_ir = Language("en")
        val fr = Language("fr")
        val es = Language("es")
        val en_us = Language("en-US")
        val ja = Language("ja")
        val azb = Language("azb")
        val ckb = Language("ckb")
        val ur = Language("ur")

        val values = listOf( // Akin to enums' values() static method
            fa to "فارسی",
            fa_af to "دری",
            ps to "پښتو",
            ckb to "کوردی",
            ar to "العربية",
            glk to "گيلکي",
            azb to "تۆرکجه",
            ur to "اردو",
            en_ir to "English (Iran)",
            en_us to "English",
            es to "Español",
            fr to "Français",
            ja to "日本語"
        )
    }
}
