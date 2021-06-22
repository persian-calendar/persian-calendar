/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cepmuvakkit.times.posAlgo

import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * @author mgeden
 */
class EarthPosition(
    val latitude: Double,
    val longitude: Double,
    val timezone: Double = (longitude / 15.0).roundToLong().toDouble(),
    private val mAltitude: Int = 0,
    private val mTemperature: Int = 10,
    private val mPressure: Int = 1010,
) {
    val altitude: Short
        get() = mAltitude.toShort()
    val pressure: Short
        get() = mPressure.toShort()
    val temperature: Short
        get() = mTemperature.toShort()

    fun toEarthHeading(target: EarthPosition): EarthHeading {
        // great circle formula from:
        // http://williams.best.vwh.net/avform.htm
        val radPerDeg = Math.PI / 180
        val lat1 = Math.toRadians(latitude) //7155849931833333333e-19 0.71
        val lat2 = Math.toRadians(target.latitude) //3737913479489224943e-19 0.373
        val lon1 = Math.toRadians(-longitude) //-5055637064497558276 e-19 -0.505
        val lon2 = Math.toRadians(-target.longitude) //-69493192920839161e-17  -0.69
        val a = sin((lat1 - lat2) / 2)
        val b = sin((lon1 - lon2) / 2)
        val d =
            2 * MATH.asin(sqrt(a * a + cos(lat1) * cos(lat2) * b * b)) //3774840207564380360e-19
        //d=2*asin(sqrt((sin((lat1-lat2)/2))^2 + cos(lat1)*cos(lat2)*(sin((lon1-lon2)/2))^2))
        // double c=a*a+Math.cos(lat1)*Math.cos(lat2))*b*b
        val tc1 = if (d > 0) {
            //tc1=acos((sin(lat2)-sin(lat1)*cos(d))/(sin(d)*cos(lat1)))
            if (sin(lon2 - lon1) < 0) {
                MATH.acos((sin(lat2) - sin(lat1) * cos(d)) / (sin(d) * cos(lat1))) //2646123918118404228e-18
            } else {
                2 * Math.PI - MATH.acos((sin(lat2) - sin(lat1) * cos(d)) / (sin(d) * cos(lat1)))
            }
        } else 0.0
        //  tc1=2*pi-acos((sin(lat2)-sin(lat1)*cos(d))/(sin(d)*cos(lat1)))
        return EarthHeading(tc1 / radPerDeg, (d * 6371000).toLong())
    }
}
