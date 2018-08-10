package com.byagowi.persiancalendar.view.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.byagowi.persiancalendar.R

class AthanVolumePreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {

  var volume: Int
    get() = getPersistedInt(1)
    set(volume) {
      val wasBlocking = shouldDisableDependents()
      persistInt(volume)
      val isBlocking = shouldDisableDependents()
      if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking)
    }

  init {
    dialogLayoutResource = R.layout.preference_volume
    dialogIcon = null
  }
}
