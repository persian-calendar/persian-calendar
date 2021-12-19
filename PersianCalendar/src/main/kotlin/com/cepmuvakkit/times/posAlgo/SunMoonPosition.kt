package com.cepmuvakkit.times.posAlgo

import com.byagowi.persiancalendar.utils.toCivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.praytimes.Coordinates
import java.util.*
import kotlin.math.cos

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
        moonPhase = (1 + cos(Math.PI - E)) / 2 //48694254279852139 e-17
        moonPhaseAscending = IslamicDate(time.toCivilDate()).dayOfMonth < 14
//        moonPhase = run { // Hacky, just that the previous wasn't able to distinguish them
//            // 0 <= phase < 2
//            // https://github.com/ilius/starcal/blob/060a0c2/scal3/moon.py
//            val d = IslamicDate(time.toCivilDate()).dayOfMonth
//            val phase = if (d >= 28) .0 else d / 14.0
//            // Southern Hemisphere, probably should handled while draw
//            // if (coordinates.isSouthernHemisphere) phase = (2.0 - phase) % 2.0
//            phase
//        }
        //System.out.println(qiblaInfo.getKiloMetres());
    }
}
