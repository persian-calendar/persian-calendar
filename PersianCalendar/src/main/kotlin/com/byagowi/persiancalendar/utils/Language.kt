package com.byagowi.persiancalendar.utils

import android.content.Context
import com.byagowi.persiancalendar.R
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

    fun getPersianMonths(context: Context): List<String> = when (this) {
        FA, EN_IR -> persianCalendarMonthsInPersian
        FA_AF -> persianCalendarMonthsInDari
        else -> persianCalendarMonths.map(context::getString)
    }

    fun getIslamicMonths(context: Context): List<String> = when (this) {
        FA, EN_IR, FA_AF -> islamicCalendarMonthsInPersian
        else -> islamicCalendarMonths.map(context::getString)
    }

    fun getGregorianMonths(context: Context, easternGregorianArabicMonths: Boolean) = when (this) {
        FA, EN_IR -> gregorianCalendarMonthsInPersian
        FA_AF -> gregorianCalendarMonthsInDari
        AR -> {
            if (easternGregorianArabicMonths) easternGregorianCalendarMonths
            else gregorianCalendarMonths.map(context::getString)
        }
        else -> gregorianCalendarMonths.map(context::getString)
    }

    fun getWeekDays(context: Context): List<String> = when (this) {
        FA, EN_IR, FA_AF -> weekDaysInPersian
        else -> weekDays.map(context::getString)
    }

    fun getWeekDaysInitials(context: Context): List<String> = when (this) {
        AR -> weekDaysInitialsInArabic
        AZB -> weekDaysInitialsInAzerbaijani
        else -> getWeekDays(context).map { it.substring(0, 1) }
    }

    companion object {
        private val persianCalendarMonths = listOf12Items(
            R.string.farvardin, R.string.ordibehesht, R.string.khordad,
            R.string.tir, R.string.mordad, R.string.shahrivar,
            R.string.mehr, R.string.aban, R.string.azar, R.string.dey,
            R.string.bahman, R.string.esfand
        )
        private val islamicCalendarMonths = listOf12Items(
            R.string.muharram, R.string.safar, R.string.rabi_al_awwal,
            R.string.rabi_al_thani, R.string.jumada_al_awwal, R.string.jumada_al_thani,
            R.string.rajab, R.string.shaban, R.string.ramadan, R.string.shawwal,
            R.string.dhu_al_qidah, R.string.dhu_al_hijjah
        )
        private val gregorianCalendarMonths = listOf12Items(
            R.string.january, R.string.february, R.string.march,
            R.string.april, R.string.may, R.string.june, R.string.july,
            R.string.august, R.string.september, R.string.october,
            R.string.november, R.string.december
        )
        private val weekDays = listOf7Items(
            R.string.saturday, R.string.sunday, R.string.monday, R.string.tuesday,
            R.string.wednesday, R.string.thursday, R.string.friday
        )

        // These are special cases and new ones should be translated in strings.xml of the language
        private val persianCalendarMonthsInPersian = listOf12Items(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد",
            "شهریور", "مهر", "آبان", "آذر", "دی",
            "بهمن", "اسفند"
        )
        private val islamicCalendarMonthsInPersian = listOf12Items(
            "مُحَرَّم", "صَفَر", "ربیع‌الاول", "ربیع‌الثانی", "جمادى‌الاولى", "جمادی‌الثانیه",
            "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
        )
        private val gregorianCalendarMonthsInPersian = listOf12Items(
            "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن", "ژوئیه", "اوت", "سپتامبر", "اکتبر",
            "نوامبر", "دسامبر"
        )
        private val persianCalendarMonthsInDari = listOf12Items(
            "حمل", "ثور", "جوزا", "سرطان", "اسد", "سنبله",
            "میزان", "عقرب", "قوس", "جدی", "دلو",
            "حوت"
        )
        private val gregorianCalendarMonthsInDari = listOf12Items(
            "جنوری", "فبروری", "مارچ", "اپریل", "می", "جون", "جولای", "آگست", "سپتمبر", "اکتبر",
            "نومبر", "دیسمبر"
        )
        private val easternGregorianCalendarMonths = listOf12Items(
            "كانون الثاني", "شباط", "آذار", "نيسان", "أيار", "حزيران", "تموز", "آب", "أيلول",
            "تشرين الأول", "تشرين الثاني", "كانون الأول"
        )
        private val weekDaysInPersian = listOf7Items(
            "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
        )
        private val weekDaysInitialsInArabic = listOf7Items(
            "سب", "أح", "اث", "ثل", "أر", "خم", "جم"
        )
        private val weekDaysInitialsInAzerbaijani = listOf7Items(
            "یئل", "سۆد", "دۇز", "آرا", "اوْد", "سۇ", "آینی"
        )
    }
}
