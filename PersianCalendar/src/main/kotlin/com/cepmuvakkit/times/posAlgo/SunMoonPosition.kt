package com.cepmuvakkit.times.posAlgo

import com.byagowi.persiancalendar.entities.LunarAge
import com.byagowi.persiancalendar.utils.isSouthernHemisphere
import io.github.persiancalendar.praytimes.Coordinates
import java.util.*

/**
 * @author mehmetrg
 */
class SunMoonPosition(time: GregorianCalendar, observerEarthCoordinates: Coordinates?, ΔT: Double) {

    val moonEcliptic: Ecliptic
    val sunEcliptic: Ecliptic

    val moonPosition: Horizontal?
    val sunPosition: Horizontal?
    val isSouthernHemisphere: Boolean

    val lunarAge: LunarAge

    init {
        val jd = AstroLib.calculateJulianDay(time)

        val tauSun = 8.32 / 1440.0 // 8.32 min  [cy], Earth to Sun distance in light speed terms
        moonEcliptic = LunarPosition.calculateMoonEclipticCoordinates(jd, ΔT)
        sunEcliptic = SolarPosition.calculateSunEclipticCoordinatesAstronomic(jd - tauSun, ΔT)
        lunarAge = LunarAge.fromDegrees(sunEcliptic.λ - moonEcliptic.λ)

        if (observerEarthCoordinates == null) {
            moonPosition = null
            sunPosition = null
            isSouthernHemisphere = false
        } else {
            val moonEquatorial =
                LunarPosition.calculateMoonEquatorialCoordinates(moonEcliptic, jd, ΔT)
            val sunEquatorial = SolarPosition.calculateSunEquatorialCoordinates(sunEcliptic, jd, ΔT)

            val longitude = observerEarthCoordinates.longitude
            val latitude = observerEarthCoordinates.latitude
            val elevation = observerEarthCoordinates.elevation
            moonPosition = moonEquatorial.equ2Topocentric(longitude, latitude, elevation, jd, ΔT)
            sunPosition = sunEquatorial.equ2Topocentric(longitude, latitude, elevation, jd, ΔT)
            isSouthernHemisphere = observerEarthCoordinates.isSouthernHemisphere
        }
    }

    companion object {
        // https://en.wikipedia.org/wiki/Lunar_distance_(astronomy)
        const val LUNAR_DISTANCE = 384399
    }
}
