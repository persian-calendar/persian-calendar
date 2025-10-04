package com.byagowi.persiancalendar.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.preferredDigits

/**
 * Day icon utilities — improved and more robust.
 *
 * Changes and improvements:
 * - Avoids use of undefined globals (`isArabicDigitSelected`, `isTamilDigitSelected`) and
 *   uses `preferredDigits` (project-defined) which is safer and deterministic.
 * - Creates visually richer bitmaps (rounded background + centered text) for previews.
 * - Adds defensive checks and clear contracts for helper functions.
 */

private const val DEFAULT_ICON_SIZE = 90

private fun textSizeForDigitsStyle(digits: Language, textLength: Int, baseSize: Int): Float = when (digits) {
    Language.ARABIC_INDIC_DIGITS -> baseSize * 0.95f
    Language.ARABIC_DIGITS -> if (textLength == 1) baseSize * 0.85f else baseSize * 0.5f
    Language.TAMIL_DIGITS -> if (textLength == 1) baseSize * 0.8f else baseSize * 0.45f
    Language.DEVANAGARI_DIGITS -> baseSize * 0.9f
    else -> if (textLength == 1) baseSize * 1.0f else baseSize * 0.6f
}

// Dynamic icon generation with a pleasant rounded background and centered number.
fun createStatusIcon(dayOfMonth: Int, size: Int = DEFAULT_ICON_SIZE): Bitmap {
    val text = formatNumber(dayOfMonth)
    val digitsStyle = preferredDigits
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = textSizeForDigitsStyle(digitsStyle, text.length, (size * 0.55f).toInt())
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    val bmp = createBitmap(size, size)
    bmp.applyCanvas {
        // draw rounded gradient background
        val canvas = this
        val radius = size * 0.15f
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bgPaint.shader = LinearGradient(
            0f,
            0f,
            size.toFloat(),
            size.toFloat(),
            Color.parseColor("#5B8DEF"),
            Color.parseColor("#4CA1FF"),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(0f, 0f, size.toFloat(), size.toFloat(), radius, radius, bgPaint)

        // optional subtle circle highlight
        val highlight = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(24, 255, 255, 255) }
        canvas.drawCircle(size * 0.25f, size * 0.25f, size * 0.38f, highlight)

        // draw centered text
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val x = size / 2f
        val y = size / 2f + bounds.height() / 2f
        canvas.drawText(text, x, y, paint)
    }
    return bmp
}

/**
 * Return a drawable resource id for day icons based on preferred digits (legacy assets).
 * If the requested day is out of range, returns 0.
 */
fun getDayIconResource(day: Int): Int = when (preferredDigits) {
    Language.DEVANAGARI_DIGITS, Language.ARABIC_DIGITS, Language.TAMIL_DIGITS -> DAYS_ICONS_ARABIC.getOrNull(day - 1)
    Language.ARABIC_INDIC_DIGITS -> DAYS_ICONS_ARABIC_INDIC.getOrNull(day - 1)
    else -> DAYS_ICONS_PERSIAN.getOrNull(day - 1)
} ?: 0

// Legacy lists (these reference drawable resources in the project)
private val DAYS_ICONS_PERSIAN = listOf31Items(
    R.drawable.day1, R.drawable.day2, R.drawable.day3, R.drawable.day4, R.drawable.day5,
    R.drawable.day6, R.drawable.day7, R.drawable.day8, R.drawable.day9, R.drawable.day10,
    R.drawable.day11, R.drawable.day12, R.drawable.day13, R.drawable.day14, R.drawable.day15,
    R.drawable.day16, R.drawable.day17, R.drawable.day18, R.drawable.day19, R.drawable.day20,
    R.drawable.day21, R.drawable.day22, R.drawable.day23, R.drawable.day24, R.drawable.day25,
    R.drawable.day26, R.drawable.day27, R.drawable.day28, R.drawable.day29, R.drawable.day30,
    R.drawable.day31
)

private val DAYS_ICONS_ARABIC = listOf31Items(
    R.drawable.day1_ar, R.drawable.day2_ar, R.drawable.day3_ar, R.drawable.day4_ar,
    R.drawable.day5_ar, R.drawable.day6_ar, R.drawable.day7_ar, R.drawable.day8_ar,
    R.drawable.day9_ar, R.drawable.day10_ar, R.drawable.day11_ar, R.drawable.day12_ar,
    R.drawable.day13_ar, R.drawable.day14_ar, R.drawable.day15_ar, R.drawable.day16_ar,
    R.drawable.day17_ar, R.drawable.day18_ar, R.drawable.day19_ar, R.drawable.day20_ar,
    R.drawable.day21_ar, R.drawable.day22_ar, R.drawable.day23_ar, R.drawable.day24_ar,
    R.drawable.day25_ar, R.drawable.day26_ar, R.drawable.day27_ar, R.drawable.day28_ar,
    R.drawable.day29_ar, R.drawable.day30_ar, R.drawable.day31_ar
)

private val DAYS_ICONS_ARABIC_INDIC = listOf31Items(
    R.drawable.day1, R.drawable.day2, R.drawable.day3, R.drawable.day4_ckb, R.drawable.day5_ckb,
    R.drawable.day6_ckb, R.drawable.day7, R.drawable.day8, R.drawable.day9, R.drawable.day10,
    R.drawable.day11, R.drawable.day12, R.drawable.day13, R.drawable.day14_ckb,
    R.drawable.day15_ckb, R.drawable.day16_ckb, R.drawable.day17, R.drawable.day18,
    R.drawable.day19, R.drawable.day20, R.drawable.day21, R.drawable.day22, R.drawable.day23,
    R.drawable.day24_ckb, R.drawable.day25_ckb, R.drawable.day26_ckb, R.drawable.day27,
    R.drawable.day28, R.drawable.day29, R.drawable.day30, R.drawable.day31
)

