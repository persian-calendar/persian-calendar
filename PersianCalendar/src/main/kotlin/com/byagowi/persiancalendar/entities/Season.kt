package com.byagowi.persiancalendar.entities

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.isSouthernHemisphere
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.sunPosition
import io.github.persiancalendar.praytimes.Coordinates
import java.util.Date
import kotlin.math.floor

enum class Season(
    @StringRes val nameStringId: Int, @DrawableRes val imageId: Int, val color: Color
) {
    SPRING(R.string.spring, R.drawable.spring, Color(0xcc80aa15)),
    SUMMER(R.string.summer, R.drawable.summer, Color(0xccfab000)),
    AUTUMN(R.string.autumn, R.drawable.fall, Color(0xccbf8015)),
    WINTER(R.string.winter, R.drawable.winter, Color(0xcc5580aa));

    companion object {
        fun fromDate(date: Date, coordinates: Coordinates?): Season {
            val sunLongitude = sunPosition(Time.fromMillisecondsSince1970(date.time)).elon
            val seasonIndex = floor(sunLongitude / 90).toInt()
                // Southern hemisphere consideration
                .let { if (coordinates?.isSouthernHemisphere == true) (it + 2) % 4 else it }
            return entries.getOrNull(seasonIndex).debugAssertNotNull ?: SPRING
        }
    }
}
