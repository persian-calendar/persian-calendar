/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cepmuvakkit.times.posAlgo

/**
 * @author mgeden
 */
class EarthPosition {
    val latitude: Double
    val longitude: Double
    val timezone: Double
    private val mAltitude: Int
    private val mTemperature: Int
    private val mPressure: Int

    constructor(latitude: Double, longitude: Double) : this(
        latitude,
        longitude,
        Math.round(longitude / 15.0).toDouble(),
        0,
        10,
        1010
    ) {
    }

    @JvmOverloads
    constructor(
        latitude: Double = 32.85,
        longitude: Double = 39.95,
        timezone: Double = 2.0,
        altitude: Int = 10,
        temperature: Int = 1010,
        pressure: Int = 0
    ) {
        this.latitude = latitude
        this.longitude = longitude
        this.timezone = timezone
        mTemperature = temperature
        mPressure = pressure
        mAltitude = altitude
    }

    constructor(
        latitude: Float,
        longitude: Float,
        timezone: Float,
        altitude: Int,
        temperature: Int,
        pressure: Int
    ) {
        this.latitude = latitude.toDouble()
        this.longitude = longitude.toDouble()
        this.timezone = timezone.toDouble()
        mTemperature = temperature
        mPressure = pressure
        mAltitude = altitude
    }

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
        val a = Math.sin((lat1 - lat2) / 2)
        val b = Math.sin((lon1 - lon2) / 2)
        val d =
            2 * MATH.asin(Math.sqrt(a * a + Math.cos(lat1) * Math.cos(lat2) * b * b)) //3774840207564380360e-19
        //d=2*asin(sqrt((sin((lat1-lat2)/2))^2 + cos(lat1)*cos(lat2)*(sin((lon1-lon2)/2))^2))
        var tc1 = 0.0
        // double c=a*a+Math.cos(lat1)*Math.cos(lat2))*b*b
        if (d > 0) {
            tc1 =
                if (Math.sin(lon2 - lon1) < 0) //tc1=acos((sin(lat2)-sin(lat1)*cos(d))/(sin(d)*cos(lat1)))
                {
                    MATH.acos(
                        (Math.sin(lat2) - Math.sin(
                            lat1
                        ) * Math.cos(d)) / (Math.sin(d) * Math.cos(lat1))
                    ) //2646123918118404228e-18
                } else {
                    2 * Math.PI - MATH.acos(
                        (Math.sin(lat2) - Math.sin(
                            lat1
                        ) * Math.cos(d)) / (Math.sin(d) * Math.cos(lat1))
                    )
                }
        }
        //  tc1=2*pi-acos((sin(lat2)-sin(lat1)*cos(d))/(sin(d)*cos(lat1)))
        return EarthHeading(tc1 / radPerDeg, (d * 6371000).toLong())
    }
}
