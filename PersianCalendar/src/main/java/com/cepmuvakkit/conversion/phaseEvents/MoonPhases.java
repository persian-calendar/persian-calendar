/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cepmuvakkit.conversion.phaseEvents;

import com.cepmuvakkit.times.posAlgo.Ecliptic;
import com.cepmuvakkit.times.posAlgo.LunarPosition;
import com.cepmuvakkit.times.posAlgo.SolarPosition;

public class MoonPhases {

	private SolarPosition solar;
	private LunarPosition lunar;
	private Ecliptic moonPos, solarPos;

	public MoonPhases() {
		solar = new SolarPosition();
		lunar = new LunarPosition();
	}

	/**
	 * searchPhaseEvent the Goal function for search of phase events [-180, 180]
	 * Difference between the longitude of the Moon from the Sun and the nominal
	 * value for a given phase (New Moon 0, First Quarter 90, New Crescent 8,
	 * First Quarter 90, Full Moon 180, Last Quarter 270)
	 * 
	 * @param jd
	 *            Julian Day
	 * @param ΔT
	 *            parameter delta-T (ΔT)
	 * @param phase
	 *            the calculated Moon Phase [in degrees] New Moon=0 New
	 *            Crescent=8 First Quarter=90 Full Moon=180 Last Quarter=270
	 * @return Difference between the longitude of the Moon from the Sun and the
	 *         nominal value for a given phase (New Moon 0, First Quarter 90,
	 *         etc.)
	 */
	public double searchPhaseEvent(double jd, double ΔT, int phase) {
		double LongDiff, tau_Sun = 8.32 / (1440.0); // 8.32 min [cy]
		moonPos = lunar.calculateMoonEclipticCoordinates(jd, ΔT);
		solarPos = solar.calculateSunEclipticCoordinatesAstronomic(
				jd - tau_Sun, ΔT);
		LongDiff = moonPos.λ - solarPos.λ;
		if (phase == 8)// Crescent Visibility at 8 degrees Angle
		{
			double elongation = Math.sqrt(LongDiff * LongDiff + moonPos.β
					* moonPos.β);
			// In case of Small angles of elongation lattitude is
			// taken into root mean square due to accuracy
			return phase - elongation;
		}
		return modulo((LongDiff - phase + 180), 360) - 180;

	}

	/**
	 * Modulo: calculates x mod y,
	 * <ul
	 * x mod y
	 * </ul>
	 * 
	 * @param double x, double y
	 * @return reminder
	 */
	private double modulo(double x, double y) {
		return y * frac(x / y);
	}

	/**
	 * Gives the fractional part of a number
	 * 
	 * @param double x
	 * @return fractional part of a number
	 */
	private double frac(double x) {
		return x - Math.floor(x);
	}
}
