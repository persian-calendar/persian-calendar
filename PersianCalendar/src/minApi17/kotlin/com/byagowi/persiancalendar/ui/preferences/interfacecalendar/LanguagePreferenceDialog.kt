package com.byagowi.persiancalendar.ui.preferences.interfacecalendar

import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.saveLanguage
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showLanguagePreferenceDialog(activity: FragmentActivity) {
    val languages = Language.values().toList()
    val names = languages.map { it.nativeName }.toTypedArray()
    MaterialAlertDialogBuilder(activity)
        .setTitle(R.string.language)
        .setSingleChoiceItems(names, languages.indexOf(language)) { dialog, which ->
            val chosenLanguage = languages[which]
            if (language != chosenLanguage) activity.appPrefs.saveLanguage(chosenLanguage)
            dialog.cancel()
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
