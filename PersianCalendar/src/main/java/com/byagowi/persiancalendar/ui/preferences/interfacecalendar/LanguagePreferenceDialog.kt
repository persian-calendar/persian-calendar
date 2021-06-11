package com.byagowi.persiancalendar.ui.preferences.interfacecalendar

import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.DEFAULT_WEEK_ENDS
import com.byagowi.persiancalendar.DEFAULT_WEEK_START
import com.byagowi.persiancalendar.LANG_AR
import com.byagowi.persiancalendar.LANG_AZB
import com.byagowi.persiancalendar.LANG_CKB
import com.byagowi.persiancalendar.LANG_EN_IR
import com.byagowi.persiancalendar.LANG_EN_US
import com.byagowi.persiancalendar.LANG_FA
import com.byagowi.persiancalendar.LANG_FA_AF
import com.byagowi.persiancalendar.LANG_GLK
import com.byagowi.persiancalendar.LANG_JA
import com.byagowi.persiancalendar.LANG_PS
import com.byagowi.persiancalendar.LANG_UR
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_PERSIAN_DIGITS
import com.byagowi.persiancalendar.PREF_WEEK_ENDS
import com.byagowi.persiancalendar.PREF_WEEK_START
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.language

fun Fragment.showLanguagePreferenceDialog() {
    val languages = listOf(
        LANG_FA to "فارسی",
        LANG_FA_AF to "دری",
        LANG_PS to "پښتو",
        LANG_CKB to "کوردی",
        LANG_AR to "العربية",
        LANG_GLK to "گيلکي",
        LANG_AZB to "تۆرکجه",
        LANG_UR to "اردو",
        LANG_EN_IR to "English (Iran)",
        LANG_EN_US to "English",
        LANG_JA to "日本語"
    )
    val languageKeys = languages.map { it.first }
    val languageNames = languages.map { it.second }.toTypedArray()
    AlertDialog.Builder(context ?: return)
        .setTitle(R.string.language)
        .setSingleChoiceItems(languageNames, languageKeys.indexOf(language)) { dialog, which ->
            val chosenLanguage = languageKeys[which]
            if (language != chosenLanguage) context?.appPrefs?.changeLanguage(chosenLanguage)
            dialog.cancel()
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}

// Preferences changes be applied automatically when user requests a language change
private fun SharedPreferences.changeLanguage(language: String) = edit {
    putString(PREF_APP_LANGUAGE, language)

    when (language) {
        LANG_UR, LANG_EN_IR, LANG_EN_US, LANG_JA -> false; else -> true
    }.let { putBoolean(PREF_PERSIAN_DIGITS, it) }

    when (language) {
        LANG_FA_AF, LANG_PS -> {
            val currentHolidays = getStringSet(PREF_HOLIDAY_TYPES, null) ?: emptySet()
            if (currentHolidays.isEmpty() || currentHolidays.size == 1 &&
                "iran_holidays" in currentHolidays
            ) putStringSet(PREF_HOLIDAY_TYPES, setOf("afghanistan_holidays"))
        }
        LANG_AZB, LANG_GLK, LANG_FA, LANG_EN_IR -> {
            val currentHolidays = getStringSet(PREF_HOLIDAY_TYPES, null) ?: emptySet()
            if (currentHolidays.isEmpty() || currentHolidays.size == 1
                && "afghanistan_holidays" in currentHolidays
            ) putStringSet(PREF_HOLIDAY_TYPES, setOf("iran_holidays"))
        }
    }

    when (language) {
        LANG_EN_US, LANG_JA, LANG_UR -> {
            putString(PREF_MAIN_CALENDAR_KEY, "GREGORIAN")
            putString(PREF_OTHER_CALENDARS_KEY, "ISLAMIC,SHAMSI")
            putString(PREF_WEEK_START, "1")
            putStringSet(PREF_WEEK_ENDS, setOf("1"))
        }
        LANG_AR -> {
            putString(PREF_MAIN_CALENDAR_KEY, "ISLAMIC")
            putString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,SHAMSI")
            putString(PREF_WEEK_START, DEFAULT_WEEK_START)
            putStringSet(PREF_WEEK_ENDS, DEFAULT_WEEK_ENDS)
        }
        LANG_AZB, LANG_GLK, LANG_FA, LANG_FA_AF, LANG_PS, LANG_EN_IR -> {
            putString(PREF_MAIN_CALENDAR_KEY, "SHAMSI")
            putString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,ISLAMIC")
            putString(PREF_WEEK_START, DEFAULT_WEEK_START)
            putStringSet(PREF_WEEK_ENDS, DEFAULT_WEEK_ENDS)
        }
    }
}
