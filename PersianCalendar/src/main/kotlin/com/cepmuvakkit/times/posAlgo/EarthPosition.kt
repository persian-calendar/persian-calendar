package com.cepmuvakkit.times.posAlgo

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * @author mgeden
 */
class EarthPosition(
    val latitude: Double, val longitude: Double,
    val timezone: Double = round(longitude / 15.0),
    val altitude: Int = 0, val temperature: Int = 10, val pressure: Int = 1010
) {

    fun toEarthHeading(target: EarthPosition): EarthHeading {
        // great circle formula from:
        // http://williams.best.vwh.net/avform.htm
        val lat1 = Math.toRadians(latitude) //7155849931833333333e-19 0.71
        val lat2 = Math.toRadians(target.latitude) //3737913479489224943e-19 0.373
        val lon1 = Math.toRadians(-longitude) //-5055637064497558276 e-19 -0.505
        val lon2 = Math.toRadians(-target.longitude) //-69493192920839161e-17  -0.69
        val a = sin((lat1 - lat2) / 2)
        val b = sin((lon1 - lon2) / 2)
        // https://en.wikipedia.org/wiki/Haversine_formula
        val d = 2 * asin(sqrt(a * a + cos(lat1) * cos(lat2) * b * b)) //3774840207564380360e-19
        //d=2*asin(sqrt((sin((lat1-lat2)/2))^2 + cos(lat1)*cos(lat2)*(sin((lon1-lon2)/2))^2))
        // double c=a*a+Math.cos(lat1)*Math.cos(lat2))*b*b
        val tc1 = if (d > 0) {
            //tc1=acos((sin(lat2)-sin(lat1)*cos(d))/(sin(d)*cos(lat1)))
            val x = acos((sin(lat2) - sin(lat1) * cos(d)) / (sin(d) * cos(lat1)))
            /*2646123918118404228e-18*/
            if (sin(lon2 - lon1) < 0) x else 2 * PI - x
        } else 0.0
        //  tc1=2*pi-acos((sin(lat2)-sin(lat1)*cos(d))/(sin(d)*cos(lat1)))
        val radPerDeg = PI / 180
        return EarthHeading(tc1 / radPerDeg, (d * 6371e3).toLong())
    }

    // Ported from https://www.movable-type.co.uk/scripts/latlong.html MIT License
    fun intermediatePoints(target: EarthPosition, pointsCount: Int): List<EarthPosition> {
        val φ1 = Math.toRadians(latitude)
        val λ1 = Math.toRadians(longitude)
        val φ2 = Math.toRadians(target.latitude)
        val λ2 = Math.toRadians(target.longitude)
        // distance between points
        val Δφ = φ2 - φ1
        val Δλ = λ2 - λ1
        val cosφ1 = cos(φ1)
        val cosφ2 = cos(φ2)
        val cosλ1 = cos(λ1)
        val cosλ2 = cos(λ2)
        val sinλ1 = sin(λ1)
        val sinλ2 = sin(λ2)
        val sinφ1 = sin(φ1)
        val sinφ2 = sin(φ2)
        val a = sin(Δφ / 2) * sin(Δφ / 2) + cosφ1 * cosφ2 * sin(Δλ / 2) * sin(Δλ / 2)
        val δ = 2 * atan2(sqrt(a), sqrt(1 - a))
        val sinδ = sin(δ)
        return (0..pointsCount).map {
            val fraction = it.toDouble() / pointsCount
            val A = sin((1 - fraction) * δ) / sinδ
            val B = sin(fraction * δ) / sinδ
            val x = A * cosφ1 * cosλ1 + B * cosφ2 * cosλ2
            val y = A * cosφ1 * sinλ1 + B * cosφ2 * sinλ2
            val z = A * sinφ1 + B * sinφ2
            val φ3 = atan2(z, sqrt(x * x + y * y))
            val λ3 = atan2(y, x)
            EarthPosition(Math.toDegrees(φ3), Math.toDegrees(λ3))
        }
    }

    companion object {
        const val R = 6371e3 // Earth radius
    }
}
