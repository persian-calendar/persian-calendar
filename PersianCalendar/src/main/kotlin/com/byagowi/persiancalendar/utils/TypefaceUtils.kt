package com.byagowi.persiancalendar.utils

import android.content.Context
import android.graphics.Typeface
import com.byagowi.persiancalendar.FONT_PATH
import com.byagowi.persiancalendar.global.language

// https://gist.github.com/artem-zinnatullin/7749076

/**
 * Using reflection to override default typeface
 * NOTICE: DO NOT FORGET TO SET TYPEFACE FOR APP THEME AS DEFAULT TYPEFACE WHICH WILL BE OVERRIDDEN
 */
fun overrideFont(defaultFontNameToOverride: String, face: Typeface): Unit = runCatching {
    val defaultFontTypefaceField =
        Typeface::class.java.getDeclaredField(defaultFontNameToOverride)
    defaultFontTypefaceField.isAccessible = true
    defaultFontTypefaceField.set(null, face)
}.onFailure(logException).let {}

fun getAppFont(context: Context): Typeface = Typeface.createFromAsset(context.assets, FONT_PATH)

fun getCalendarFragmentFont(context: Context): Typeface =
    if (!language.isArabicScript) Typeface.create("sans-serif", Typeface.NORMAL)
    else getAppFont(context)
