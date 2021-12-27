package com.cepmuvakkit.times.posAlgo

import io.github.persiancalendar.praytimes.Coordinates
import java.util.*
import kotlin.math.cos
import kotlin.math.floor

/**
 * @author mehmetrg
 */
class SunMoonPosition(time: GregorianCalendar, observerEarthCoordinates: Coordinates?, ΔT: Double) {

    val moonEcliptic: Ecliptic
    val sunEcliptic: Ecliptic

    val moonPosition: Horizontal?
    val sunPosition: Horizontal?

    val moonAgeInDegrees: Double

    init {
        val jd = AstroLib.calculateJulianDay(time)

        val tauSun = 8.32 / 1440.0 // 8.32 min  [cy], Earth to Sun distance in light speed terms
        moonEcliptic = LunarPosition.calculateMoonEclipticCoordinates(jd, ΔT)
        sunEcliptic = SolarPosition.calculateSunEclipticCoordinatesAstronomic(jd - tauSun, ΔT)

        fun to360(angle: Double) = angle % 360.0 + if (angle < 0) 360 else 0
        moonAgeInDegrees = to360(sunEcliptic.λ - moonEcliptic.λ)

        if (observerEarthCoordinates == null) {
            moonPosition = null
            sunPosition = null
        } else {
            val moonEquatorial =
                LunarPosition.calculateMoonEquatorialCoordinates(moonEcliptic, jd, ΔT)
            val sunEquatorial = SolarPosition.calculateSunEquatorialCoordinates(sunEcliptic, jd, ΔT)

            val longitude = observerEarthCoordinates.longitude
            val latitude = observerEarthCoordinates.latitude
            val elevation = observerEarthCoordinates.elevation
            moonPosition = moonEquatorial.equ2Topocentric(longitude, latitude, elevation, jd, ΔT)
            sunPosition = sunEquatorial.equ2Topocentric(longitude, latitude, elevation, jd, ΔT)
        }
    }

    // https://en.wikipedia.org/wiki/Tithi
    val tithi get() = floor(moonAgeInDegrees / 12)

    // https://github.com/janczer/goMoonPhase/blob/0363844/MoonPhase.go#L94
    val moonPhase get() = (1 - cos(Math.toRadians(moonAgeInDegrees))) / 2

    // https://github.com/janczer/goMoonPhase/blob/0363844/MoonPhase.go#L342
    private val moonPhaseOrdinal get() = floor((moonAgeInDegrees / 360 + .0625) * 8).toInt()
    val moonPhaseName get() = moonPhasesNames.getOrNull(moonPhaseOrdinal) ?: moonPhasesNames[0]
    val moonPhaseEmoji get() = moonPhasesEmojis.getOrNull(moonPhaseOrdinal) ?: moonPhasesEmojis[0]

    companion object {
        // https://en.wikipedia.org/wiki/Lunar_distance_(astronomy)
        const val LUNAR_DISTANCE = 384399

        val moonPhasesNames = listOf(
            "New Moon", "Waxing Crescent", "First Quarter", "Waxing Gibbous", "Full Moon",
            "Waning Gibbous", "Third Quarter", "Waning Crescent", "New Moon"
        )
        val moonPhasesEmojis = listOf("🌑", "🌒", "🌓", "🌔", "🌕", "🌖", "🌗", "🌘", "🌑")
    }
}
