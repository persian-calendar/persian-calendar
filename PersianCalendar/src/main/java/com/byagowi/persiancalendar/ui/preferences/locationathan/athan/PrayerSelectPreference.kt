package com.byagowi.persiancalendar.ui.preferences.locationathan.athan

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.byagowi.persiancalendar.utils.splitIgnoreEmpty

class PrayerSelectPreference(context: Context, attrs: AttributeSet?) :
    DialogPreference(context, attrs) {
    // convert comma separated string to a set
    // convert set to a comma separated string
    var prayers: Set<String>
        get() = getPersistedString("").splitIgnoreEmpty(",").toSet()
        set(prayers) {
            val wasBlocking = shouldDisableDependents()
            persistString(TextUtils.join(",", prayers))
            val isBlocking = shouldDisableDependents()
            if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking)
        }
}
