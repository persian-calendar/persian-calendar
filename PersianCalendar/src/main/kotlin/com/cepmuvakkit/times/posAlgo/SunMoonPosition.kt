package com.cepmuvakkit.times.posAlgo

import com.byagowi.persiancalendar.R
import io.github.persiancalendar.praytimes.Coordinates
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author mehmetrg
 */
class SunMoonPosition(time: GregorianCalendar, observerEarthCoordinates: Coordinates?, ΔT: Double) {

    val moonEcliptic: Ecliptic
    val sunEcliptic: Ecliptic

    val moonPhase: Double
    val moonPhaseAscending: Boolean

    val moonPosition: Horizontal?
    val sunPosition: Horizontal?

    init {
        val jd = AstroLib.calculateJulianDay(time)

        val tauSun = 8.32 / 1440.0 // 8.32 min  [cy], Earth to Sun distance in light speed terms
        moonEcliptic = LunarPosition.calculateMoonEclipticCoordinates(jd, ΔT)
        sunEcliptic = SolarPosition.calculateSunEclipticCoordinatesAstronomic(jd - tauSun, ΔT)

        val e = Math.toRadians(sunEcliptic.λ - moonEcliptic.λ)
        moonPhase = (1 - cos(e)) / 2
        moonPhaseAscending = sin(e) < 0

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
}
