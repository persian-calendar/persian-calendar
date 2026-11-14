package com.byagowi.persiancalendar.entities

import android.content.Context
import android.content.res.Resources
import android.view.inputmethod.InputMethodManager
import androidx.annotation.VisibleForTesting
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.AFGHANISTAN_TIMEZONE_ID
import com.byagowi.persiancalendar.AU_IN_KM
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.NEPAL_TIMEZONE_ID
import com.byagowi.persiancalendar.global.iso8601DateFormat
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.shared.R
import com.byagowi.persiancalendar.utils.debugLog
import com.byagowi.persiancalendar.utils.listOf12Items
import com.byagowi.persiancalendar.utils.listOf7Items
import com.byagowi.persiancalendar.utils.logException
import io.github.persiancalendar.praytimes.CalculationMethod
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToLong

enum class Language(val code: String, val nativeName: String) {
    // The following order is used for language change dialog also
    // Official languages
    FA(
        "fa", "فارسی",
    ),
    FA_AF(
        "fa-AF", "دری",
    ),
    PS(
        "ps", "پښتو",
    ),

    // Rest, sorted by their language code
    AR(
        "ar", "العربية",
    ),
    AZB(
        "azb", "تۆرکجه",
    ),
    CKB(
        "ckb", "کوردی",
    ),
    DE(
        "de", "Deutsch",
    ),
    EN_IR(
        "en", "English (Iran)",
    ),
    EN_US(
        "en-US", "English",
    ),
    ES(
        "es", "Español",
    ),
    FR(
        "fr", "Français",
    ),
    GLK(
        "glk", "گيلکي",
    ),
    IN(
        "in", "Bahasa Indonesia",
    ),
    IT(
        "it", "Italiano",
    ),
    JA(
        "ja", "日本語",
    ),
    KMR(
        "kmr", "Kurdî",
    ),
    NE(
        "ne", "नेपाली",
    ),
    OTA(
        "ota", "عثمانى",
    ),
    PA_PK(
        "pa-PK", "پنجابی",
    ),
    PT(
        "pt", "Português",
    ),
    RU(
        "ru", "Русский",
    ),
    TA(
        "ta", "தமிழ்",
    ),
    TG(
        "tg", "Тоҷикӣ",
    ),
    TR(
        "tr", "Türkçe",
    ),
    UK(
        "uk", "Українська",
    ),
    UR(
        "ur", "اردو",
    ),
    ZH_CN(
        "zh-CN", "中文",
    ),
    ZH_TW(
        "zh-TW", "繁體中文",
    );

    val isArabic get() = this == AR
    val isDari get() = this == FA_AF
    val isPersian get() = this == FA
    val isPersianOrDari get() = isPersian || isDari
    val isNepali get() = this == NE
    val isTamil get() = this == TA

    val showNepaliCalendar get() = this == NE

    val language get() = code.replace(Regex("-(IR|AF|US|CN)"), "")

    // en-IR and fa-AF aren't recognized by system, that's handled by #language
    fun asSystemLocale(): Locale = Locale.forLanguageTag(language)

    val inParentheses: String
        get() = when (this) {
            JA, ZH_CN, ZH_TW -> "%s（%s）"
            else -> "%s (%s)"
        }

    // Formatting "Day Month Year" considerations
    val dmy: String
        get() = when (this) {
            CKB -> $$"%1$sی %2$sی %3$s"
            ZH_CN, ZH_TW -> $$"%3$s 年 %2$s %1$s 日"
            else -> $$"%1$s %2$s %3$s"
        }
    val dm: String
        get() = when (this) {
            CKB -> $$"%1$sی %2$s"
            JA, ZH_CN, ZH_TW -> $$"%2$s %1$s"
            EN_US -> $$"%2$s %1$s"
            else -> $$"%1$s %2$s"
        }
    val my: String
        get() = when (this) {
            CKB -> $$"%1$sی %2$s"
            ZH_CN, ZH_TW -> $$"%2$s 年 %1$s"
            else -> $$"%1$s %2$s"
        }
    val timeAndDateFormat: String
        get() = when (this) {
            JA, ZH_CN, ZH_TW -> $$"%2$s %1$s"
            else -> $$"%1$s$$spacedComma%2$s"
        }
    val clockAmPmOrder: String
        get() = when (this) {
            ZH_CN, ZH_TW -> $$"%2$s %1$s"
            else -> $$"%1$s %2$s"
        }

