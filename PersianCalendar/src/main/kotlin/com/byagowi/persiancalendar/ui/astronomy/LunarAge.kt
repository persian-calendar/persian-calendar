package com.byagowi.persiancalendar.ui.astronomy

import com.byagowi.persiancalendar.utils.isSouthernHemisphere
import io.github.persiancalendar.praytimes.Coordinates
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt

@JvmInline
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
                coordinates?.isSouthernHemisphere == true -> entries[entries.size - ordinal].rawEmoji
                else -> rawEmoji
            }
        }
    }

    companion object {
        private fun to360(angle: Double) = angle % 360 + if (angle < 0) 360 else 0
        fun fromDegrees(e: Double) = LunarAge(to360(e) / 360)

        const val PERIOD = 29.530588853 // Actually this isn't a constant and is decreasing slooowly
    }
}
