package com.byagowi.persiancalendar.ui.astronomy

import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.AU_IN_KM
import com.byagowi.persiancalendar.LRM
import com.byagowi.persiancalendar.utils.titleStringId
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.equatorialToEcliptic
import io.github.cosinekitty.astronomy.geoVector
import io.github.cosinekitty.astronomy.sunPosition
import java.util.Date
import java.util.Locale
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private fun formatAngle(value: Double): String {
    val degrees = floor(value)
    return "${degrees.toInt()}°${((value - degrees) * 60).roundToInt()}’"
}

fun showHoroscopesDialog(activity: FragmentActivity, date: Date = Date()) {
    val time = Time.fromMillisecondsSince1970(date.time)
    androidx.appcompat.app.AlertDialog.Builder(activity)
        .setMessage(listOf(
            Body.Sun, Body.Moon, Body.Mercury, Body.Venus, Body.Mars, Body.Jupiter,
            Body.Saturn, Body.Uranus, Body.Neptune, Body.Pluto
        ).joinToString("\n") { body ->
            val (longitude, distance) = when (body) {
                Body.Sun -> sunPosition(time).let { it.elon to it.vec.length() }
                Body.Moon -> eclipticGeoMoon(time).let { it.lon to it.dist }
                else -> equatorialToEcliptic(geoVector(body, time, Aberration.Corrected))
                    .let { it.elon to it.vec.length() }
            }
            val name = activity.getString(body.titleStringId)
            name + ": %s%s %s %,d km".format(
                Locale.ENGLISH,
                LRM,
                formatAngle(longitude % 30), // Remaining angle
                Zodiac.fromTropical(longitude).emoji,
                (distance * AU_IN_KM).roundToLong()
            )
        })
        .show()
}
