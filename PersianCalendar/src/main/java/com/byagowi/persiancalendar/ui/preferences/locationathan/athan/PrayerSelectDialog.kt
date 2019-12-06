package com.byagowi.persiancalendar.ui.preferences.locationathan.athan

import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import com.byagowi.persiancalendar.R

class PrayerSelectDialog : PreferenceDialogFragmentCompat() {

    private var prayers: Set<String> = emptySet()

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
        super.onPrepareDialogBuilder(builder)
        if (builder == null) return
        val prayerSelectPreference = preference as PrayerSelectPreference

        val entriesKeys = resources.getStringArray(R.array.prayerTimeKeys)

        prayers = prayerSelectPreference.prayers

        val checked = BooleanArray(entriesKeys.size)
        entriesKeys.indices.forEach { checked[it] = entriesKeys[it] in prayers }

        builder.setMultiChoiceItems(R.array.prayerTimeNames, checked) { _, which, isChecked ->
            prayers = when (isChecked) {
                true -> prayers + entriesKeys[which].toString()
                false -> prayers - entriesKeys[which].toString()
            }
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) (preference as PrayerSelectPreference).prayers = prayers
    }
}
