package com.cepmuvakkit.times.posAlgo

import kotlin.math.cos

/**
 * @author mehmetrg
 */
class SunMoonPosition(
    jd: Double, latitude: Double, longitude: Double, altitude: Double, ΔT: Double
) {
    val sunPosition: Horizontal
    val moonPosition: Horizontal
    val moonPhase: Double
    val destinationHeading: EarthHeading
    fun illumunatedFractionofMoon(jd: Double, ΔT: Double): Double {
        return moonPhase
    }

    init {
        val earth = EarthPosition(latitude, longitude)
        val tau_Sun = 8.32 / 1440.0 // 8.32 min  [cy]
        val moonPosEc = LunarPosition.calculateMoonEclipticCoordinates(jd, ΔT)
        val solarPosEc = SolarPosition.calculateSunEclipticCoordinatesAstronomic(jd - tau_Sun, ΔT)
        val E = Math.toRadians(solarPosEc.λ - moonPosEc.λ)
        val moonPosEq = LunarPosition.calculateMoonEqutarialCoordinates(moonPosEc, jd, ΔT)
        val solarPosEq = SolarPosition.calculateSunEquatorialCoordinates(solarPosEc, jd, ΔT)
        moonPosition =
            moonPosEq.equ2Topocentric(longitude, latitude, altitude, jd, ΔT) //az=183.5858
        sunPosition = solarPosEq.equ2Topocentric(longitude, latitude, altitude, jd, ΔT)
        //System.out.println(moonPosition.Az);
        // System.out.println(moonPosition.h);

        // double E = 0;// APC_Sun.L-APC_Moon.l_Moon;//l_moon 1.4421 L=6.18064// E=4.73850629772695878
        val qibla = EarthPosition(21.416666667, 39.816666)
        destinationHeading = earth.toEarthHeading(qibla)
        // moonPhase = (1 + cos(pi - E)) / 2;
        moonPhase = (1 + cos(Math.PI - E)) / 2 //48694254279852139 e-17
        //System.out.println(qiblaInfo.getKiloMetres());
    }
}
