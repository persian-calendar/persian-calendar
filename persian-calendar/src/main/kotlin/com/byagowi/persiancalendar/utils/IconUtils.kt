package com.byagowi.persiancalendar.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.preferredDigits

// Dynamic icon generation, currently unused
fun createStatusIcon(dayOfMonth: Int): Bitmap {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textSize = if (isArabicDigitSelected) 75f else 90f
        it.textAlign = Paint.Align.CENTER
        it.color = Color.WHITE
    }
    val text = formatNumber(dayOfMonth)
    val bounds = Rect()
    paint.getTextBounds(text, 0, text.length, bounds)
    return createBitmap(90, 90).applyCanvas {
        drawText(text, 45f, 45 + bounds.height() / 2f, paint)
    }
}

fun getDayIconResource(day: Int): Int = when (preferredDigits) {
    Language.DEVANAGARI_DIGITS, Language.ARABIC_DIGITS -> DAYS_ICONS_ARABIC
    Language.ARABIC_INDIC_DIGITS -> DAYS_ICONS_ARABIC_INDIC
    else -> DAYS_ICONS_PERSIAN
}.getOrNull(day - 1) ?: 0

private val DAYS_ICONS_PERSIAN = listOf31Items(
    R.drawable.day1, R.drawable.day2, R.drawable.day3, R.drawable.day4, R.drawable.day5,
    R.drawable.day6, R.drawable.day7, R.drawable.day8, R.drawable.day9, R.drawable.day10,
    R.drawable.day11, R.drawable.day12, R.drawable.day13, R.drawable.day14, R.drawable.day15,
    R.drawable.day16, R.drawable.day17, R.drawable.day18, R.drawable.day19, R.drawable.day20,
    R.drawable.day21, R.drawable.day22, R.drawable.day23, R.drawable.day24, R.drawable.day25,
    R.drawable.day26, R.drawable.day27, R.drawable.day28, R.drawable.day29, R.drawable.day30,
    R.drawable.day31
)

// No Urdu ones are provided as they use Arabic ones nowadays
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
