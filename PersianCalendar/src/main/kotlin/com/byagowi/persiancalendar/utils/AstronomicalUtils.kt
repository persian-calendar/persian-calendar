package com.byagowi.persiancalendar.utils

import android.content.Context
import android.icu.util.ChineseCalendar
import android.os.Build
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
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
import java.util.*
import kotlin.math.atan2

// Based on Mehdi's work

fun isMoonInScorpio(jdn: Jdn) = isMoonInScorpio(jdn.toPersianCalendar(), jdn.toIslamicCalendar())

fun isMoonInScorpio(persianDate: PersianDate, islamicDate: IslamicDate) =
    (((islamicDate.dayOfMonth + 1) * 12.2f + (persianDate.dayOfMonth + 1)) / 30f +
            persianDate.month).toInt() % 12 == 8

fun getZodiacInfo(context: Context, jdn: Jdn, withEmoji: Boolean, short: Boolean): String {
    if (!isAstronomicalExtraFeaturesEnabled) return ""
    val persianDate = jdn.toPersianCalendar()
    val islamicDate = jdn.toIslamicCalendar()
    val moonInScorpioText = if (isMoonInScorpio(persianDate, islamicDate))
        context.getString(R.string.moonInScorpio) else ""

    if (short) return moonInScorpioText
    return "%s\n%s$spacedColon%s\n%s".format(
        generateYearName(context, persianDate, withEmoji),
        context.getString(R.string.zodiac),
        Zodiac.fromPersianCalendar(persianDate).format(context, withEmoji),
        moonInScorpioText
    ).trim()
}

fun generateYearName(
    context: Context,
    persianDate: PersianDate,
    withEmoji: Boolean,
    time: GregorianCalendar? = null
): String {
    val yearNames = listOfNotNull(
        language.inParentheses.format(
            ChineseZodiac.fromPersianCalendar(persianDate).format(context, withEmoji),
            context.getString(R.string.shamsi_calendar_short)
        ),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val date = ChineseCalendar((time ?: Jdn(persianDate).toJavaCalendar()).time)
            val year = date[ChineseCalendar.YEAR]
            language.inParentheses.format(
                ChineseZodiac.fromChineseCalendar(date).format(context, withEmoji),
                context.getString(R.string.chinese) + spacedComma + formatNumber(year)
            )
        } else null
    ).let { if (language.isUserAbleToReadPersian) it else it.reversed() }.joinToString(" ")
    return "%s$spacedColon%s".format(context.getString(R.string.year_name), yearNames)
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

val planetsTitles by lazy(LazyThreadSafetyMode.NONE) {
    mapOf(
        Body.Mercury to R.string.mercury,
        Body.Venus to R.string.venus,
        Body.Earth to R.string.earth,
        Body.Mars to R.string.mars,
        Body.Jupiter to R.string.jupiter,
        Body.Saturn to R.string.saturn,
        Body.Uranus to R.string.uranus,
        Body.Neptune to R.string.neptune,
        Body.Pluto to R.string.pluto,
        Body.Sun to R.string.sun,
        Body.Moon to R.string.moon,
    ).withDefault { R.string.empty }
}