    val isLessKnownRtl: Boolean
        get() = when (this) {
            AZB, GLK, OTA, PA_PK -> true
            else -> false
        }

    val betterToUseShortCalendarName: Boolean
        get() = when (this) {
            EN_US, EN_IR, JA, ZH_CN, ZH_TW, FR, ES, DE, PT, IT, AR, TR, TG, RU, UK, CKB, IN -> true
            else -> false
        }

    val mightPreferUmmAlquraIslamicCalendar: Boolean
        get() = when (this) {
            FA_AF, PS, UR, AR, CKB, EN_US, JA, ZH_CN, ZH_TW, FR, ES, DE, PT, IT, TR, KMR, TA, TG, NE, RU, UK, IN, PA_PK -> true
            else -> false
        }

    val preferredCalculationMethod: CalculationMethod
        get() = when (this) {
            FA_AF, PS, UR, ES, DE, AR, CKB, TR, IT, JA, PT, ZH_CN, ZH_TW, OTA, KMR, TG, NE, TA, UK, PA_PK -> CalculationMethod.MWL
            FA, AZB, EN_IR, EN_US, GLK -> CalculationMethod.Tehran
            IN -> CalculationMethod.Singapore
            FR -> CalculationMethod.France
            RU -> CalculationMethod.Russia
        }

    val isHanafiMajority: Boolean
        get() = when (this) {
            TR, FA_AF, PS, TG, NE, TA -> true
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
            FA, AZB, CKB, EN_IR, EN_US, GLK -> true
            else -> false
        }

    // Whether locale uses الفبا or not
    val isArabicScript: Boolean
        get() = when (this) {
            EN_US, JA, ZH_CN, ZH_TW, FR, ES, DE, PT, IT, RU, UK, TR, KMR, EN_IR, TG, NE, TA, IN -> false
            FA, FA_AF, PS, AR, AZB, CKB, GLK, OTA, UR, PA_PK -> true
        }

    // Whether locale would prefer local digits like ۱۲۳ over the global ones, 123, initially at least
    val prefersLocalNumeral: Boolean
        get() = when (this) {
            UR, EN_IR, EN_US, JA, ZH_CN, ZH_TW, FR, ES, DE, PT, IT, RU, UK, TR, KMR, TG, PA_PK -> false
            else -> true
        }

    // Whether the language doesn't need " and " between date parts or not
    val languagePrefersHalfSpaceAndInDates: Boolean
        get() = when (this) {
            JA, ZH_CN, ZH_TW -> true
            else -> false
        }

    // Local digits (۱۲۳) make sense for the locale
    val canHaveLocalNumeral get() = isArabicScript || isNepali || isTamil

    // Prefers ٤٥٦ over ۴۵۶
    val preferredNumeral
        get() = when (this) {
            FA, FA_AF, PS, GLK, AZB -> Numeral.PERSIAN
            AR, CKB, OTA -> Numeral.ARABIC_INDIC
            NE -> Numeral.DEVANAGARI
            TA -> Numeral.TAMIL
            else -> Numeral.ARABIC
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
            EN_US, JA, ZH_CN, ZH_TW, FR, ES, DE, PT, IT, RU, UK, UR, PA_PK, TR, KMR, TG, TA, IN -> true
            else -> false
        }

    // We can presume user would prefer Gregorian calendar at least initially
    private val prefersPersianCalendar: Boolean
        get() = when (this) {
            AZB, GLK, FA, FA_AF, PS, EN_IR -> true
            else -> false
        }

