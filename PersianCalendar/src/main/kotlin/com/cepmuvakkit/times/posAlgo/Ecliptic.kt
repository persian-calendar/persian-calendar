package com.cepmuvakkit.times.posAlgo

import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Zodiac

/**
 * @author mehmetrg
 */
class Ecliptic(
    longitude: Double, // λ: the ecliptic longitude
    latitude: Double, // β: the ecliptic latitude
    radius: Double = 0.0 // Δ: distance in km
) {
    val λ = longitude
    val β = latitude
    val Δ = radius

    // https://github.com/janczer/goMoonPhase/blob/0363844/MoonPhase.go#L363
    val zodiac: Zodiac
        get() = when {
            λ < 33.18 -> Zodiac.ARIES
            λ < 51.16 -> Zodiac.TAURUS
            λ < 93.44 -> Zodiac.GEMINI
            λ < 119.48 -> Zodiac.CANCER
            λ < 135.30 -> Zodiac.LEO
            λ < 173.34 -> Zodiac.VIRGO
            λ < 224.17 -> Zodiac.LIBRA
            λ < 242.57 -> Zodiac.SCORPIO
            λ < 271.26 -> Zodiac.SAGITTARIUS
            λ < 302.49 -> Zodiac.CAPRICORN
            λ < 311.72 -> Zodiac.AQUARIUS
            λ < 348.58 -> Zodiac.PISCES
            else -> Zodiac.ARIES
        }
}
