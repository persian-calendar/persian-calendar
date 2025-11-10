package com.byagowi.persiancalendar.utils

import android.content.res.Resources
import android.icu.util.ChineseCalendar
import android.os.Build
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.numeral
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
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.horizon
import io.github.cosinekitty.astronomy.rotationEqdHor
import io.github.cosinekitty.astronomy.search
import io.github.cosinekitty.astronomy.sunPosition
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.math.atan2

fun isMoonInScorpio(time: Time) = eclipticGeoMoon(time).lon in Zodiac.scorpioRange

fun lunarLongitude(jdn: Jdn, setIranTime: Boolean = false, hourOfDay: Int): Double =
    eclipticGeoMoon(jdn.toAstronomyTime(hourOfDay, setIranTime)).lon

// This only checks the midday, useful for calendar table where fast calculation is needed
fun isMoonInScorpio(jdn: Jdn, hourOfDay: Int = 12, setIranTime: Boolean = false): Boolean =
    lunarLongitude(jdn, setIranTime, hourOfDay) in Zodiac.scorpioRange

sealed interface MoonInScorpioState {
    data object Borji : MoonInScorpioState
    data object Falaki : MoonInScorpioState
    data class Start(val clock: Clock) : MoonInScorpioState
    data class End(val clock: Clock) : MoonInScorpioState
}

private fun Double.withMaxDegreeValue(max: Double): Double {
    var deg = this
    while (deg <= max - 360.0) deg += 360.0
    while (deg > max) deg -= 360.0
    return deg
}

fun searchMoonAgeTime(jdn: Jdn, targetDegrees: Double): Clock? {
    return search(jdn.toAstronomyTime(0), (jdn + 1).toAstronomyTime(0), 1.0) { time ->
        val d = eclipticGeoMoon(time).lon - sunPosition(time).elon
        (d.mod(360.0) - targetDegrees).withMaxDegreeValue(+180.0)
    }?.toMillisecondsSince1970()?.let { timestamp ->
        Clock(GregorianCalendar().also { it.timeInMillis = timestamp })
    }
}

// setIranTime should be used only for tests
private fun searchLunarLongitude(jdn: Jdn, targetLon: Double, setIranTime: Boolean): Clock {
    val startTime = jdn.toAstronomyTime(hourOfDay = 0, setIranTime = setIranTime)
    val endTime = startTime.addDays(1.0)
    return search(startTime, endTime, 1.0) { time ->
        (eclipticGeoMoon(time).lon - targetLon).withMaxDegreeValue(+180.0)
    }?.toMillisecondsSince1970()?.let { timeInMillis ->
        Clock(
            GregorianCalendar().also {
                if (setIranTime) it.timeZone = TimeZone.getTimeZone(IRAN_TIMEZONE_ID)
                it.timeInMillis = timeInMillis
            }
        )
    } ?: Clock.zero
}

fun moonInScorpioState(jdn: Jdn, setIranTime: Boolean = false): MoonInScorpioState? {
    val end = isMoonInScorpio(jdn, 0, setIranTime)
    val start = isMoonInScorpio(jdn + 1, 0, setIranTime)
    return when {
        start && end ->
            if (lunarLongitude(jdn, setIranTime, hourOfDay = 12) <= Zodiac.SCORPIO.tropicalRange[1])
                MoonInScorpioState.Borji else MoonInScorpioState.Falaki

        start -> MoonInScorpioState.Start(
            searchLunarLongitude(jdn, Zodiac.scorpioRange.start, setIranTime)
        )

        end -> MoonInScorpioState.End(
            searchLunarLongitude(jdn, Zodiac.scorpioRange.endInclusive, setIranTime)
        )

        else -> null
    }
}

fun generateYearName(
    resources: Resources,
    jdn: Jdn,
    withEmoji: Boolean,
    time: GregorianCalendar? = null,
    withOldEraName: Boolean = false,
): String {
    val persianDate = jdn.toPersianDate()
    val yearNames = listOfNotNull(
        language.value.inParentheses.format(
            ChineseZodiac.fromPersianCalendar(persianDate).format(
                resources = resources,
                withEmoji = withEmoji,
                isPersian = language.value.isPersianOrDari,
                withOldEraName = withOldEraName,
            ),
            resources.getString(R.string.shamsi_calendar_short)
        ),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val date = ChineseCalendar((time ?: jdn.toGregorianCalendar()).time)
            val year = date[ChineseCalendar.YEAR]
            language.value.inParentheses.format(
                ChineseZodiac.fromChineseCalendar(date).format(
                    resources = resources,
                    isPersian = false,
                    withEmoji = withEmoji,
                ),
                resources.getString(R.string.chinese) + spacedComma + numeral.value.format(year)
            )
        } else null
    ).let { if (language.value.isUserAbleToReadPersian) it else it.reversed() }.joinToString(" ")
    return "${resources.getString(R.string.year_name)}$spacedColon${yearNames}"
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
    @StringRes
    get(): Int = when (this) {
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

val Body.symbol
    get() = when (this) {
        Body.Sun -> "☉"
        Body.Earth -> "🜨"
        Body.Moon -> "☽"
        Body.Mars -> "♂"
        Body.Mercury -> "☿"
        Body.Jupiter -> "♃"
        Body.Venus -> "♀"
        Body.Saturn -> "♄"
        Body.Uranus -> "⛢"
        Body.Neptune -> "♆"
        Body.Pluto -> "♇"
        else -> ""
    }
