package com.byagowi.persiancalendar

object AppLocalesData {
    fun getPersianCalendarMonths(locale: String) = when (locale) {
        LANG_FA_AF -> fa_af.persianCalendarMonths
        LANG_PS -> ps.persianCalendarMonths
        LANG_GLK -> glk.persianCalendarMonths
        LANG_AR -> ar.persianCalendarMonths
        LANG_CKB -> ckb.persianCalendarMonths
        LANG_UR -> ur.persianCalendarMonths
        LANG_EN_US -> en.persianCalendarMonths
        LANG_JA -> ja.persianCalendarMonths
        LANG_AZB -> azb.persianCalendarMonths
        LANG_EN_IR, LANG_FA -> fa.persianCalendarMonths
        else -> fa.persianCalendarMonths
    }

    fun getIslamicCalendarMonths(locale: String) = when (locale) {
        LANG_FA_AF -> fa_af.islamicCalendarMonths
        LANG_PS -> ps.islamicCalendarMonths
        LANG_GLK -> glk.islamicCalendarMonths
        LANG_AR -> ar.islamicCalendarMonths
        LANG_CKB -> ckb.islamicCalendarMonths
        LANG_UR -> ur.islamicCalendarMonths
        LANG_EN_US -> en.islamicCalendarMonths
        LANG_JA -> ja.islamicCalendarMonths
        LANG_AZB -> azb.islamicCalendarMonths
        LANG_EN_IR, LANG_FA -> fa.islamicCalendarMonths
        else -> fa.islamicCalendarMonths
    }

    fun getGregorianCalendarMonths(locale: String, isEasternArabicMonth: Boolean) = when (locale) {
        LANG_FA_AF -> fa_af.gregorianCalendarMonths
        LANG_PS -> ps.gregorianCalendarMonths
        LANG_GLK -> glk.gregorianCalendarMonths
        LANG_AR -> {
            if (isEasternArabicMonth) ar.easternGregorianCalendarMonths
            else ar.gregorianCalendarMonths
        }
        LANG_CKB -> ckb.gregorianCalendarMonths
        LANG_UR -> ur.gregorianCalendarMonths
        LANG_EN_US -> en.gregorianCalendarMonths
        LANG_JA -> ja.gregorianCalendarMonths
        LANG_AZB -> azb.gregorianCalendarMonths
        LANG_EN_IR, LANG_FA -> fa.gregorianCalendarMonths
        else -> fa.gregorianCalendarMonths
    }

    fun getWeekDays(locale: String) = when (locale) {
        LANG_FA_AF -> fa_af.weekDays
        LANG_PS -> ps.weekDays
        LANG_GLK -> glk.weekDays
        LANG_AR -> ar.weekDays
        LANG_CKB -> ckb.weekDays
        LANG_UR -> ur.weekDays
        LANG_EN_US -> en.weekDays
        LANG_JA -> ja.weekDays
        LANG_AZB -> azb.weekDays
        LANG_EN_IR, LANG_FA -> fa.weekDays
        else -> fa.weekDays
    }

    fun getWeekDaysInitials(locale: String) = when (locale) {
        LANG_AR -> ar.weekDaysInitials
        LANG_AZB -> azb.weekDaysInitials
        else -> getWeekDays(locale).map { it.substring(0, 1) }
    }

