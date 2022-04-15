package com.byagowi.persiancalendar.ui.astronomy

import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.equatorialToEcliptic
import io.github.cosinekitty.astronomy.geoVector
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt

const val AU_IN_KM = 149597871 // astronomical unit, ~earth/sun distance

private fun formatAngle(value: Double): String {
    val degrees = floor(value)
    return "${degrees.toInt()}°${((value - degrees) * 60).roundToInt()}’"
}

fun showHoroscopesDialog(activity: FragmentActivity, date: Date = Date()) {
    val time = Time.fromMillisecondsSince1970(date.time)
    MaterialAlertDialogBuilder(activity)
        .setMessage(listOf(
            Body.Sun, Body.Moon, Body.Mercury, Body.Venus, Body.Mars, Body.Jupiter,
            Body.Saturn, Body.Uranus, Body.Neptune, Body.Pluto
        ).joinToString("\n") { body ->
            body.name + equatorialToEcliptic(geoVector(body, time, Aberration.None)).let {
                ": ${Zodiac.fromTropical(it.elon).emoji} ${
                    formatAngle(it.elon % 30)
                } ${"%,d".format(Locale.ENGLISH, (it.vec.length() * AU_IN_KM).roundToInt())} km"
            }
        })
        .show()
}
