package com.byagowi.persiancalendar.entities

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.byagowi.persiancalendar.R
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
import org.jetbrains.compose.resources.StringResource
import kotlin.math.floor

enum class Season(
    val nameStringRes: StringResource, @get:DrawableRes val imageId: Int, val color: Color,
) {
    SPRING(Res.string.spring, R.drawable.spring, Color(0xcc80aa15)),
    SUMMER(Res.string.summer, R.drawable.summer, Color(0xccfab000)),
    AUTUMN(Res.string.autumn, R.drawable.autumn, Color(0xccbf8015)),
    WINTER(Res.string.winter, R.drawable.winter, Color(0xcc5580aa));

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
