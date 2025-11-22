package com.byagowi.persiancalendar.entities

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.AFGHANISTAN_TIMEZONE_ID
import com.byagowi.persiancalendar.AU_IN_KM
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.NEPAL_TIMEZONE_ID
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac
import com.byagowi.persiancalendar.ui.astronomy.LunarAge
import com.byagowi.persiancalendar.ui.map.MapType
import com.byagowi.persiancalendar.utils.debugLog
import com.byagowi.persiancalendar.utils.listOf12Items
import com.byagowi.persiancalendar.utils.listOf7Items
import com.byagowi.persiancalendar.utils.logException
import io.github.cosinekitty.astronomy.EclipseKind
import io.github.persiancalendar.praytimes.CalculationMethod
import org.jetbrains.annotations.VisibleForTesting
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToLong


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
    DE("de", "Deutsch"),
    EN_IR("en", "English (Iran)"),
    EN_US("en-US", "English"),
    ES("es", "Español"),
    FR("fr", "Français"),
    GLK("glk", "گيلکي"),
    IT("it", "Italiano"),
    JA("ja", "日本語"),
    KMR("kmr", "Kurdî"),
    NE("ne", "नेपाली"),
    OTA("ota", "عثمانى"),
    PT("pt", "Português"),
    RU("ru", "Русский"),
    TA("ta", "தமிழ்"),
    TG("tg", "Тоҷикӣ"),
    TR("tr", "Türkçe"),
    UR("ur", "اردو"),
    ZH_CN("zh-CN", "中文");

    val isArabic get() = this == AR
    val isDari get() = this == FA_AF
    val isPersian get() = this == FA
    val isPersianOrDari get() = isPersian || isDari
    val isNepali get() = this == NE
    val isTamil get() = this == TA

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
            CKB -> $$"%1$sی %2$sی %3$s"
            ZH_CN -> $$"%3$s 年 %2$s %1$s 日"
            else -> $$"%1$s %2$s %3$s"
        }
    val dm: String
        get() = when (this) {
            CKB -> $$"%1$sی %2$s"
            JA, ZH_CN -> $$"%2$s %1$s"
            EN_US -> $$"%2$s %1$s"
            else -> $$"%1$s %2$s"
        }
    val my: String
        get() = when (this) {
            CKB -> $$"%1$sی %2$s"
            ZH_CN -> $$"%2$s 年 %1$s"
            else -> $$"%1$s %2$s"
        }
    val timeAndDateFormat: String
        get() = when (this) {
            JA, ZH_CN -> $$"%2$s %1$s"
            else -> $$"%1$s$$spacedComma%2$s"
        }
    val clockAmPmOrder: String
        get() = when (this) {
            ZH_CN -> $$"%2$s %1$s"
            else -> $$"%1$s %2$s"
        }

    val isLessKnownRtl: Boolean
        get() = when (this) {
            AZB, GLK, OTA -> true
            else -> false
        }

    val betterToUseShortCalendarName: Boolean
        get() = when (this) {
            EN_US, EN_IR, JA, ZH_CN, FR, ES, DE, PT, IT, AR, TR, TG, RU, CKB -> true
            else -> false
        }

    val mightPreferUmmAlquraIslamicCalendar: Boolean
        get() = when (this) {
            FA_AF, PS, UR, AR, CKB, EN_US, JA, ZH_CN, FR, ES, DE, PT, IT, TR, KMR, TA, TG, NE, RU -> true
            else -> false
        }

    val preferredCalculationMethod: CalculationMethod
        get() = when (this) {
            FA_AF, PS, UR, AR, CKB, TR, KMR, TG, NE, TA -> CalculationMethod.MWL
            else -> CalculationMethod.Tehran
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
            EN_US, JA, ZH_CN, FR, ES, DE, PT, IT, RU, TR, KMR, EN_IR, TG, NE, TA -> false
            else -> true
        }

    // Whether locale would prefer local digits like ۱۲۳ over the global ones, 123, initially at least
    val prefersLocalNumeral: Boolean
        get() = when (this) {
            UR, EN_IR, EN_US, JA, ZH_CN, FR, ES, DE, PT, IT, RU, TR, KMR, TG -> false
            else -> true
        }

    // Whether the language doesn't need " and " between date parts or not
    val languagePrefersHalfSpaceAndInDates: Boolean
        get() = when (this) {
            JA, ZH_CN -> true
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
            EN_US, JA, ZH_CN, FR, ES, DE, PT, IT, RU, UR, TR, KMR, TG, TA -> true
            else -> false
        }

    private val prefersNepaliCalendar: Boolean
        get() = isNepali

    // We can presume user would prefer Gregorian calendar at least initially
    private val prefersIslamicCalendar: Boolean
        get() = when (this) {
            AR, OTA -> true
            else -> false
        }

    // We can presume user would prefer Gregorian calendar at least initially
    private val prefersPersianCalendar: Boolean
        get() = when (this) {
            AZB, GLK, FA, FA_AF, PS, EN_IR -> true
            else -> false
        }

    val defaultCalendars
        get() = when {
            this == FA -> listOf(Calendar.SHAMSI, Calendar.GREGORIAN, Calendar.ISLAMIC)
            prefersGregorianCalendar -> listOf(
                Calendar.GREGORIAN, Calendar.ISLAMIC, Calendar.SHAMSI
            )

            prefersIslamicCalendar -> listOf(Calendar.ISLAMIC, Calendar.GREGORIAN, Calendar.SHAMSI)
            prefersPersianCalendar -> listOf(Calendar.SHAMSI, Calendar.GREGORIAN, Calendar.ISLAMIC)
            prefersNepaliCalendar -> listOf(Calendar.NEPALI, Calendar.GREGORIAN)
            else -> listOf(Calendar.SHAMSI, Calendar.GREGORIAN, Calendar.ISLAMIC)
        }

    val defaultWeekStart
        get() = when (this) {
            FA, FA_AF, PS, AR, AZB, CKB, EN_IR, GLK, OTA -> WeekDay.SATURDAY
            EN_US -> WeekDay.SUNDAY
            JA, ZH_CN, FR, ES, DE, PT, IT, RU, UR, TR, KMR, TG, TA, NE -> WeekDay.MONDAY
        }
    val defaultWeekStartAsString get() = defaultWeekStart.ordinal.toString()

    val defaultWeekEnds
        get() = when {
            this == FA || isIranExclusive -> setOf(WeekDay.FRIDAY)
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

    fun getGregorianMonths(resources: Resources, alternativeGregorianMonths: Boolean) =
        when (this) {
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
        else -> WeekDay.stringIds.map { resources.getString(it) }
    }

    fun getWeekDaysInitials(resources: Resources): List<String> = when (this) {
        FA, FA_AF -> weekDaysInitialsInPersian
        EN_IR -> weekDaysInitialsInEnglishIran
        else -> WeekDay.shortStringIds.map(resources::getString)
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

    // Too hard to translate and don't want to disappoint translators thus not in the common
    // i18n system yet
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

    fun tryTranslateAthanVibrationSummary() = when (this) {
        EN_US, EN_IR -> "Enable vibrator in the beginning of athan"
        FA, FA_AF -> "فعال‌سازی لرزش در ابتدای پخش اذان"
        else -> null
    }

    // https://en.wikipedia.org/wiki/List_of_date_formats_by_country
    fun allNumericsDateFormat(year: Int, month: Int, dayOfMonth: Int, numeral: Numeral): String {
        val sep = when (this) {
            PS, NE -> '-'
            KMR, RU, TR, DE -> '.'
            else -> '/'
        }
        val needsZeroPad = when (this) {
            FR, IT, NE, PT, RU, TR, KMR -> true
            // According to the dated on the first page of https://calendar.ut.ac.ir/documents/2139738/7092644/Calendar-1404.pdf
            // It seems it's a zero padded locale
            // But according to نیازهای شرایط محلی برای زبان فارسی ایران it's not
            // https://drive.google.com/file/d/1yDoUbXnV_q6mrzzaRZK_AvsOLaU-O9Qy/view
            FA -> false
            else -> false
        }
        val format = when (this) {
            // Year major
            FA, AZB, GLK, FA_AF, EN_IR, PS, JA, NE, ZH_CN, OTA -> $$"%1$s$$sep%2$s$$sep%3$s"
            // Month major
            EN_US -> $$"%2$s$$sep%3$s$$sep%1$s"
            // Day major, most likely everything else goes here but check via JS'
            // new Date().toLocaleDateString('XX')
            AR, CKB, ES, DE, FR, IT, KMR, PT, RU, TG, TR, UR, TA -> $$"%3$s$$sep%2$s$$sep%1$s"
        }
        return format.format(
            numeral.format(year), numeral.format(
                "$month".let { if (needsZeroPad) it.padStart(2, '0') else it }), numeral.format(
                "$dayOfMonth".let { if (needsZeroPad) it.padStart(2, '0') else it })
        )
    }

    fun moonNames(lunarAge: LunarAge.Phase): String {
        return when (this) {
            // https://commons.wikimedia.org/wiki/File:Lunar-Phase-Diagram-Parsi.png
            FA, FA_AF -> when (lunarAge) {
                LunarAge.Phase.NEW_MOON -> "ماه نو یا بَرن"
                LunarAge.Phase.WAXING_CRESCENT -> "هلال سوی ماه تمام"
                LunarAge.Phase.FIRST_QUARTER -> "یک‌چهارم نخست"
                LunarAge.Phase.WAXING_GIBBOUS -> "برآمدگی سوی ماه تمام"
                LunarAge.Phase.FULL_MOON -> "ماه تمام یا بدر"
                LunarAge.Phase.WANING_GIBBOUS -> "برآمدگی سوی کمرنگی"
                LunarAge.Phase.THIRD_QUARTER -> "یک‌چهارم سوم"
                LunarAge.Phase.WANING_CRESCENT -> "هلال سوی کمرنگی"
            }

            NE -> when (lunarAge) {
                LunarAge.Phase.NEW_MOON -> "औंशी"
                LunarAge.Phase.WAXING_CRESCENT -> "शुक्ल पक्ष प्रतिपदा"
                LunarAge.Phase.FIRST_QUARTER -> "शुक्ल पक्ष पञ्चमी"
                LunarAge.Phase.WAXING_GIBBOUS -> "शुक्ल पक्ष चतुर्दशी"
                LunarAge.Phase.FULL_MOON -> "पूर्णिमा"
                LunarAge.Phase.WANING_GIBBOUS -> "कृष्ण पक्ष चतुर्दशीर"
                LunarAge.Phase.THIRD_QUARTER -> "कृष्ण पक्ष पञ्चमी"
                LunarAge.Phase.WANING_CRESCENT -> "कृष्ण पक्ष प्रतिपदा"
            }

            TA -> when (lunarAge) {
                LunarAge.Phase.NEW_MOON -> "இல்மதி"
                LunarAge.Phase.WAXING_CRESCENT -> "மூன்றாம்பிறை"
                LunarAge.Phase.FIRST_QUARTER -> "முதல் கால்பகுதி"
                LunarAge.Phase.WAXING_GIBBOUS -> "வளர்பிறை"
                LunarAge.Phase.FULL_MOON -> "முழுமதி"
                LunarAge.Phase.WANING_GIBBOUS -> "தேய்பிறை"
                LunarAge.Phase.THIRD_QUARTER -> "மூன்றாம் கால்பகுதி"
                LunarAge.Phase.WANING_CRESCENT -> "தேய் மூன்றாம் பிறை"
            }

            else -> when (lunarAge) {
                LunarAge.Phase.NEW_MOON -> "New moon"
                LunarAge.Phase.WAXING_CRESCENT -> "Waxing crescent"
                LunarAge.Phase.FIRST_QUARTER -> "First quarter"
                LunarAge.Phase.WAXING_GIBBOUS -> "Waxing gibbous"
                LunarAge.Phase.FULL_MOON -> "Full moon"
                LunarAge.Phase.WANING_GIBBOUS -> "Waning gibbous"
                LunarAge.Phase.THIRD_QUARTER -> "Third quarter"
                LunarAge.Phase.WANING_CRESCENT -> "Waning crescent"
            }
        }
    }

    fun formatCompatibility(compatibility: ChineseZodiac.Compatibility): String {
        return when {
            isPersianOrDari -> when (compatibility) {
                ChineseZodiac.Compatibility.BEST -> "بهترین"
                ChineseZodiac.Compatibility.BETTER -> "خوب"
                ChineseZodiac.Compatibility.NEUTRAL -> "خنثی"
                ChineseZodiac.Compatibility.WORSE -> "بد"
                ChineseZodiac.Compatibility.WORST -> "بدترین"
            }

            this == ZH_CN -> when (compatibility) {
                ChineseZodiac.Compatibility.BEST -> "三合" // Sān Hé
                ChineseZodiac.Compatibility.BETTER -> "六合" // Liù Hé
                ChineseZodiac.Compatibility.NEUTRAL -> "三會" // Sān Huì
                ChineseZodiac.Compatibility.WORSE -> "六沖" // Liù Chōng
                ChineseZodiac.Compatibility.WORST -> "口舌" // Kǒu Shé
            }

            else -> when (compatibility) {
                ChineseZodiac.Compatibility.BEST -> "Best"
                ChineseZodiac.Compatibility.BETTER -> "Better"
                ChineseZodiac.Compatibility.NEUTRAL -> "Neutral"
                ChineseZodiac.Compatibility.WORSE -> "Worse"
                ChineseZodiac.Compatibility.WORST -> "Worst"
            }
        }
    }

    // Indian locales always need to see moon view as https://en.wikipedia.org/wiki/Tithi
    val alwaysNeedMoonState: Boolean
        get() = when (this) {
            NE, TA -> true
            else -> false
        }

    fun formatAuAsKm(value: Double): String = formatKm((value * AU_IN_KM).roundToLong())
    fun formatKm(value: Long): String = preferredNumeral.formatLongNumber(value) + " " + kilometer

    fun mapType(mapType: MapType): String? {
        return when (this) {
            FA, FA_AF -> when (mapType) {
                MapType.NONE -> null
                MapType.DAY_NIGHT -> "تاریکی شب"
                MapType.MOON_VISIBILITY -> "پدیداری ماه"
                MapType.MAGNETIC_FIELD_STRENGTH -> "قدرت میدان مغناطیسی"
                MapType.MAGNETIC_DECLINATION -> "انحراف مغناطیسی"
                MapType.MAGNETIC_INCLINATION -> "میل مغناطیسی"
                MapType.TIME_ZONES -> null
                MapType.TECTONIC_PLATES -> "صفحه‌های زمین‌ساخت/تکتونیک"
                MapType.EVENING_YALLOP -> null
                MapType.EVENING_ODEH -> null
                MapType.MORNING_YALLOP -> null
                MapType.MORNING_ODEH -> null
            }

            else -> null
        }
    }

    fun mapButtons(stringId: Int): String? {
        return when (this) {
            FA, FA_AF -> when (stringId) {
                R.string.show_globe_view_label -> "کرهٔ سه‌بعدی"
                R.string.show_direct_path_label -> "مسیر مستقیم"
                R.string.show_grid_label -> "توری"
                R.string.show_my_location_label -> "مکان‌یاب / GPS"
                R.string.show_location_label -> "مکان"
                R.string.show_night_mask_label -> "تاریکی شب"
                else -> null
            }

            else -> null
        }
    }

    val inch
        get() = when {
            isArabicScript -> "اینچ"
            else -> "in"
        }

    val centimeter
        get() = when {
            isArabicScript -> "سانتی‌متر"
            else -> "cm"
        }

    val kilometer
        get() = when {
            isArabicScript -> "کیلومتر"
            else -> "km"
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

                "en", EN_US.code -> guessLanguageFromTimezoneId() ?: guessLanguageFromKeyboards(
                    context
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
                        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            submethod.languageTag
                        } else @Suppress("DEPRECATION") submethod.locale
                        debugLog("Language: '$locale' is available in keyboards")
                        if (locale.isEmpty()) return@forEach
                        val language = valueOfLanguageCode(locale) ?: valueOfLanguageCode(
                            locale.split("-").firstOrNull().orEmpty()
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

        // These are special cases and new ones should be translated in strings.xml of the language
        private val persianCalendarMonthsInPersian = listOf12Items(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد",
            "شهریور", "مهر", "آبان", "آذر", "دی",
            "بهمن", "اسفند"
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
            "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
        )
        private val gregorianCalendarMonthsInPersian = listOf12Items(
            "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
            "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
        )
        val persianCalendarMonthsInDariOrPersianOldEra = listOf12Items(
            "حمل", "ثور", "جوزا", "سرطان", "اسد", "سنبله",
            "میزان", "عقرب", "قوس", "جدی", "دلو", "حوت"
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
            "جولای", "آگوست", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
        )
        private val easternGregorianCalendarMonths = listOf12Items(
            "كانون الثاني", "شباط", "آذار", "نيسان", "أيار", "حزيران", "تموز", "آب", "أيلول",
            "تشرين الأول", "تشرين الثاني", "كانون الأول"
        )
        private val weekDaysInPersian = listOf7Items(
            // https://apll.ir/wp-content/uploads/2018/10/D-1394.pdf
            // advices to use یکشنبه and پنجشنبه on "مرکبهایی که بسیطگونه است"
            // The updated version https://www.ekhtebar.ir/wp-content/uploads/2023/07/Dastour-e-Khat-17.04.1402-3.pdf
            // doesn't have it though
            "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
        )
        private val weekDaysInitialsInPersian = listOf7Items(
            "ش", "ی", "د", "س", "چ", "پ", "ج"
        )
        private val weekDaysInEnglishIran = listOf7Items(
            "Shanbe", "Yekshanbe", "Doshanbe", "Seshanbe", "Chahaarshanbe",
            "Panjshanbe", "Jom'e"
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
    }
}
