package com.byagowi.persiancalendar.ui.preferences.locationathan.numeric

import android.content.Context
import android.util.AttributeSet

import androidx.preference.EditTextPreference

/**
 * Created by ebraminio on 2/16/16.
 */
class NumericPreference : EditTextPreference {

    private var mDouble: Double? = null

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)

    override fun getText(): String? = mDouble.toString()

    // http://stackoverflow.com/a/10848393
    override fun setText(text: String?) {
        val wasBlocking = shouldDisableDependents()
        mDouble = text?.let { parseDouble(it) }
        persistString(mDouble.toString())
        val isBlocking = shouldDisableDependents()
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking)
    }

    private fun parseDouble(text: String): Double? {
        return try {
            java.lang.Double.parseDouble(text)
        } catch (e: NumberFormatException) {
            null
        } catch (e: NullPointerException) {
            null
        }

    }
}
