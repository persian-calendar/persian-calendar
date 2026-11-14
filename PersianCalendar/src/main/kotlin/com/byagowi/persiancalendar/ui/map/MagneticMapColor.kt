package com.byagowi.persiancalendar.ui.map

import kotlin.math.abs

internal fun magneticMapColor(value: Float): Int {
    val alpha = (abs(value) * 255 / 180).toInt().coerceIn(0, 255)
    return when {
        value > 1 -> (alpha shl 24) or 0xFF0000
        value < -1 -> (alpha shl 24) or 0xFF
        else -> ((30 - abs(value) * 30).toInt() shl 24) or 0xFF00
    }
}
