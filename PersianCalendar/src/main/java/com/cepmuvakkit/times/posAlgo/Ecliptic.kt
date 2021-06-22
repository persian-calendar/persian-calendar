/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cepmuvakkit.times.posAlgo

/**
 * @author mehmetrg
 */
class Ecliptic {
    var λ //λ the ecliptic longitude
            = 0.0
    var β //β the ecliptic latitude
            = 0.0
    var Δ //distance  in km
            = 0.0

    internal constructor(longitude: Double, latitude: Double) {
        λ = longitude
        β = latitude
    }

    internal constructor(longitude: Double, latitude: Double, radius: Double) {
        λ = longitude
        β = latitude
        Δ = radius
    }
}
