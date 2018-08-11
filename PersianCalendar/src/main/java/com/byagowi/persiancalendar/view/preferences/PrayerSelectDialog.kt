package com.byagowi.persiancalendar.view.preferences

import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import com.byagowi.persiancalendar.R

class PrayerSelectDialog : PreferenceDialogFragmentCompat() {

  private lateinit var prayers: MutableSet<String>

  override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
    super.onPrepareDialogBuilder(builder)
    val prayerspref = preference as PrayerSelectPreference

    val entriesKeys = resources.getStringArray(R.array.prayerTimeKeys)

    prayers = prayerspref.prayers.toMutableSet()

    val checked = BooleanArray(entriesKeys.size)
    for (i in entriesKeys.indices) {
      checked[i] = prayers.contains(entriesKeys[i])
    }

    builder?.setMultiChoiceItems(R.array.prayerTimeNames, checked) { _, which, isChecked ->
      if (isChecked) {
        prayers.add(entriesKeys[which].toString())
      } else {
        prayers.remove(entriesKeys[which].toString())
      }
    }
  }

  override fun onDialogClosed(positiveResult: Boolean) {
    if (positiveResult) {
      (preference as PrayerSelectPreference).prayers = prayers
    }
  }
}
