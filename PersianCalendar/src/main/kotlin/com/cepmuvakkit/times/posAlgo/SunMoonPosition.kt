package com.cepmuvakkit.times.posAlgo

import com.byagowi.persiancalendar.entities.LunarAge
import com.byagowi.persiancalendar.utils.isSouthernHemisphere
import io.github.persiancalendar.praytimes.Coordinates
import java.util.*

/**
 * @author mehmetrg
 */
class SunMoonPosition(time: GregorianCalendar, observer: Coordinates?, ΔT: Double) {

    val moonEcliptic: Ecliptic
    val sunEcliptic: Ecliptic

    val moonPosition: Horizontal?
    val sunPosition: Horizontal?
    val lunarSunlitTilt: Double

    val lunarAge: LunarAge

    init {
        val jd = AstroLib.calculateJulianDay(time)

        val tauSun = 8.32 / 1440.0 // 8.32 min  [cy], Earth to Sun distance in light speed terms
        moonEcliptic = LunarPosition.calculateMoonEclipticCoordinates(jd, ΔT)
        sunEcliptic = SolarPosition.calculateSunEclipticCoordinatesAstronomic(jd - tauSun, ΔT)
        lunarAge = LunarAge.fromDegrees(moonEcliptic.λ - sunEcliptic.λ)

        if (observer == null) {
            moonPosition = null
            sunPosition = null
            lunarSunlitTilt = if (lunarAge.isAscending) 180.0 else .0
        } else {
            val moonEquatorial =
                LunarPosition.calculateMoonEquatorialCoordinates(moonEcliptic, jd, ΔT)
            val sunEquatorial = SolarPosition.calculateSunEquatorialCoordinates(sunEcliptic, jd, ΔT)

            val longitude = observer.longitude
            val latitude = observer.latitude
            val elevation = observer.elevation
            val theta = SolarPosition.calculateGreenwichSiderealTime(jd, ΔT)
            moonPosition = moonEquatorial.equ2Topocentric(longitude, latitude, elevation, theta)
            sunPosition = sunEquatorial.equ2Topocentric(longitude, latitude, elevation, theta)
            lunarSunlitTilt = if (lunarAge.isAscending) {
                if (observer.isSouthernHemisphere) .0 else 180.0
            } else {
                if (observer.isSouthernHemisphere) 180.0 else .0
            }
            // TODO: Make sure our sun equatorial and moon topocentric exactly matches with what it
            //       want before enable it
            // TODO: Make sure enabling it won't ruin moon's animation much, or fixed it in some
            //       hour of a day
            // lunarSunlitTilt = lunarSunlitTilt(sunEquatorial, moonPosition, time, observer) + 90
        }
    }

    val isNight get() = sunPosition?.run { altitude <= -10 }
    val isMoonGone get() = moonPosition?.run { altitude <= -5 }

    companion object {
        // https://en.wikipedia.org/wiki/Lunar_distance_(astronomy)
        const val LUNAR_DISTANCE = 384399
    }
}

class SunMoonPositionForMap(time: GregorianCalendar) {

    private val moonEcliptic: Ecliptic
    private val sunEcliptic: Ecliptic

    private val moonEquatorial: Equatorial
    private val sunEquatorial: Equatorial

    private val ΔT = .0
    private val jd = AstroLib.calculateJulianDay(time)

    init {
        val tauSun = 8.32 / 1440.0 // 8.32 min  [cy], Earth to Sun distance in light speed terms
        moonEcliptic = LunarPosition.calculateMoonEclipticCoordinates(jd, ΔT)
        sunEcliptic = SolarPosition.calculateSunEclipticCoordinatesAstronomic(jd - tauSun, ΔT)
        moonEquatorial = LunarPosition.calculateMoonEquatorialCoordinates(moonEcliptic, jd, ΔT)
        sunEquatorial = SolarPosition.calculateSunEquatorialCoordinates(sunEcliptic, jd, ΔT)
    }

    private val theta = SolarPosition.calculateGreenwichSiderealTime(jd, ΔT)

    fun isNight(latitude: Double, longitude: Double) =
        sunEquatorial.equ2Topocentric(longitude, latitude, .0, theta).altitude <= -10

    fun isMoonGone(latitude: Double, longitude: Double) =
        moonEquatorial.equ2Topocentric(longitude, latitude, .0, theta).altitude <= -5
}
