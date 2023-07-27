package com.byagowi.persiancalendar.entities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.inputmethod.InputMethodManager
import androidx.annotation.VisibleForTesting
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.AFGHANISTAN_TIMEZONE_ID
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.NEPAL_TIMEZONE_ID
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.utils.listOf12Items
import com.byagowi.persiancalendar.utils.listOf7Items
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.variants.debugLog
import io.github.cosinekitty.astronomy.EclipseKind
import io.github.persiancalendar.praytimes.CalculationMethod
import java.util.Locale
import java.util.TimeZone


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
    NE("ne", "नेपाली"),
    RU("ru", "Русский"),
    TG("tg", "Тоҷикӣ"),
    TR("tr", "Türkçe"),
    UR("ur", "اردو"),
    ZH_CN("zh-CN", "中文");

    val isArabic get() = this == AR
    val isDari get() = this == FA_AF
    val isPersian get() = this == FA
    val isIranianEnglish get() = this == EN_IR
    val isNepali get() = this == NE

    val showNepaliCalendar get() = this == NE

    val language get() = code.replace(Regex("-(IR|AF|US|CN)"), "")

    // en-IR and fa-AF aren't recognized by system, that's handled by #language
    fun asSystemLocale() = Locale(language)

    val inParentheses: String
        get() = when (this) {
            JA, ZH_CN -> "%s（%s）"
            else -> "%s (%s)"
        }

    // Formatting "Day Month Year" considerations
    val dmy: String
        get() = when (this) {
            CKB -> "%1\$sی %2\$sی %3\$s"
            ZH_CN -> "%3\$s 年 %2\$s %1\$s 日"
            else -> "%1\$s %2\$s %3\$s"
        }
    val dm: String
        get() = when (this) {
            CKB -> "%1\$sی %2\$s"
            JA, ZH_CN -> "%2\$s %1\$s"
            else -> "%1\$s %2\$s"
        }
    val my: String
        get() = when (this) {
            CKB -> "%1\$sی %2\$s"
            ZH_CN -> "%2\$s 年 %1\$s"
            else -> "%1\$s %2\$s"
        }
    val timeAndDateFormat: String
        get() = when (this) {
            JA, ZH_CN -> "%2\$s %1\$s"
            else -> "%1\$s$spacedComma%2\$s"
        }
    val clockAmPmOrder: String
        get() = when (this) {
            ZH_CN -> "%2\$s %1\$s"
            else -> "%1\$s %2\$s"
        }

    val isLessKnownRtl: Boolean
        get() = when (this) {
            AZB, GLK -> true
            else -> false
        }

    val betterToUseShortCalendarName: Boolean
        get() = when (this) {
            EN_US, JA, ZH_CN, FR, ES, AR, TR, TG, RU -> true
            else -> false
        }

    val mightPreferUmmAlquraIslamicCalendar: Boolean
        get() = when (this) {
            FA_AF, PS, UR, AR, CKB, EN_US, JA, ZH_CN, FR, ES, TR, KMR, TG, NE, RU -> true
            else -> false
        }

    val preferredCalculationMethod: CalculationMethod
        get() = when (this) {
            FA_AF, PS, UR, AR, CKB, TR, KMR, TG, NE -> CalculationMethod.MWL
            else -> CalculationMethod.Tehran
        }

    val isHanafiMajority: Boolean
        get() = when (this) {
            TR, FA_AF, PS, TG, NE -> true
            else -> false
        }

    // Based on locale, we can presume user is able to read Persian
    val isUserAbleToReadPersian: Boolean
        get() = when (this) {
            FA, GLK, AZB, FA_AF, EN_IR -> true
            else -> false
        }

    val showIranTimeOption
        get() = when (this) {
            FA, AZB, CKB, EN_IR, EN_US, ES, FR, GLK, JA, ZH_CN, KMR -> true
            else -> false
        }

    // Whether locale uses الفبا or not
    val isArabicScript: Boolean
        get() = when (this) {
            EN_US, JA, ZH_CN, FR, ES, RU, TR, KMR, EN_IR, TG, NE -> false
            else -> true
        }

    // Whether locale would prefer local digits like ۱۲۳ over the global ones, 123, initially at least
    val prefersLocalDigits: Boolean
        get() = when (this) {
            UR, EN_IR, EN_US, JA, ZH_CN, FR, ES, RU, TR, KMR, TG -> false
            else -> true
        }

    // Whether the language doesn't need " and " between date parts or not
    val languagePrefersHalfSpaceAndInDates: Boolean
        get() = when (this) {
            JA, ZH_CN -> true
            else -> false
        }

    // Local digits (۱۲۳) make sense for the locale
    val canHaveLocalDigits get() = isArabicScript || isIranianEnglish || isNepali

    // Prefers ٤٥٦ over ۴۵۶
    val preferredDigits
        get() = when (this) {
            AR, CKB -> ARABIC_INDIC_DIGITS
            NE -> DEVANAGARI_DIGITS
            else -> PERSIAN_DIGITS
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
            EN_US, JA, ZH_CN, FR, ES, RU, UR, TR, KMR, TG -> true
            else -> false
        }

    private val prefersNepaliCalendar: Boolean
        get() = isNepali

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

    val defaultMainCalendar
        get() = when {
            this == FA -> CalendarType.SHAMSI.name
            prefersGregorianCalendar -> CalendarType.GREGORIAN.name
            prefersIslamicCalendar -> CalendarType.ISLAMIC.name
            prefersPersianCalendar -> CalendarType.SHAMSI.name
            prefersNepaliCalendar -> CalendarType.NEPALI.name
            else -> CalendarType.SHAMSI.name
        }

    val defaultOtherCalendars
        get() = when {
            this == FA -> "${CalendarType.GREGORIAN.name},${CalendarType.ISLAMIC.name}"
            prefersGregorianCalendar -> "${CalendarType.ISLAMIC.name},${CalendarType.SHAMSI.name}"
            prefersIslamicCalendar -> "${CalendarType.GREGORIAN.name},${CalendarType.SHAMSI.name}"
            prefersPersianCalendar -> "${CalendarType.GREGORIAN.name},${CalendarType.ISLAMIC.name}"
            prefersNepaliCalendar -> CalendarType.GREGORIAN.name
            else -> "${CalendarType.GREGORIAN.name},${CalendarType.ISLAMIC.name}"
        }

    val defaultWeekStart
        get() = when (this) {
            FA -> "0"
            TR, TG, RU -> "2" // Monday
            else -> if (prefersGregorianCalendar || isNepali) "1"/*Sunday*/ else "0"
        }

    val defaultWeekEnds
        get() = when {
            this == FA -> setOf("6")
            isNepali -> setOf("0")
            prefersGregorianCalendar -> setOf("0", "1") // Saturday and Sunday
            else -> setOf("6") // 6 means Friday
        }

    val additionalShiftWorkTitles: List<String>
        get() = when (this) {
            FA -> listOf("مرخصی", "صبح/شب", "صبح/عصر", "عصر/شب")
            else -> emptyList()
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

    fun getNepaliMonths(): List<String> = when (this) {
        NE -> nepaliMonths
        else -> nepaliMonthsInEnglish
    }

    fun getWeekDays(context: Context): List<String> = when (this) {
        FA, EN_IR, FA_AF -> weekDaysInPersian
        else -> weekDays.map(context::getString)
    }

    fun getWeekDaysInitials(context: Context): List<String> = when (this) {
        EN_IR -> weekDaysInitialsInEnglishIran
        FA, FA_AF -> weekDaysInitialsInPersian
        else -> weekDaysInitials.map(context::getString)
    }

    fun getCountryName(cityItem: CityItem): String = when {
        !isArabicScript -> cityItem.countryEn
        isArabic -> cityItem.countryAr
        this == CKB -> cityItem.countryCkb
        else -> cityItem.countryFa
    }

    fun getCityName(cityItem: CityItem): String = when {
        !isArabicScript -> cityItem.en
        isArabic -> cityItem.ar
        this == CKB -> cityItem.ckb
        else -> cityItem.fa
    }

    val countriesOrder
        get() = when {
            isAfghanistanExclusive -> afCodeOrder
            isArabic -> arCodeOrder
            this == TR || this == KMR -> trCodeOrder
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

    // Too hard to translate and don't want to disappoint translators thus
    // not moved yet to our common i18n system
    fun tryTranslateEclipseType(isSolar: Boolean, type: EclipseKind) = when (this) {
        EN_US, EN_IR -> {
            when {
                isSolar && type == EclipseKind.Annular -> "Annular solar eclipse"
                isSolar && type == EclipseKind.Partial -> "Partial solar eclipse"
                !isSolar && type == EclipseKind.Partial -> "Partial lunar eclipse"
                !isSolar && type == EclipseKind.Penumbral -> "Penumbral lunar eclipse"
                isSolar && type == EclipseKind.Total -> "Total solar eclipse"
                !isSolar && type == EclipseKind.Total -> "Total lunar eclipse"
                else -> null
            }
        }

        FA, FA_AF -> {
            when {
                isSolar && type == EclipseKind.Annular -> "خورشیدگرفتگی حلقه‌ای"
                isSolar && type == EclipseKind.Partial -> "خورشیدگرفتگی جزئی"
                !isSolar && type == EclipseKind.Partial -> "ماه‌گرفتگی جزئی"
                !isSolar && type == EclipseKind.Penumbral -> "ماه‌گرفتگی نیم‌سایه‌ای"
                isSolar && type == EclipseKind.Total -> "خورشیدگرفتگی کلی"
                !isSolar && type == EclipseKind.Total -> "ماه‌گرفتگی کلی"
                else -> null
            }
        }

        else -> null
    }

    companion object {
        @SuppressLint("ConstantLocale")
        val userDeviceLanguage = Locale.getDefault().language ?: "en"

        @SuppressLint("ConstantLocale")
        private val userDeviceCountry = Locale.getDefault().country ?: "IR"

        private val userTimeZoneId = TimeZone.getDefault().id ?: IRAN_TIMEZONE_ID

        // Preferred app language for certain locale
        fun getPreferredDefaultLanguage(context: Context): Language {
            return when (userDeviceLanguage) {
                FA.code -> if (userDeviceCountry == "AF") FA_AF else FA
                "en", EN_US.code ->
                    guessLanguageFromTimezoneId() ?: guessLanguageFromKeyboards(context)

                else -> valueOfLanguageCode(userDeviceLanguage) ?: EN_US
            }
        }

        private fun guessLanguageFromTimezoneId(): Language? = when (userTimeZoneId) {
            IRAN_TIMEZONE_ID -> FA
            AFGHANISTAN_TIMEZONE_ID -> FA_AF
            NEPAL_TIMEZONE_ID -> NE
            // Other than these specific zones let's respect user device language anyway
            else -> null
        }

        // Based on https://stackoverflow.com/a/28216764 but doesn't seem to work
        private fun guessLanguageFromKeyboards(context: Context): Language = runCatching {
            val imm = context.getSystemService<InputMethodManager>() ?: return EN_US
            for (method in imm.enabledInputMethodList) {
                for (submethod in imm.getEnabledInputMethodSubtypeList(method, true)) {
                    if (submethod.mode == "keyboard") {
                        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            submethod.languageTag
                        } else @Suppress("DEPRECATION") submethod.locale
                        debugLog("Language: '$locale' is available in keyboards")
                        if (locale.isEmpty()) continue
                        val language = valueOfLanguageCode(locale)
                            ?: valueOfLanguageCode(locale.split("-").firstOrNull() ?: "")
                        // Use the knowledge only to detect Persian language
                        // as others might be surprising
                        if (language == FA || language == FA_AF) return language
                    }
                }
            }
            return EN_US
        }.onFailure(logException).getOrNull() ?: EN_US

        fun valueOfLanguageCode(languageCode: String) = entries.find { it.code == languageCode }

        private val arabicSortReplacements = mapOf(
            'ی' to "ي",
            'ک' to "ك",
            'گ' to "كی",
            'ژ' to "زی",
            'چ' to "جی",
            'پ' to "بی",
            'و' to "نی",
            'ڕ' to "ری",
            'ڵ' to "لی",
            'ڤ' to "فی",
            'ۆ' to "وی",
            'ێ' to "یی",
            'ھ' to "نی",
            'ە' to "هی",
        )

        @VisibleForTesting
        fun prepareForArabicSort(text: String): String =
            text.map { arabicSortReplacements[it] ?: it }.joinToString("")

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
        private val weekDaysInitials = listOf7Items(
            R.string.saturday_short, R.string.sunday_short, R.string.monday_short,
            R.string.tuesday_short, R.string.wednesday_short, R.string.thursday_short,
            R.string.friday_short
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
            // https://apll.ir/wp-content/uploads/2018/10/D-1394.pdf
            // advices to use یکشنبه and پنجشنبه on "مرکبهایی که بسیطگونه است"
            "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
        )
        private val weekDaysInitialsInPersian = listOf7Items(
            "ش", "ی", "د", "س", "چ", "پ", "ج"
        )
        private val weekDaysInitialsInEnglishIran = listOf7Items(
            "Sh", "Ye", "Do", "Se", "Ch", "Pa", "Jo"
        )

        // https://github.com/techgaun/ad-bs-converter/blob/4731f2c/src/converter.js
        // https://en.wikipedia.org/wiki/Vikram_Samvat
        val nepaliMonths = listOf12Items(
            "बैशाख", "जेष्ठ", "आषाढ", "श्रावण", "भाद्र", "आश्विन",
            "कार्तिक", "मंसिर", "पौष", "माघ", "फाल्गुन", "चैत्र"
        )
        val nepaliMonthsInEnglish = listOf12Items(
            "Baisakh", "Jestha", "Ashadh", "Shrawan", "Bhadra", "Ashwin",
            "Kartik", "Mangsir", "Paush", "Mangh", "Falgun", "Chaitra"
        )

        private val irCodeOrder = listOf("zz", "ir", "tr", "af", "iq")
        private val afCodeOrder = listOf("zz", "af", "ir", "tr", "iq")
        private val arCodeOrder = listOf("zz", "iq", "tr", "ir", "af")
        private val trCodeOrder = listOf("zz", "tr", "ir", "iq", "af")

        // See the naming here, https://developer.mozilla.org/en-US/docs/Web/CSS/list-style-type
        val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val ARABIC_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val ARABIC_INDIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val DEVANAGARI_DIGITS = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')
        // CJK digits: charArrayOf('０', '１', '２', '３', '４', '５', '６', '７', '８', '９')
        // but they weren't looking nice in the UI
    }
}