    private object fa {
        val persianCalendarMonths = listOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی",
            "بهمن", "اسفند"
        )
        val islamicCalendarMonths = listOf(
            "مُحَرَّم", "صَفَر", "ربیع‌الاول", "ربیع‌الثانی", "جمادى‌الاولى", "جمادی‌الثانیه",
            "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
        )
        val gregorianCalendarMonths = listOf(
            "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن", "ژوئیه", "اوت", "سپتامبر", "اکتبر",
            "نوامبر", "دسامبر"
        )
        val weekDays = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
    }

    private object fa_af {
        val persianCalendarMonths = listOf(
            "حمل", "ثور", "جوزا", "سرطان", "اسد", "سنبله", "میزان", "عقرب", "قوس", "جدی", "دلو",
            "حوت"
        )
        val islamicCalendarMonths = listOf(
            "مُحَرَّم", "صَفَر", "ربیع‌الاول", "ربیع‌الثانی", "جمادى‌الاولى", "جمادی‌الثانیه",
            "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
        )
        val gregorianCalendarMonths = listOf(
            "جنوری", "فبروری", "مارچ", "اپریل", "می", "جون", "جولای", "آگست", "سپتمبر", "اکتبر",
            "نومبر", "دیسمبر"
        )
        val weekDays = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
    }

    private object ps {
        val persianCalendarMonths = listOf(
            "وری", "غویی", "غبرګولی", "چنګاښ", "زمری", "وږی", "تله", "لړم", "لیندۍ", "مرغومی",
            "سلواغه", "کب"
        )
        val islamicCalendarMonths = listOf(
            "مُحَرَّم", "صَفَر", "ربیع‌الاول", "ربیع‌الثانی", "جمادى‌الاولى", "جمادی‌الثانیه",
            "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
        )
        val gregorianCalendarMonths = listOf(
            "جنوری", "فبروری", "مارچ", "اپریل", "می", "جون", "جولای", "آگست", "سپتمبر", "اکتبر",
            "نومبر", "دیسمبر"
        )
        val weekDays = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
    }

    private object glk {
        val persianCalendarMonths = listOf(
            "فروردین", "اؤردیبهشت", "خؤرداد", "تیر", "مۊرداد", "شاریور", "مهر", "آبان", "آذر",
            "دی", "بهمن", "ايسفند"
        )
        val islamicCalendarMonths = listOf(
            "مُحَرَّم", "صَفَر", "ربيع الأول", "ربیع الثاني", "جمادي الأولى", "جمادي الثانية",
            "رجب", "شعبان", "رمضان", "شوال", "ذؤ القعده", "ذؤ الحجه"
        )
        val gregorianCalendarMonths = listOf(
            "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژۊئن", "ژۊئیه", "اۊت", "سپتامبر", "اؤکتؤبر",
            "نؤوامبر", "دسامبر"
        )
        val weekDays = listOf(
            "شمبه", "یکشمبه", "دۊشمبه", "سه شمبه", "چارشمبه", "پئن شمبه", "جۊما"
        )
    }

    private object ar {
        val persianCalendarMonths = listOf(
            "الحمل", "الثور", "الجوزاء", "السرطان", "الأسد", "السنبلة", "المیزان", "العقرب",
            "القوس", "الجدي", "الدلو", "الحوت"
        )
        val islamicCalendarMonths = listOf(
            "المحرم", "صفر", "ربيع الأول", "ربيع الثاني", "جمادى الاولى", "جمادى الثانية",
            "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )
        val gregorianCalendarMonths = listOf(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر",
            "اكتوبر", "نوفمبر", "ديسمبر"
        )
        val easternGregorianCalendarMonths = listOf(
            "كانون الثاني", "شباط", "آذار", "نيسان", "أيار", "حزيران", "تموز", "آب", "أيلول",
            "تشرين الأول", "تشرين الثاني", "كانون الأول"
        )
        val weekDays = listOf(
            "السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة"
        )
        val weekDaysInitials = listOf("سب", "أح", "اث", "ثل", "أر", "خم", "جم")
    }

    private object ckb {
        val persianCalendarMonths = listOf(
            "خاکەلێوە", "گوڵان", "جۆزەردان", "پووشپەڕ", "گەلاوێژ", "خەرمانان", "ڕەزبەر", "خەزەڵوەر",
            "سەرماوەز", "بەفرانبار", "ڕێبەندان", "ڕەشەمە"
        )
        val islamicCalendarMonths = listOf(
            "موحەڕڕەم", "سەفەر", "ڕەبیعەلئەووەڵ", "ڕەبیعەلئاخیر", "جومادەلئوولا", "جومادەلئاخیر",
            "ڕەجەب", "شەعبان", "ڕەمەزان", "شەووال", "زولقەعدە", "زولحەججە"
        )
        val gregorianCalendarMonths = listOf(
            "جانواری", "فێبرواری", "مارچ", "ئاپریل", "مەی", "جوون", "جولای", "ئۆگست", "سێپتەمبەر",
            "ئۆکتۆبەر", "نۆڤەمبەر", "دیسەمبەر"
        )
        val weekDays = listOf(
            "شەممە", "یەکشەممە", "دووشەممە", "سێشەممە", "چوارشەممە", "پێنجشەممە", "ھەینی"
        )
    }

    private object azb {
        val persianCalendarMonths = listOf(
            "فروردین", "اوردیبهشت", "خورداد", "تیر", "مورداد", "شهریور", "مهر", "آبان", "آذر",
            "دی", "بهمن", "اسفند"
        )
        val islamicCalendarMonths = listOf(
            "موحررم", "صفر", "ربيع الاوول", "ربيع الآخیر", "جمادى الاوول", "جمادى الآخیر", "رجب",
            "شعبان", "رمضان", "شوال", "ذیقعده", "ذیحججه"
        )
        val gregorianCalendarMonths = listOf(
            "ژانویه", "فوریه", "مارس", "آوریل", "مئی", "ژوئن", "ژوئیه", "آقوست", "سپتامبر",
            "اوْکتوبر", "نوْوامبر", "دسامبر"
        )
        val weekDays = listOf(
            "يئل‌گونو", "سۆدگونو", "دۇزگونو", "آراگون", "اوْدگونو", "سۇگونو", "آینی‌گون"
        )
        val weekDaysInitials = listOf("یئل", "سۆد", "دۇز", "آرا", "اوْد", "سۇ", "آینی")
    }

    private object ur {
        val persianCalendarMonths = listOf(
            "فروردی", "اردیبہشت", "خرداد", "تیر", "امرداد", "شہریور", "مہر", "آبان", "آذر",
            "دی", "بہمن", "اسفندر"
        )
        val islamicCalendarMonths = listOf(
            "محرم", "صفر", "ربيع الأول", "ربیع الثاني", "جمادى الأولى", "جمادی الثانية", "رجب",
            "شعبان", "رمضان", "شوال", "ذو القعده", "ذو الحجه"
        )
        val gregorianCalendarMonths = listOf(
            "جنوری", "فروری", "مارچ", "اپریل", "مئی", "جون", "جولائی", "اگست", "ستمبر", "اکتوبر",
            "نومبر", "دسمبر"
        )
        val weekDays = listOf(
            "سنیچر", "اتوار", "پیر", "منگل", "بدھ", "جمعرات", "جمعہ"
        )
    }

    private object en {
        val persianCalendarMonths = listOf(
            "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar", "Mehr", "Aban",
            "Azar", "Dey", "Bahman", "Esfand"
        )
        val islamicCalendarMonths = listOf(
            "Muharram", "Safar", "Rabi' al-awwal", "Rabi' al-Thani", "Jumada al-awwal",
            "Jumada al-Thani", "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qidah",
            "Dhu al-Hijjah"
        )
        val gregorianCalendarMonths = listOf(
            "January", "February", "March", "April", "May", "June", "July", "August", "September",
            "October", "November", "December"
        )
        val weekDays = listOf(
            "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
        )
    }

    private object ja {
        val persianCalendarMonths = listOf(
            "ファルヴァルディーン", "オルディーベヘシト", "ホルダード", "ティール", "モルダード",
            "シャハリーヴァル", "メフル", "アーバーン", "アーザル", "デイ", "バフマン", "エスファンド"
        )
        val islamicCalendarMonths = listOf(
            "ムハッラム", "サファル", "ラビー・ウル・アッワル ", "ラビー・ウッサーニ", "ジュマダル・ウッラー",
            "ジュマダッサーニ", "ラジャブ", "シャバーン", "ラマダーン", "シャッワール", "ズルカーダ",
            "ズルヒッジャ"
        )
        val gregorianCalendarMonths = listOf(
            "1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"
        )
        val weekDays = listOf(
            "土曜日", "日曜日", "月曜日", "火曜日", "水曜日", "木曜日", "金曜日"
        )
    }
}