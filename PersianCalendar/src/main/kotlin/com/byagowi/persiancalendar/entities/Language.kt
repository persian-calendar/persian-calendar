package com.byagowi.persiancalendar.entities

import android.annotation.SuppressLint
import android.content.Context
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.listOf12Items
import com.byagowi.persiancalendar.utils.listOf7Items
import io.github.persiancalendar.praytimes.CalculationMethod
import java.util.*

enum class Language(val code: String, val nativeName: String) {
    // The following order is used for language change dialog also
    // Official languages
    FA("fa", "فارسی"),
    FA_AF("fa-AF", "دری"),
    PS("ps", "پښتو"),

    // Rest, sorted by their language code
    AR("ar", "العربية"),
    AZB("azb", "تۆرکجه"),
    CKB("ckb", "کوردی"),
    EN_IR("en", "English (Iran)"),
    EN_US("en-US", "English"),
    ES("es", "Español"),
    FR("fr", "Français"),
    GLK("glk", "گيلکي"),
    JA("ja", "日本語"),
    KMR("kmr", "Kurdî"),
    TR("tr", "Türkçe"),
    UR("ur", "اردو");

    val isArabic get() = this == AR
    val isDari get() = this == FA_AF
    val isPersian get() = this == FA
    val isTurkish get() = this == TR
    private val isNorthernKurdish get() = this == KMR
    private val isKurdish get() = this == CKB

    val language get() = code.replace(Regex("-(IR|AF|US)"), "")

    // en-IR and fa-AF aren't recognized by system, that's handled by #language
    fun asSystemLocale() = Locale(language)

    // Formatting "Day Month Year" considerations
    val dmy: String get() = if (isKurdish) "%sی %sی %s" else "%s %s %s"
    val dm: String get() = if (isKurdish) "%sی %s" else "%s %s"
    val my: String get() = if (isKurdish) "%sی %s" else "%s %s"

    val isLessKnownRtl: Boolean
        get() = when (this) {
            AZB, GLK -> true
            else -> false
        }

    val betterToUseShortCalendarName: Boolean
        get() = when (this) {
            EN_US, JA, FR, ES, AR, TR -> true
            else -> false
        }

    val mightPreferUmmAlquraIslamicCalendar: Boolean
        get() = when (this) {
            FA_AF, PS, UR, AR, CKB, EN_US, JA, FR, ES, TR, KMR -> true
            else -> false
        }

    val preferredCalculationMethod: CalculationMethod
        get() = when (this) {
            FA_AF, PS, UR, AR, CKB, TR, KMR -> CalculationMethod.MWL
            else -> CalculationMethod.Tehran
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
            EN_US, JA, FR, ES, TR, KMR, EN_IR -> false
            else -> true
        }

