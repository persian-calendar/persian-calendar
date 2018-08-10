package com.byagowi.persiancalendar.view.preferences

import android.content.Context
import android.util.AttributeSet

import com.byagowi.persiancalendar.R

import androidx.preference.DialogPreference

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
