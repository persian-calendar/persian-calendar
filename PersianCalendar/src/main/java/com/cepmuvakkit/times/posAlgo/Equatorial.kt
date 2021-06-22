/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cepmuvakkit.times.posAlgo

class Equatorial {
    var α //right ascension (α) -also RA-, or hour angle (H) -also HA-
            = 0.0
    var δ //declination (δ)
            = 0.0
    var Δ //distance to the earth(Δ) in km
            = 0.0

    internal constructor() {}
    internal constructor(sunRightAscension: Double, sunDeclination: Double) {
        α = sunRightAscension
        δ = sunDeclination
    }

    internal constructor(sunRightAscension: Double, sunDeclination: Double, radius: Double) {
        α = sunRightAscension
        δ = sunDeclination
        Δ = radius
    }

    fun Equ2Topocentric(
        longitude: Double,
        latitude: Double,
        Height: Double,
        jd: Double,
        ΔT: Double
    ): Horizontal {
        val ϕ = Math.toRadians(latitude)
        val ρsinϕPr = ρsinϕPrime(ϕ, Height)
        val ρCosϕPr = ρCosϕPrime(ϕ, Height)

        //Calculate the Sidereal time

        //double ΔT = AstroLib.calculateTimeDifference(jd);
        val theta: Double = SolarPosition.Companion.calculateGreenwichSiderealTime(jd, ΔT)

        //Convert to radians
        val δrad = Math.toRadians(δ)
        val cosδ = Math.cos(δrad)
        //  4.26345151167726E-5
        //Calculate the Parallax
        val π = getHorizontalParallax(Δ)
        val sinπ = Math.sin(π)

        //Calculate the hour angle
        val H = Math.toRadians(AstroLib.limitDegrees(theta + longitude - α))
        val cosH = Math.cos(H)
        val sinH = Math.sin(H)

        //Calculate the adjustment in right ascension
        val Δα = MATH.atan2(-ρCosϕPr * sinπ * sinH, cosδ - ρCosϕPr * sinπ * cosH)
        val horizontal = Horizontal()
        //  CAA2DCoordinate Topocentric;
        //    double αPrime =Math.toRadians(α)+Δα;
        val δPrime = MATH.atan2(
            (Math.sin(δrad) - ρsinϕPr * sinπ) * Math.cos(Δα),
            cosδ - ρCosϕPr * sinπ * cosH
        )
        val HPrime = H - Δα
        horizontal.azimuth = Math.toDegrees(
            MATH.atan2(
                Math.sin(HPrime),
                Math.cos(HPrime) * Math.sin(ϕ) - Math.tan(δPrime) * Math.cos(ϕ)
            ) + Math.PI
        )
        horizontal.elevation = Math.toDegrees(
            MATH.asin(
                Math.sin(ϕ) * Math.sin(δPrime) + Math.cos(ϕ) * Math.cos(δPrime) * Math.cos(HPrime)
            )
        )
        return horizontal
    }

    fun ρsinϕPrime(ϕ: Double, Height: Double): Double {
        val U = MATH.atan(0.99664719 * Math.tan(ϕ))
        return 0.99664719 * Math.sin(U) + Height / 6378149 * Math.sin(ϕ)
    }

    fun ρCosϕPrime(ϕ: Double, Height: Double): Double {
        //Convert from degress to radians
        val U = MATH.atan(0.99664719 * Math.tan(ϕ))
        return Math.cos(U) + Height / 6378149 * Math.cos(ϕ)
    }

    fun getHorizontalParallax(RadiusVector: Double): Double {
        return MATH.asin(6378.14 / RadiusVector)
    }
}