    // Whether locale would prefer local digits like ۱۲۳ over the global ones, 123, initially at least
    val prefersLocalDigits: Boolean
        get() = when (this) {
            UR, EN_IR, EN_US, JA, FR, ES, TR, KMR -> false
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
    private val prefersGregorianCalendar: Boolean
        get() = when (this) {
            EN_US, JA, FR, ES, UR, TR, KMR -> true
            else -> false
        }

    // We can presume user would prefer Gregorian calendar at least initially
    private val prefersIslamicCalendar: Boolean
        get() = when (this) {
            AR -> true
            else -> false
        }

    // We can presume user would prefer Gregorian calendar at least initially
    private val prefersPersianCalendar: Boolean
        get() = when (this) {
            AZB, GLK, FA, FA_AF, PS, EN_IR -> true
            else -> false
        }

    val defaultMainCalendar get() = when {
        this == FA -> CalendarType.SHAMSI.name
        prefersGregorianCalendar -> CalendarType.GREGORIAN.name
        prefersIslamicCalendar -> CalendarType.ISLAMIC.name
        prefersPersianCalendar -> CalendarType.SHAMSI.name
        else -> CalendarType.SHAMSI.name
    }

    val defaultOtherCalendars get() = when {
        this == FA -> "${CalendarType.GREGORIAN.name},${CalendarType.ISLAMIC.name}"
        prefersGregorianCalendar -> "${CalendarType.ISLAMIC.name},${CalendarType.SHAMSI.name}"
        prefersIslamicCalendar -> "${CalendarType.ISLAMIC.name},${CalendarType.SHAMSI.name}"
        prefersPersianCalendar -> "${CalendarType.GREGORIAN.name},${CalendarType.ISLAMIC.name}"
        else -> "${CalendarType.GREGORIAN.name},${CalendarType.ISLAMIC.name}"
    }

    val defaultWeekStart get() = when {
        this == FA -> "0"
        isTurkish -> "2" // Monday
        prefersGregorianCalendar -> "1" // Sunday
        else -> "0"
    }

    val defaultWeekEnds get() = when {
        this == FA -> setOf("6")
        prefersGregorianCalendar -> setOf("0", "1") // Saturday and Sunday
        else -> setOf("6") // 6 means Friday
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
        TR -> weekDaysInitialsInTurkish
        EN_IR -> weekDaysInitialsInEnglishIran
        else -> getWeekDays(context).map { it.substring(0, 1) }
    }

    fun getCountryName(cityItem: CityItem): String = when {
        !isArabicScript -> cityItem.countryEn
        isArabic -> cityItem.countryAr
        isKurdish -> cityItem.countryCkb
        else -> cityItem.countryFa
    }

    fun getCityName(cityItem: CityItem): String = when {
        !isArabicScript -> cityItem.en
        isArabic -> cityItem.ar
        isKurdish -> cityItem.ckb
        else -> cityItem.fa
    }

    val countriesOrder
        get() = when {
            isAfghanistanExclusive -> afCodeOrder
            isArabic -> arCodeOrder
            isTurkish || isNorthernKurdish -> trCodeOrder
            else -> irCodeOrder
        }

    // Some languages don't have alphabet order matching with unicode order, this fixes them
    fun prepareForSort(text: String) = when {
        isArabicScript && !isArabic -> prepareForArabicSort(text)
        // We will need some preparation for non-English latin script
        // languages (Turkish, Spanish, French, ...) but our cities.json
        // don't have those a translation to those, so
        else -> text
    }

    private fun prepareForArabicSort(text: String) = text
        .replace("ی", "ي")
        .replace("ک", "ك")
        .replace("گ", "كی")
        .replace("ژ", "زی")
        .replace("چ", "جی")
        .replace("پ", "بی")
        .replace("و", "نی")
        .replace("ڕ", "ری")
        .replace("ڵ", "لی")
        .replace("ڤ", "فی")
        .replace("ۆ", "وی")
        .replace("ێ", "یی")
        .replace("ھ", "نی")
        .replace("ە", "هی")

    companion object {
        @SuppressLint("ConstantLocale")
        val userDeviceLanguage = Locale.getDefault().language ?: EN_US.language

        // Preferred app language for certain locale
        fun preferredDefaultLanguage() = when (userDeviceLanguage) {
            FA.code, "en", EN_US.code -> FA.code
            else -> values().find { it.code == userDeviceLanguage }?.code ?: EN_US.code
        }

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
        private val weekDaysInitialsInTurkish = listOf7Items(
            "Ct", "Pz", "Pt", "Sa", "Ça", "Pe", "Cu"
        )
        private val weekDaysInitialsInEnglishIran = listOf7Items(
            "Sh", "Ye", "Do", "Se", "Ch", "Pa", "Jo"
        )

        private val irCodeOrder = listOf("zz", "ir", "tr", "af", "iq")
        private val afCodeOrder = listOf("zz", "af", "ir", "tr", "iq")
        private val arCodeOrder = listOf("zz", "iq", "tr", "ir", "af")
        private val trCodeOrder = listOf("zz", "tr", "ir", "iq", "af")
    }
}
