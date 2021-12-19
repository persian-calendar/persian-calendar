package com.cepmuvakkit.times.posAlgo

import io.github.persiancalendar.praytimes.Coordinates
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author mehmetrg
 */
class SunMoonPosition(
    time: GregorianCalendar, coordinates: Coordinates, ΔT: Double,
    val latitude: Double = coordinates.latitude
) {
    val sunPosition: Horizontal
    val moonPosition: Horizontal
    val moonPhase: Double
    val moonPhaseAscending: Boolean
    fun illumunatedFractionofMoon(jd: Double, ΔT: Double): Double {
        return moonPhase
    }

    val moonEcliptic: Ecliptic
    val sunEcliptic: Ecliptic

    init {
        val jd = AstroLib.calculateJulianDay(time)
        val tauSun = 8.32 / 1440.0 // 8.32 min  [cy]
        moonEcliptic = LunarPosition.calculateMoonEclipticCoordinates(jd, ΔT)
        sunEcliptic = SolarPosition.calculateSunEclipticCoordinatesAstronomic(jd - tauSun, ΔT)
        val E = Math.toRadians(sunEcliptic.λ - moonEcliptic.λ)
        val moonPosEq = LunarPosition.calculateMoonEquatorialCoordinates(moonEcliptic, jd, ΔT)
        val solarPosEq = SolarPosition.calculateSunEquatorialCoordinates(sunEcliptic, jd, ΔT)
        moonPosition = moonPosEq.equ2Topocentric(
            coordinates.longitude, coordinates.latitude, coordinates.elevation, jd, ΔT
        ) //az=183.5858
        sunPosition = solarPosEq.equ2Topocentric(
            coordinates.longitude, coordinates.latitude, coordinates.elevation, jd, ΔT
        )

        // double E = 0;// APC_Sun.L-APC_Moon.l_Moon;//l_moon 1.4421 L=6.18064// E=4.73850629772695878
        // moonPhase = (1 + cos(pi - E)) / 2;
        moonPhase = (1 - cos(E)) / 2 //48694254279852139 e-17
        moonPhaseAscending = sin(E) < 0
    }
}
