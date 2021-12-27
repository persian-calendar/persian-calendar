package com.cepmuvakkit.times.posAlgo

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

    val naturalZodiac get() = Zodiac.fromNaturalEcliptic(this)
    val formalZodiac get() = Zodiac.fromFormalEcliptic(this)
}