    val defaultCalendars: List<Calendar>
        get() = when {
            this == FA -> listOf(Calendar.SHAMSI, Calendar.GREGORIAN, Calendar.ISLAMIC)
            this == CKB -> if (userTimeZoneId == IRAN_TIMEZONE_ID) listOf(
                Calendar.SHAMSI, Calendar.GREGORIAN, Calendar.ISLAMIC,
            ) else listOf(Calendar.GREGORIAN, Calendar.ISLAMIC)

            this == IN -> listOf(Calendar.GREGORIAN, Calendar.ISLAMIC)
            this == EN_IR -> listOf(Calendar.SHAMSI, Calendar.GREGORIAN)
            this == TR -> listOf(Calendar.GREGORIAN, Calendar.ISLAMIC)
            this == AR || this == OTA -> listOf(Calendar.ISLAMIC, Calendar.GREGORIAN)
            prefersGregorianCalendar -> listOf(Calendar.GREGORIAN)
            this == NE -> listOf(Calendar.NEPALI, Calendar.GREGORIAN)
            prefersPersianCalendar -> listOf(Calendar.SHAMSI, Calendar.GREGORIAN, Calendar.ISLAMIC)
            else -> listOf(Calendar.GREGORIAN)
        }.let {
            if (when (userTimeZoneId) {
                    IRAN_TIMEZONE_ID, AFGHANISTAN_TIMEZONE_ID -> true
                    else -> false
                } && Calendar.SHAMSI !in it
            ) it + Calendar.SHAMSI else it
        }

    val defaultWeekStart
        get() = when (this) {
            FA, FA_AF, PS, AR, AZB, CKB, EN_IR, GLK, OTA -> WeekDay.SATURDAY
            EN_US, IN -> WeekDay.SUNDAY
            JA, ZH_CN, ZH_TW, FR, ES, DE, PT, IT, RU, UK, UR, PA_PK, TR, KMR, TG, TA, NE -> WeekDay.MONDAY
        }
    val defaultWeekStartAsString get() = defaultWeekStart.ordinal.toString()

    val defaultWeekEnds: Set<WeekDay>
        get() = when {
            this == FA || isIranExclusive -> setOf(WeekDay.FRIDAY)
            this == IN -> setOf(WeekDay.SATURDAY, WeekDay.SUNDAY)
            isAfghanistanExclusive -> setOf(WeekDay.FRIDAY)
            isNepali -> setOf(WeekDay.SATURDAY)
            prefersGregorianCalendar -> setOf(WeekDay.SATURDAY, WeekDay.SUNDAY)
            else -> setOf(WeekDay.FRIDAY)
        }
    val defaultWeekEndsAsStringSet get() = defaultWeekEnds.map { it.ordinal.toString() }.toSet()

    val additionalShiftWorkTitles: List<String>
        get() = when (this) {
            FA -> listOf("مرخصی", "صبح/شب", "صبح/عصر", "عصر/شب")
            else -> emptyList()
        }

    fun getPersianMonths(
        resources: Resources,
        alternativeMonthsInAzeri: Boolean,
        afghanistanHolidaysIsEnable: Boolean,
    ): List<String> = when (this) {
        FA -> persianCalendarMonthsInPersian
        FA_AF -> persianCalendarMonthsInDariOrPersianOldEra
        AZB -> if (alternativeMonthsInAzeri) persianCalendarMonthsInAzeriAlternative
        else persianCalendarMonths.map(resources::getString)

        AR -> if (userTimeZoneId == IRAN_TIMEZONE_ID) persianCalendarMonthsInArabicIran
        else persianCalendarMonths.map(resources::getString)

        EN_US -> when {
            userTimeZoneId == AFGHANISTAN_TIMEZONE_ID -> persianCalendarMonthsInDariOrPersianOldEraTransliteration
            afghanistanHolidaysIsEnable -> persianCalendarMonthsInDariOrPersianOldEraTransliteration
            else -> persianCalendarMonths.map(resources::getString)
        }

        else -> persianCalendarMonths.map(resources::getString)
    }

