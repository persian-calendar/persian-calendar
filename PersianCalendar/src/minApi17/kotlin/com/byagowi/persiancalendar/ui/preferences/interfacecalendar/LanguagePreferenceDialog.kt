package com.byagowi.persiancalendar.ui.preferences.interfacecalendar

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.language
import com.byagowi.persiancalendar.utils.saveLanguage

fun showLanguagePreferenceDialog(activity: Activity) {
    val languages = Language.values().toList()
    val names = languages.map { it.nativeName }.toTypedArray()
    AlertDialog.Builder(activity)
        .setTitle(R.string.language)
        .setSingleChoiceItems(names, languages.indexOf(language)) { dialog, which ->
            val chosenLanguage = languages[which]
            if (language != chosenLanguage) activity.appPrefs.saveLanguage(chosenLanguage)
            dialog.cancel()
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
