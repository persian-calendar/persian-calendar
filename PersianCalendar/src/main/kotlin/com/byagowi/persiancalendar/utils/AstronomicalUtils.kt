package com.byagowi.persiancalendar.utils

import android.content.res.Resources
import android.icu.util.ChineseCalendar
import android.os.Build
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac
import com.byagowi.persiancalendar.ui.astronomy.Zodiac
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.horizon
import io.github.cosinekitty.astronomy.rotationEqdHor
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import java.util.GregorianCalendar
import kotlin.math.atan2

// Based on Mehdi's work

fun isMoonInScorpio(jdn: Jdn): Boolean = isMoonInScorpio(jdn.toPersianDate(), jdn.toIslamicDate())

fun isMoonInScorpio(persianDate: PersianDate, islamicDate: IslamicDate): Boolean {
    return (((islamicDate.dayOfMonth + 1) * 12.2f + (persianDate.dayOfMonth + 1)) / 30f +
            persianDate.month).toInt() % 12 == 8
}

fun generateZodiacInformation(resources: Resources, jdn: Jdn, withEmoji: Boolean): String {
    val persianDate = jdn.toPersianDate()
    return "%s\n%s$spacedColon%s".format(
        generateYearName(resources, persianDate, withEmoji),
        resources.getString(R.string.zodiac),
        Zodiac.fromPersianCalendar(persianDate).format(resources, withEmoji)
    ).trim()
}

fun generateYearName(
    resources: Resources,
    persianDate: PersianDate,
    withEmoji: Boolean,
    time: GregorianCalendar? = null
): String {
    val yearNames = listOfNotNull(
        language.value.inParentheses.format(
            ChineseZodiac.fromPersianCalendar(persianDate).format(resources, withEmoji, true),
            resources.getString(R.string.shamsi_calendar_short)
        ),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val date = ChineseCalendar((time ?: Jdn(persianDate).toGregorianCalendar()).time)
            val year = date[ChineseCalendar.YEAR]
            language.value.inParentheses.format(
                ChineseZodiac.fromChineseCalendar(date).format(resources, withEmoji, false),
                resources.getString(R.string.chinese) + spacedComma + formatNumber(year)
            )
        } else null
    ).let { if (language.value.isUserAbleToReadPersian) it else it.reversed() }.joinToString(" ")
    return "%s$spacedColon%s".format(resources.getString(R.string.year_name), yearNames)
}

// https://github.com/cosinekitty/astronomy/blob/0547aaf/demo/csharp/camera/camera.cs#L98
fun sunlitSideMoonTiltAngle(time: Time, observer: Observer): Double {
    val moonEquator = equator(Body.Moon, time, observer, EquatorEpoch.OfDate, Aberration.None)
    val sunEquator = equator(Body.Sun, time, observer, EquatorEpoch.OfDate, Aberration.None)
    val moonHorizontal =
        horizon(time, observer, moonEquator.ra, moonEquator.dec, Refraction.None)
    val vec = rotationEqdHor(time, observer)
        .pivot(2, moonHorizontal.azimuth)
        .pivot(1, moonHorizontal.altitude)
        .rotate(sunEquator.vec)
    return Math.toDegrees(atan2(vec.z, vec.y))
}

val Body.titleStringId
    get(): @StringRes Int = when (this) {
        Body.Mercury -> R.string.mercury
        Body.Venus -> R.string.venus
        Body.Earth -> R.string.earth
        Body.Mars -> R.string.mars
        Body.Jupiter -> R.string.jupiter
        Body.Saturn -> R.string.saturn
        Body.Uranus -> R.string.uranus
        Body.Neptune -> R.string.neptune
        Body.Pluto -> R.string.pluto
        Body.Sun -> R.string.sun
        Body.Moon -> R.string.moon
        else -> R.string.empty
    }