    fun getIslamicMonths(resources: Resources): List<String> = when (this) {
        FA, FA_AF -> islamicCalendarMonthsInPersian
        else -> islamicCalendarMonths.map(resources::getString)
    }

    fun getGregorianMonths(
        resources: Resources,
        alternativeGregorianMonths: Boolean,
    ): List<String> = when (this) {
        FA -> {
            if (alternativeGregorianMonths) gregorianCalendarMonthsInPersianEnglishPronunciation
            else gregorianCalendarMonthsInPersian
        }

        FA_AF -> gregorianCalendarMonthsInDari

        AR -> {
            if (alternativeGregorianMonths) easternGregorianCalendarMonths
            else gregorianCalendarMonths.map(resources::getString)
        }

        else -> gregorianCalendarMonths.map(resources::getString)
    }

    fun getNepaliMonths(): List<String> = when (this) {
        NE -> nepaliMonths
        else -> nepaliMonthsInEnglish
    }

    fun getWeekDays(resources: Resources): List<String> = when (this) {
        FA, FA_AF -> weekDaysInPersian
        EN_IR -> weekDaysInEnglishIran
        else -> WeekDay.entries.map {
            resources.getString(
                when (it) {
                    WeekDay.SATURDAY -> R.string.saturday
                    WeekDay.SUNDAY -> R.string.sunday
                    WeekDay.MONDAY -> R.string.monday
                    WeekDay.TUESDAY -> R.string.tuesday
                    WeekDay.WEDNESDAY -> R.string.wednesday
                    WeekDay.THURSDAY -> R.string.thursday
                    WeekDay.FRIDAY -> R.string.friday
                },
            )
        }
    }