// --- Helpful utilities (pure Kotlin) ---

/** Returns true when the provided day is within 1..31. */
fun isValidDay(day: Int): Boolean = day in 1..31

/** Returns a short, localizable day label. */
fun getDayName(day: Int): String = if (isValidDay(day)) "Day $day" else "Invalid Day"

/**
 * Picks an icon resource for a given language explicitly (useful for previewing alternate icons).
 * Returns 0 when not available.
 */
fun getLocalizedDayIcon(day: Int, language: Language): Int = when (language) {
    Language.DEVANAGARI_DIGITS, Language.ARABIC_DIGITS, Language.TAMIL_DIGITS -> DAYS_ICONS_ARABIC.getOrNull(day - 1)
    Language.ARABIC_INDIC_DIGITS -> DAYS_ICONS_ARABIC_INDIC.getOrNull(day - 1)
    else -> DAYS_ICONS_PERSIAN.getOrNull(day - 1)
} ?: 0

/**
 * Create a simple preview bitmap (black text on white) for quick layouts.
 */
fun createDayPreview(day: Int, size: Int = 100): Bitmap {
    val text = formatNumber(day)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size * 0.4f
        textAlign = Paint.Align.CENTER
        color = Color.BLACK
    }
    val bmp = createBitmap(size, size)
    bmp.applyCanvas {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val x = size / 2f
        val y = size / 2f + bounds.height() / 2f
        drawColor(Color.WHITE)
        drawText(text, x, y, paint)
    }
    return bmp
}

/**
 * Choose a pleasant color for a day number. Use a small palette and map by modulo to remain stable.
 */
fun getDayColor(day: Int): Int {
    val palette = listOf(
        Color.parseColor("#FF7043"), // deep orange
        Color.parseColor("#4CAF50"), // green
        Color.parseColor("#42A5F5"), // blue
        Color.parseColor("#AB47BC"), // purple
        Color.parseColor("#FFCA28"), // amber
        Color.parseColor("#26A69A"), // teal
        Color.parseColor("#8D6E63")  // brown
    )
    return palette[(day - 1).coerceAtLeast(0) % palette.size]
}

/**
 * Localized short descriptions for days. Kept minimal — localization should ideally use resources.
 */
fun getDayDescription(day: Int, language: Language): String = when (language) {
    Language.ARABIC_DIGITS -> "اليوم $day"
    Language.TAMIL_DIGITS -> "நாள் $day"
    Language.DEVANAGARI_DIGITS -> "दिन $day"
    else -> "روز $day"
}

/** Create a colored preview with a light background and colored text for a friendlier look. */
fun createColoredDayPreview(day: Int, size: Int = 100): Bitmap {
    val text = formatNumber(day)
    val color = getDayColor(day)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size * 0.4f
        textAlign = Paint.Align.CENTER
        color = color
    }
    val bmp = createBitmap(size, size)
    bmp.applyCanvas {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        drawColor(Color.WHITE)
        val x = size / 2f
        val y = size / 2f + bounds.height() / 2f
        drawText(text, x, y, paint)
    }
    return bmp
}

/** Weekday name mapping and helpers. */
fun getDayOfWeekName(dayIndex: Int, language: Language): String {
    // dayIndex is expected to be 0..6 (0 = Saturday) in many calendars; caller should adapt accordingly.
    return when ((dayIndex % 7 + 7) % 7) {
        0 -> if (language == Language.ARABIC_DIGITS) "السبت" else "شنبه"
        1 -> if (language == Language.ARABIC_DIGITS) "الأحد" else "یکشنبه"
        2 -> if (language == Language.ARABIC_DIGITS) "الاثنين" else "دوشنبه"
        3 -> if (language == Language.ARABIC_DIGITS) "الثلاثاء" else "سه‌شنبه"
        4 -> if (language == Language.ARABIC_DIGITS) "الأربعاء" else "چهارشنبه"
        5 -> if (language == Language.ARABIC_DIGITS) "الخميس" else "پنج‌شنبه"
        6 -> if (language == Language.ARABIC_DIGITS) "الجمعة" else "جمعه"
        else -> ""
    }
}

fun isWeekendIndex(dayIndex: Int): Boolean = ((dayIndex % 7 + 7) % 7) == 5 || ((dayIndex % 7 + 7) % 7) == 6

fun getDaySummary(day: Int, dayIndex: Int, language: Language): String =
    getDayDescription(day, language) + " - " + getDayOfWeekName(dayIndex, language)

/** Outlined text preview (stroke). */
fun createOutlinedDayPreview(day: Int, size: Int = 100): Bitmap {
    val text = formatNumber(day)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size * 0.4f
        textAlign = Paint.Align.CENTER
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.DKGRAY
    }
    val bmp = createBitmap(size, size)
    bmp.applyCanvas {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val x = size / 2f
        val y = size / 2f + bounds.height() / 2f
        drawColor(Color.TRANSPARENT)
        drawText(text, x, y, paint)
    }
    return bmp
}
 
