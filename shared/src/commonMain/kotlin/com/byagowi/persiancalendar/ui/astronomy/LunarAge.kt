package com.byagowi.persiancalendar.ui.astronomy

import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.moon_first_quarter
import com.byagowi.persiancalendar.shared.generated.resources.moon_full_moon
import com.byagowi.persiancalendar.shared.generated.resources.moon_new_moon
import com.byagowi.persiancalendar.shared.generated.resources.moon_third_quarter
import com.byagowi.persiancalendar.shared.generated.resources.moon_waning_crescent
import com.byagowi.persiancalendar.shared.generated.resources.moon_waning_gibbous
import com.byagowi.persiancalendar.shared.generated.resources.moon_waxing_crescent
import com.byagowi.persiancalendar.shared.generated.resources.moon_waxing_gibbous
import com.byagowi.persiancalendar.utils.isSouthernHemisphere
import io.github.persiancalendar.praytimes.Coordinates
import org.jetbrains.compose.resources.StringResource
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

    enum class Phase(val title: StringResource, private val rawEmoji: String) {
        NEW_MOON(Res.string.moon_new_moon, "🌑"),
        WAXING_CRESCENT(Res.string.moon_waxing_crescent, "🌒"),
        FIRST_QUARTER(Res.string.moon_first_quarter, "🌓"),
        WAXING_GIBBOUS(Res.string.moon_waxing_gibbous, "🌔"),
        FULL_MOON(Res.string.moon_full_moon, "🌕"),
        WANING_GIBBOUS(Res.string.moon_waning_gibbous, "🌖"),
        THIRD_QUARTER(Res.string.moon_third_quarter, "🌗"),
        WANING_CRESCENT(Res.string.moon_waning_crescent, "🌘");

        fun emoji(coordinates: Coordinates?): String {
            return when {
                ordinal == 0 -> rawEmoji
                coordinates?.isSouthernHemisphere == true -> entries[entries.size - ordinal].rawEmoji
                else -> rawEmoji
            }
        }
    }

    companion object {
        fun fromDegrees(e: Double) = LunarAge(e.mod(360.0) / 360)

        const val PERIOD = 29.530588853 // Actually this isn't a constant and is decreasing slooowly
    }
}