    fun getWeekDaysInitials(resources: Resources): List<String> = when (this) {
        FA, FA_AF -> weekDaysInitialsInPersian
        EN_IR -> weekDaysInitialsInEnglishIran
        else -> WeekDay.entries.map {
            resources.getString(
                when (it) {
                    WeekDay.SATURDAY -> R.string.saturday_short
                    WeekDay.SUNDAY -> R.string.sunday_short
                    WeekDay.MONDAY -> R.string.monday_short
                    WeekDay.TUESDAY -> R.string.tuesday_short
                    WeekDay.WEDNESDAY -> R.string.wednesday_short
                    WeekDay.THURSDAY -> R.string.thursday_short
                    WeekDay.FRIDAY -> R.string.friday_short
                },
            )
        }
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

    // Some languages don't have alphabet order matching with Unicode order, this fixes them
    fun prepareForSort(text: String) = when {
        isArabicScript && !isArabic -> prepareForArabicSort(text)
        // We will need some preparation for non-English latin script
        // languages (Turkish, Spanish, French, …) but our cities.json
        // don't have those a translation to those, so
        else -> text
    }

    // https://en.wikipedia.org/wiki/List_of_date_formats_by_country
    fun allNumericsDateFormat(year: Int, month: Int, dayOfMonth: Int, numeral: Numeral): String {
        val iso8601DateFormat = iso8601DateFormat
        val sep = if (iso8601DateFormat) "-" else when (this) {
            PS, NE -> '-'
            KMR, RU, TR, DE -> '.'
            else -> '/'
        }
        val needsZeroPad = iso8601DateFormat || when (this) {
            FR, IT, NE, PT, RU, UK, TR, KMR -> true
            // According to the dated on the first page of https://calendar.ut.ac.ir/documents/2139738/7092644/Calendar-1404.pdf
            // It seems it's a zero padded locale
            // But according to نیازهای شرایط محلی برای زبان فارسی ایران it's not
            // https://drive.google.com/file/d/1yDoUbXnV_q6mrzzaRZK_AvsOLaU-O9Qy/view
            FA -> false
            else -> false
        }
        val format = if (iso8601DateFormat) $$"%1$s$$sep%2$s$$sep%3$s" else when (this) {
            // Year major
            FA, AZB, GLK, FA_AF, EN_IR, PS, JA, NE, ZH_CN, ZH_TW, OTA -> $$"%1$s$$sep%2$s$$sep%3$s"
            // Month major
            EN_US -> $$"%2$s$$sep%3$s$$sep%1$s"
            // Day major, most likely everything else goes here but check via JS'
            // new Date().toLocaleDateString('XX')
            AR, CKB, ES, DE, FR, IT, KMR, PT, RU, UK, TG, TR, UR, PA_PK, TA, IN -> $$"%3$s$$sep%2$s$$sep%1$s"
        }
        return format.format(
            numeral.format(year),
            numeral.format(
                "$month".let { if (needsZeroPad) it.padStart(2, '0') else it },
            ),
            numeral.format(
                "$dayOfMonth".let { if (needsZeroPad) it.padStart(2, '0') else it },
            ),
        )
    }

    // Indian locales always need to see moon view as https://en.wikipedia.org/wiki/Tithi
    val alwaysNeedMoonState: Boolean
        get() = when (this) {
            NE, TA -> true
            else -> false
        }

    fun formatAuAsKm(value: Double, kilometer: String): String =
        preferredNumeral.formatLongNumber((value * AU_IN_KM).roundToLong()) + " " + kilometer

    companion object {
        @Suppress("ConstantLocale")
        val userDeviceLanguage = Locale.getDefault().language ?: "en"

        @Suppress("ConstantLocale")
        private val userDeviceCountry = Locale.getDefault().country ?: "IR"

        private val userTimeZoneId = TimeZone.getDefault().id ?: IRAN_TIMEZONE_ID

        // Preferred app language for certain locale
        fun getPreferredDefaultLanguage(context: Context): Language {
            return when (userDeviceLanguage) {
                FA.code -> if (userDeviceCountry == "AF") FA_AF else FA

                "en", EN_US.code -> guessLanguageFromTimezoneId() ?: guessLanguageFromKeyboards(
                    context,
                )

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
            val imm = context.getSystemService<InputMethodManager>() ?: return@runCatching EN_US
            imm.enabledInputMethodList.forEach outer@{ method ->
                imm.getEnabledInputMethodSubtypeList(method, true).forEach { submethod ->
                    if (submethod.mode == "keyboard") {
                        val locale = submethod.languageTag
                        debugLog("Language: '$locale' is available in keyboards")
                        if (locale.isEmpty()) return@forEach
                        val language = valueOfLanguageCode(locale) ?: valueOfLanguageCode(
                            locale.split("-").firstOrNull().orEmpty(),
                        )
                        // Use the knowledge only to detect Persian language
                        // as others might be surprising
                        if (language?.isPersianOrDari == true) return@runCatching language
                    }
                }
            }
            return@runCatching EN_US
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
            R.string.bahman, R.string.esfand,
        )
        private val islamicCalendarMonths = listOf12Items(
            R.string.muharram, R.string.safar, R.string.rabi_alawwal,
            R.string.rabi_althani, R.string.jumada_al_awwal, R.string.jumada_al_thani,
            R.string.rajab, R.string.shabaan, R.string.ramadan, R.string.shawwal,
            R.string.dhu_al_qidah, R.string.dhu_al_hijjah,
        )
        private val gregorianCalendarMonths = listOf12Items(
            R.string.january, R.string.february, R.string.march,
            R.string.april, R.string.may, R.string.june, R.string.july,
            R.string.august, R.string.september, R.string.october,
            R.string.november, R.string.december,
        )

        // These are special cases and new ones should be translated in strings.xml of the language
        private val persianCalendarMonthsInPersian = listOf12Items(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد",
            "شهریور", "مهر", "آبان", "آذر", "دی",
            "بهمن", "اسفند",
        )
        private val persianCalendarMonthsInArabicIran = listOf12Items(
            "فروردین", "أرديبهشت", "خرداد", "تير", "مرداد",
            "شهريور", "مهر", "آبان", "آذر", "دي", "بهمن", "إسفند",
        )
        private val persianCalendarMonthsInAzeriAlternative = listOf12Items(
            "آغلارگۆلر", "گۆلن", "قیزاران", "قوْرا پیشیرن",
            "قۇیروق دوْغان", "زۇمار", "خزل", "قیروو",
            "آذر", "چیلله", "دوْندوران", "بایرام",
        )
        private val islamicCalendarMonthsInPersian = listOf12Items(
            "مُحَرَّم", "صَفَر", "ربیع‌الاول", "ربیع‌الثانی", "جمادى‌الاولى", "جمادی‌الثانیه",
            "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه",
        )
        private val gregorianCalendarMonthsInPersian = listOf12Items(
            "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
            "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر",
        )
        val persianCalendarMonthsInDariOrPersianOldEra = listOf12Items(
            "حمل", "ثور", "جوزا", "سرطان", "اسد", "سنبله",
            "میزان", "عقرب", "قوس", "جدی", "دلو", "حوت",
        )
        val persianCalendarMonthsInDariOrPersianOldEraTransliteration = listOf12Items(
            // https://www.evertype.com/standards/af/af-locales.pdf
            "Hamal", "Sawr", "Jawzā", "Saratān", "Asad", "Sonbola",
            "Mīzān", "Aqrab", "Qaws", "Jady", "Dalv", "Hūt",
        )
        private val gregorianCalendarMonthsInDari = listOf12Items(
            "جنوری", "فبروری", "مارچ", "اپریل", "می", "جون",
            "جولای", "اگست", "سپتمبر", "اکتوبر", "نومبر", "دسمبر",
        )
        private val gregorianCalendarMonthsInPersianEnglishPronunciation = listOf12Items(
            "جنوری", "فبروری", "مارچ", "اپریل", "می", "جون",
            "جولای", "آگوست", "سپتامبر", "اکتبر", "نوامبر", "دسامبر",
        )
        private val easternGregorianCalendarMonths = listOf12Items(
            "كانون الثاني", "شباط", "آذار", "نيسان", "أيار", "حزيران", "تموز", "آب", "أيلول",
            "تشرين الأول", "تشرين الثاني", "كانون الأول",
        )
        private val weekDaysInPersian = listOf7Items(
            // https://apll.ir/wp-content/uploads/2018/10/D-1394.pdf
            // advices to use یکشنبه and پنجشنبه on "مرکبهایی که بسیطگونه است"
            // The updated version https://www.ekhtebar.ir/wp-content/uploads/2023/07/Dastour-e-Khat-17.04.1402-3.pdf
            // doesn't have it though
            "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه",
        )
        private val weekDaysInitialsInPersian = listOf7Items(
            "ش", "ی", "د", "س", "چ", "پ", "ج",
        )
        private val weekDaysInEnglishIran = listOf7Items(
            "Shanbe", "Yekshanbe", "Doshanbe", "Seshanbe", "Chahaarshanbe",
            "Panjshanbe", "Jom’e",
        )
        private val weekDaysInitialsInEnglishIran = listOf7Items(
            "Sh", "Ye", "Do", "Se", "Ch", "Pa", "Jo",
        )

        // https://github.com/techgaun/ad-bs-converter/blob/4731f2c/src/converter.js
        // https://en.wikipedia.org/wiki/Vikram_Samvat
        val nepaliMonths = listOf12Items(
            "बैशाख", "जेष्ठ", "आषाढ", "श्रावण", "भाद्र", "आश्विन",
            "कार्तिक", "मंसिर", "पौष", "माघ", "फाल्गुन", "चैत्र",
        )
        val nepaliMonthsInEnglish = listOf12Items(
            "Baisakh", "Jestha", "Ashadh", "Shrawan", "Bhadra", "Ashwin",
            "Kartik", "Mangsir", "Paush", "Mangh", "Falgun", "Chaitra",
        )

        private val irCodeOrder = listOf("zz", "ir", "tr", "af", "iq")
        private val afCodeOrder = listOf("zz", "af", "ir", "tr", "iq")
        private val arCodeOrder = listOf("zz", "iq", "tr", "ir", "af")
        private val trCodeOrder = listOf("zz", "tr", "ir", "iq", "af")
    }
}
