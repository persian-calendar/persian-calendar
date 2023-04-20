package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.AFGHANISTAN_TIMEZONE_ID
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.saveLanguage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.TimeZone

fun showLanguagePreferenceDialog(activity: FragmentActivity) {
    val languages = enumValues<Language>().toList().let { languages ->
        if (TimeZone.getDefault().id in listOf(IRAN_TIMEZONE_ID, AFGHANISTAN_TIMEZONE_ID)) languages
        else languages.sortedBy { it.code }
    }
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
