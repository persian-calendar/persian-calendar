package com.cepmuvakkit.times.posAlgo

/**
 * @author mehmetrg
 */
class Ecliptic @JvmOverloads constructor(
    longitude: Double, // λ: the ecliptic longitude
    latitude: Double, // β: the ecliptic latitude
    radius: Double = 0.0 // Δ: distance in km
) {
    @JvmField
    val λ = longitude

    @JvmField
    val β = latitude

    @JvmField
    val Δ = radius
}
