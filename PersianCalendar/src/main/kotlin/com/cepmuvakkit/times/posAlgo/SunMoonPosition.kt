package com.cepmuvakkit.times.posAlgo

import io.github.persiancalendar.praytimes.Coordinates
import java.util.*
import kotlin.math.cos

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

    // https://github.com/janczer/goMoonPhase/blob/0363844/MoonPhase.go#L94
    val moonPhase get() = (1 - cos(Math.toRadians(moonAgeInDegrees))) / 2

    private fun to360(angle: Double) = angle % 360.0 + if (angle < 0) 360 else 0

    companion object {
        // https://en.wikipedia.org/wiki/Lunar_distance_(astronomy)
        const val LUNAR_DISTANCE = 384399
    }
}
