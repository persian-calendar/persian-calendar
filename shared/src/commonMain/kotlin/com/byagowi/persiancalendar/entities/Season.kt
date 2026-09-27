package com.byagowi.persiancalendar.entities

import androidx.compose.ui.graphics.Color
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.autumn
import com.byagowi.persiancalendar.shared.generated.resources.spring
import com.byagowi.persiancalendar.shared.generated.resources.summer
import com.byagowi.persiancalendar.shared.generated.resources.winter
import com.byagowi.persiancalendar.utils.debugAssertNotNull
import com.byagowi.persiancalendar.utils.isSouthernHemisphere
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.sunPosition
import io.github.persiancalendar.praytimes.Coordinates
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import kotlin.math.floor

enum class Season(
    val nameStringRes: StringResource, val imageRes: DrawableResource, val color: Color,
) {
    SPRING(Res.string.spring, Res.drawable.spring, Color(0xcc80aa15)),
    SUMMER(Res.string.summer, Res.drawable.summer, Color(0xccfab000)),
    AUTUMN(Res.string.autumn, Res.drawable.autumn, Color(0xccbf8015)),
    WINTER(Res.string.winter, Res.drawable.winter, Color(0xcc5580aa));

    companion object {
        fun fromTimeInMillis(timeInMillis: Long, coordinates: Coordinates?): Season {
            val sunLongitude = sunPosition(Time.fromMillisecondsSince1970(timeInMillis)).elon
            val seasonIndex = floor(sunLongitude / 90).toInt()
                // Southern Hemisphere consideration
                .let { if (coordinates?.isSouthernHemisphere == true) (it + 2) % 4 else it }
            return entries.getOrNull(seasonIndex).debugAssertNotNull ?: SPRING
        }
    }
}
