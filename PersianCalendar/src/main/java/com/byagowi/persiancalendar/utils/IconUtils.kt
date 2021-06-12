package com.byagowi.persiancalendar.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.graphics.drawable.IconCompat
import com.byagowi.persiancalendar.ARABIC_DIGITS
import com.byagowi.persiancalendar.ARABIC_INDIC_DIGITS
import com.byagowi.persiancalendar.DAYS_ICONS_ARABIC
import com.byagowi.persiancalendar.DAYS_ICONS_ARABIC_INDIC
import com.byagowi.persiancalendar.DAYS_ICONS_PERSIAN

fun getDayIconResource(day: Int): Int = when (preferredDigits) {
    ARABIC_DIGITS -> DAYS_ICONS_ARABIC
    ARABIC_INDIC_DIGITS -> DAYS_ICONS_ARABIC_INDIC
    else -> DAYS_ICONS_PERSIAN
}.getOrNull(day - 1) ?: 0

fun createStatusIcon(context: Context, dayOfMonth: Int): IconCompat {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.textSize = when (preferredDigits) {
        ARABIC_DIGITS -> 75f; else -> 90f
    }
    paint.textAlign = Paint.Align.CENTER
    paint.typeface = getAppFont(context)
    val text = formatNumber(dayOfMonth)
    val bounds = Rect()
    paint.color = Color.WHITE
    paint.getTextBounds(text, 0, text.length, bounds)
    val bitmap = Bitmap.createBitmap(90, 90, Bitmap.Config.ARGB_8888)
    Canvas(bitmap).drawText(text, 45f, 45 + bounds.height() / 2f, paint)
    return IconCompat.createWithBitmap(bitmap)
}
