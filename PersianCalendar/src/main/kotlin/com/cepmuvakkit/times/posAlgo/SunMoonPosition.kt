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

    init {
        val jd = AstroLib.calculateJulianDay(time)
        val tau_Sun = 8.32 / 1440.0 // 8.32 min  [cy]
        val moonPosEc = LunarPosition.calculateMoonEclipticCoordinates(jd, ΔT)
        val solarPosEc = SolarPosition.calculateSunEclipticCoordinatesAstronomic(jd - tau_Sun, ΔT)
        val E = Math.toRadians(solarPosEc.λ - moonPosEc.λ)
        val moonPosEq = LunarPosition.calculateMoonEquatorialCoordinates(moonPosEc, jd, ΔT)
        val solarPosEq = SolarPosition.calculateSunEquatorialCoordinates(solarPosEc, jd, ΔT)
        moonPosition = moonPosEq.equ2Topocentric(
            coordinates.longitude, coordinates.latitude, coordinates.elevation, jd, ΔT
        ) //az=183.5858
        sunPosition = solarPosEq.equ2Topocentric(
            coordinates.longitude, coordinates.latitude, coordinates.elevation, jd, ΔT
        )
        //System.out.println(moonPosition.Az);
        // System.out.println(moonPosition.h);

        // double E = 0;// APC_Sun.L-APC_Moon.l_Moon;//l_moon 1.4421 L=6.18064// E=4.73850629772695878
        // moonPhase = (1 + cos(pi - E)) / 2;
        moonPhase = (1 - cos(E)) / 2 //48694254279852139 e-17
        moonPhaseAscending = sin(E) < 0
    }
}
