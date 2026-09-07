package com.byagowi.persiancalendar.ui.astronomy

import io.github.persiancalendar.praytimes.Coordinates
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt

@kotlin.jvm.JvmInline
value class LunarAge private constructor(private val fraction: Double) {

    val isAscending get() = fraction < .5

    val days get() = fraction * PERIOD

    // See also, https://github.com/janczer/goMoonPhase/blob/0363844/MoonPhase.go#L94
    val absolutePhaseValue get() = (1 - cos(fraction * 2 * PI)) / 2

    // Eight is number phases in this system, named by most of the cultures
    fun toPhase() = Phase.entries.getOrNull((fraction * 8).roundToInt()) ?: Phase.NEW_MOON

    enum class Phase(private val rawEmoji: String) {
        NEW_MOON("🌑"),
        WAXING_CRESCENT("🌒"),
        FIRST_QUARTER("🌓"),
        WAXING_GIBBOUS("🌔"),
        FULL_MOON("🌕"),
        WANING_GIBBOUS("🌖"),
        THIRD_QUARTER("🌗"),
        WANING_CRESCENT("🌘");

        fun emoji(coordinates: Coordinates?): String {
            return when {
                ordinal == 0 -> rawEmoji
                (coordinates?.latitude ?: 0.0) < 0.0 -> entries[entries.size - ordinal].rawEmoji
                else -> rawEmoji
            }
        }
    }

    companion object {
        fun fromDegrees(e: Double) = LunarAge(e.mod(360.0) / 360)

        const val PERIOD = 29.530588853 // Actually this isn't a constant and is decreasing slooowly
    }
}
