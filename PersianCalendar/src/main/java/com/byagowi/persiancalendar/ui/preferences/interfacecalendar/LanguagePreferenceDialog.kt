package com.byagowi.persiancalendar.ui.preferences.interfacecalendar

import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.language

fun Fragment.showLanguagePreferenceDialog() {
    val languageNames = resources.getStringArray(R.array.languageNames)
    val languageKeys = resources.getStringArray(R.array.languageKeys)
    AlertDialog.Builder(layoutInflater.context)
        .setTitle(R.string.language)
        .setSingleChoiceItems(languageNames, languageKeys.indexOf(language)) { _, which ->
            context?.appPrefs?.edit { putString(PREF_APP_LANGUAGE, languageKeys[which]) }
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
