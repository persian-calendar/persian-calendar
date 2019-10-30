package com.byagowi.persiancalendar.ui.preferences.locationathan.numeric

import android.content.Context
import android.util.AttributeSet

import androidx.preference.EditTextPreference
import java.lang.Exception

class NumericPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    EditTextPreference(context, attrs) {

    private var mDouble = .0

    override fun getText(): String = mDouble.toString()

    // http://stackoverflow.com/a/10848393
    override fun setText(text: String?) {
        val wasBlocking = shouldDisableDependents()
        mDouble = text?.toDoubleOrNull() ?: .0
        persistString(mDouble.toString())
        val isBlocking = shouldDisableDependents()
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking)
    }
}
