package com.cepmuvakkit.times.posAlgo

import com.byagowi.persiancalendar.R
import io.github.persiancalendar.praytimes.Coordinates
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author mehmetrg
 */
class SunMoonPosition(time: GregorianCalendar, observerEarthCoordinates: Coordinates, ΔT: Double) {

    val moonEcliptic: Ecliptic
    val sunEcliptic: Ecliptic

    val moonPhase: Double
    val moonPhaseAscending: Boolean

    val moonPosition: Horizontal
    val sunPosition: Horizontal

    init {
        val jd = AstroLib.calculateJulianDay(time)

        val tauSun = 8.32 / 1440.0 // 8.32 min  [cy]
        moonEcliptic = LunarPosition.calculateMoonEclipticCoordinates(jd, ΔT)
        sunEcliptic = SolarPosition.calculateSunEclipticCoordinatesAstronomic(jd - tauSun, ΔT)

        val e = Math.toRadians(sunEcliptic.λ - moonEcliptic.λ)
        moonPhase = (1 - cos(e)) / 2
        moonPhaseAscending = sin(e) < 0

        val moonEquatorial = LunarPosition.calculateMoonEquatorialCoordinates(moonEcliptic, jd, ΔT)
        val sunEquatorial = SolarPosition.calculateSunEquatorialCoordinates(sunEcliptic, jd, ΔT)

        val longitude = observerEarthCoordinates.longitude
        val latitude = observerEarthCoordinates.latitude
        val elevation = observerEarthCoordinates.elevation
        // az=183.5858
        moonPosition = moonEquatorial.equ2Topocentric(longitude, latitude, elevation, jd, ΔT)
        sunPosition = sunEquatorial.equ2Topocentric(longitude, latitude, elevation, jd, ΔT)
    }

    // https://github.com/janczer/goMoonPhase/blob/master/MoonPhase.go#L363
    val moonZodiac: Int
        get() = when {
            moonEcliptic.λ < 33.18 -> R.string.aries
            moonEcliptic.λ < 51.16 -> R.string.taurus
            moonEcliptic.λ < 93.44 -> R.string.gemini
            moonEcliptic.λ < 119.48 -> R.string.cancer
            moonEcliptic.λ < 135.30 -> R.string.leo
            moonEcliptic.λ < 173.34 -> R.string.virgo
            moonEcliptic.λ < 224.17 -> R.string.libra
            moonEcliptic.λ < 242.57 -> R.string.scorpio
            moonEcliptic.λ < 271.26 -> R.string.sagittarius
            moonEcliptic.λ < 302.49 -> R.string.capricorn
            moonEcliptic.λ < 311.72 -> R.string.aquarius
            moonEcliptic.λ < 348.58 -> R.string.pisces
            else -> R.string.aries
        }
}
