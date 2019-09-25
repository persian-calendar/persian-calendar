package com.byagowi.persiancalendar.utils

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import com.byagowi.persiancalendar.FONT_PATH

// https://gist.github.com/artem-zinnatullin/7749076
object TypefaceUtils {

    val isCustomFontEnabled: Boolean
        get() = Utils.isArabicDigitSelected() || Utils.isNonArabicScriptSelected()

    /**
     * Using reflection to override default typeface
     * NOTICE: DO NOT FORGET TO SET TYPEFACE FOR APP THEME AS DEFAULT TYPEFACE WHICH WILL BE OVERRIDDEN
     */
    fun overrideFont(defaultFontNameToOverride: String, face: Typeface) {
        try {
            val defaultFontTypefaceField = Typeface::class.java.getDeclaredField(defaultFontNameToOverride)
            defaultFontTypefaceField.isAccessible = true
            defaultFontTypefaceField.set(null, face)
        } catch (e: Exception) {
            Log.e("TAG", "Can not set custom font $face instead of $defaultFontNameToOverride")
        }

    }

    fun getAppFont(context: Context): Typeface = Typeface.createFromAsset(context.assets, FONT_PATH)

    fun getCalendarFragmentFont(context: Context): Typeface =
            if (isCustomFontEnabled)
                Typeface.create("sans-serif-light", Typeface.NORMAL)
            else
                getAppFont(context)
}
