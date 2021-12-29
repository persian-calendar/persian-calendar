package com.byagowi.persiancalendar.entities

import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt

@JvmInline
value class LunarAge private constructor(private val deg: Double) {

    val isAscending get() = deg < 180
    val absolutePhaseValue get() = (1 - cos(Math.toRadians(deg))) / 2
    val days get() = (deg / 360) * 29.530588853 // Actually this isn't a constant and is decreasing
    val tithi get() = (floor(deg / 12) + 1).toInt() // https://en.wikipedia.org/wiki/Tithi
    fun toPhase() = Phase.values().getOrNull((deg / 45).roundToInt()) ?: Phase.NEW_MOON

    enum class Phase(val emoji: String) {
        NEW_MOON("🌑"), WAXING_CRESCENT("🌒"), FIRST_QUARTER("🌓"),
        WAXING_GIBBOUS("🌔"), FULL_MOON("🌕"), WANING_GIBBOUS("🌖"),
        THIRD_QUARTER("🌗"), WANING_CRESCENT("🌘")
    }

    companion object {
        private fun to360(angle: Double) = angle % 360 + if (angle < 0) 360 else 0
        fun fromDegrees(e: Double) = LunarAge(to360(e))
    }
}
