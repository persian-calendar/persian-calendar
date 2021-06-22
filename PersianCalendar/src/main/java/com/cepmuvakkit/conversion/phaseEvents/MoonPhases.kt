/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cepmuvakkit.conversion.phaseEvents

import com.cepmuvakkit.times.posAlgo.Ecliptic
import com.cepmuvakkit.times.posAlgo.LunarPosition
import com.cepmuvakkit.times.posAlgo.SolarPosition
import kotlin.math.floor
import kotlin.math.sqrt

class MoonPhases {
    private val solar: SolarPosition = SolarPosition()
    private val lunar: LunarPosition = LunarPosition()
    private var moonPos: Ecliptic? = null
    private var solarPos: Ecliptic? = null

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
        val tau_Sun = 8.32 / 1440.0 // 8.32 min [cy]
        moonPos = lunar.calculateMoonEclipticCoordinates(jd, ΔT)
        solarPos = solar.calculateSunEclipticCoordinatesAstronomic(
            jd - tau_Sun, ΔT
        )
        val longDiff = moonPos!!.λ - solarPos!!.λ
        if (phase == 8) // Crescent Visibility at 8 degrees Angle
        {
            val elongation = sqrt(longDiff * longDiff + moonPos!!.β * moonPos!!.β)
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
    private fun modulo(x: Double, y: Double): Double {
        return y * frac(x / y)
    }

    /**
     * Gives the fractional part of a number
     *
     * @return fractional part of a number
     */
    private fun frac(x: Double): Double {
        return x - floor(x)
    }

}
