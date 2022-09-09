package com.byagowi.persiancalendar.ui.astronomy

import android.content.Context
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.generateYearName
import com.byagowi.persiancalendar.utils.planetsTitles
import com.byagowi.persiancalendar.utils.sunlitSideMoonTiltAngle
import com.byagowi.persiancalendar.utils.toJavaCalendar
import com.byagowi.persiancalendar.utils.toObserver
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.equatorialToEcliptic
import io.github.cosinekitty.astronomy.geoVector
import io.github.cosinekitty.astronomy.helioVector
import io.github.cosinekitty.astronomy.searchGlobalSolarEclipse
import io.github.cosinekitty.astronomy.searchLocalSolarEclipse
import io.github.cosinekitty.astronomy.searchLunarEclipse
import io.github.persiancalendar.calendar.PersianDate
import java.util.*

class AstronomyState(val date: GregorianCalendar) {
    private val time = Time.fromMillisecondsSince1970(date.time.time)
    val sun = equatorialToEcliptic(geoVector(Body.Sun, time, Aberration.Corrected))
    val moon = equatorialToEcliptic(geoVector(Body.Moon, time, Aberration.Corrected))
    val moonTilt by lazy(LazyThreadSafetyMode.NONE) {
        coordinates?.let { coordinates ->
            sunlitSideMoonTiltAngle(time, coordinates.toObserver()).toFloat()
        }
    }
    val planets by lazy(LazyThreadSafetyMode.NONE) {
        solarSystemPlanets
            .map { planetsTitles.getValue(it) to equatorialToEcliptic(helioVector(it, time)) }
    }

    fun generateHeader(context: Context, persianDate: PersianDate): String {
        val observer = coordinates?.toObserver()
        return (listOf(
            if (observer != null) {
                searchLocalSolarEclipse(time, observer).let { it.kind to it.peak.time }
            } else {
                searchGlobalSolarEclipse(time).let { it.kind to it.peak }
            },
            searchLunarEclipse(time).let { it.kind to it.peak }
        ).mapIndexed { i, (kind, peak) ->
            val formattedDate = Date(peak.toMillisecondsSince1970()).toJavaCalendar()
                .formatDateAndTime()
            val isSolar = i == 0
            val title = if (isSolar) R.string.solar_eclipse else R.string.lunar_eclipse
            (language.tryTranslateEclipseType(isSolar, kind) ?: context.getString(title)) +
                    spacedColon + formattedDate
        } + listOf(generateYearName(context, persianDate, true, date))).joinToString("\n")
    }

    companion object {
        private val solarSystemPlanets = listOf(
            Body.Mercury, Body.Venus, Body.Earth, Body.Mars, Body.Jupiter, Body.Saturn,
            Body.Uranus, Body.Neptune
        )
    }
}
