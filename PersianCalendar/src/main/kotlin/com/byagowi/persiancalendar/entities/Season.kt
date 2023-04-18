package com.byagowi.persiancalendar.entities

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.isSouthernHemisphere
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.sunPosition
import io.github.persiancalendar.praytimes.Coordinates
import java.util.Date
import kotlin.math.floor

enum class Season(
    @StringRes val nameStringId: Int, @DrawableRes val imageId: Int, @ColorInt val color: Int
) {
    SPRING(R.string.spring, R.drawable.spring, 0xcc80aa15.toInt()),
    SUMMER(R.string.summer, R.drawable.summer, 0xccfab000.toInt()),
    AUTUMN(R.string.autumn, R.drawable.fall, 0xccbf8015.toInt()),
    WINTER(R.string.winter, R.drawable.winter, 0xcc5580aa.toInt());

    companion object {
        fun fromDate(date: Date, coordinates: Coordinates?): Season {
            val sunLongitude = sunPosition(Time.fromMillisecondsSince1970(date.time)).elon
            val seasonIndex = floor(sunLongitude / 90).toInt()
                // Southern hemisphere consideration
                .let { if (coordinates?.isSouthernHemisphere == true) (it + 2) % 4 else it }
            return enumValues<Season>().getOrNull(seasonIndex).debugAssertNotNull ?: SPRING
        }
    }
}
