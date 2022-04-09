package com.cepmuvakkit.times.posAlgo

import java.util.*

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

    fun sunAltitude(latitude: Double, longitude: Double) =
        sunEquatorial.equ2TopocentricAltitude(longitude, latitude, .0, theta)

    fun moonAltitude(latitude: Double, longitude: Double) =
        moonEquatorial.equ2TopocentricAltitude(longitude, latitude, .0, theta)
}
