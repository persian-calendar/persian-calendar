package com.cepmuvakkit.times.posAlgo

import io.github.persiancalendar.praytimes.Coordinates
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author mehmetrg
 */
class SunMoonPosition(time: GregorianCalendar, observerEarthCoordinates: Coordinates, ΔT: Double) {

    val moonPosition: Horizontal
    val sunPosition: Horizontal

    val moonEcliptic: Ecliptic
    val sunEcliptic: Ecliptic

    val moonPhase: Double
    val moonPhaseAscending: Boolean

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
}
