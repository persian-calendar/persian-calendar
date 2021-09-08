package com.byagowi.persiancalendar.ui.preferences.interfacecalendar

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.language

fun showLanguagePreferenceDialog(context: Context) {
    val languages = Language.values().toList()
    val names = languages.map { it.nativeName }.toTypedArray()
    AlertDialog.Builder(context)
        .setTitle(R.string.language)
        .setSingleChoiceItems(names, languages.indexOf(language)) { dialog, which ->
            val chosenLanguage = languages[which]
            if (language != chosenLanguage) changeLanguage(context.appPrefs, chosenLanguage)
            dialog.cancel()
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
