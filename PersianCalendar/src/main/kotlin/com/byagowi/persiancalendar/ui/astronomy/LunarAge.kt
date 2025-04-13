package com.byagowi.persiancalendar.ui.astronomy

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.isSouthernHemisphere
import io.github.persiancalendar.praytimes.Coordinates
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt

@JvmInline
value class LunarAge private constructor(private val fraction: Double) {

    val isAscending get() = fraction < .5

    val days get() = fraction * PERIOD

    // See also, https://github.com/janczer/goMoonPhase/blob/0363844/MoonPhase.go#L94
    val absolutePhaseValue get() = (1 - cos(fraction * 2 * PI)) / 2

    // 30 is number phases in https://en.wikipedia.org/wiki/Tithi and no rounding is need apparently
    val tithi get() = (floor(fraction * 30) + 1).toInt()

    // Eight is number phases in this system, named by most of the cultures
    fun toPhase() = Phase.entries.getOrNull((fraction * 8).roundToInt()) ?: Phase.NEW_MOON

    enum class Phase(@StringRes val stringRes: Int, private val rawEmoji: String) {
        NEW_MOON(R.string.new_moon, "🌑"),
        WAXING_CRESCENT(R.string.waxing_crescent, "🌒"),
        FIRST_QUARTER(R.string.first_quarter, "🌓"),
        WAXING_GIBBOUS(R.string.waxing_gibbous, "🌔"),
        FULL_MOON(R.string.full_moon, "🌕"),
        WANING_GIBBOUS(R.string.waning_gibbous, "🌖"),
        THIRD_QUARTER(R.string.third_quarter, "🌗"),
        WANING_CRESCENT(R.string.waning_crescent, "🌘");

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
