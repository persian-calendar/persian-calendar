package com.cepmuvakkit.times.posAlgo

import kotlin.math.floor
import kotlin.math.hypot

object MoonPhases {
    /**
     * searchPhaseEvent the Goal function for search of phase events [-180, 180]
     * Difference between the longitude of the Moon from the Sun and the nominal
     * value for a given phase (New Moon 0, First Quarter 90, New Crescent 8,
     * First Quarter 90, Full Moon 180, Last Quarter 270)
     *
     * @param jd    Julian Day
     * @param ΔT    parameter delta-T (ΔT)
     * @param phase the calculated Moon Phase [in degrees] New Moon=0 New
     * Crescent=8 First Quarter=90 Full Moon=180 Last Quarter=270
     * @return Difference between the longitude of the Moon from the Sun and the
     * nominal value for a given phase (New Moon 0, First Quarter 90,
     * etc.)
     */
    fun searchPhaseEvent(jd: Double, ΔT: Double, phase: Int): Double {
        val tauSun = 8.32 / 1440.0 // 8.32 min [cy]
        val moonPos = LunarPosition.calculateMoonEclipticCoordinates(jd, ΔT)
        val solarPos = SolarPosition.calculateSunEclipticCoordinatesAstronomic(jd - tauSun, ΔT)
        val longDiff = moonPos.λ - solarPos.λ
        if (phase == 8) { // Crescent Visibility at 8 degrees Angle
            val elongation = hypot(longDiff, moonPos.β)
            // In case of Small angles of elongation lattitude is
            // taken into root mean square due to accuracy
            return phase - elongation
        }
        return modulo(longDiff - phase + 180, 360.0) - 180
    }

    /**
     * Modulo: calculates x mod y,
     *
     * @return reminder
     */
    private fun modulo(x: Double, y: Double): Double = y * frac(x / y)

    /**
     * Gives the fractional part of a number
     *
     * @return fractional part of a number
     */
    private fun frac(x: Double): Double = x - floor(x)
}
