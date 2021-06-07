package com.byagowi.persiancalendar.ui.preferences.interfacecalendar

import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
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
        .setSingleChoiceItems(languageNames, languageKeys.indexOf(language)) { _, which ->
            context?.appPrefs?.edit { putString(PREF_APP_LANGUAGE, languageKeys[which]) }
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
