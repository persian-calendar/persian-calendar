package com.byagowi.persiancalendar.view.preferences

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.preference.DialogPreference
import java.util.*

class PrayerSelectPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {
  // convert comma separated string to a set
  // convert set to a comma separated string
  var prayers: Set<String>
    get() = hashSetOf(*TextUtils.split(getPersistedString(""), ","))
    set(prayers) {
      val wasBlocking = shouldDisableDependents()
      persistString(TextUtils.join(",", prayers))
      val isBlocking = shouldDisableDependents()
      if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking)
    }
}
