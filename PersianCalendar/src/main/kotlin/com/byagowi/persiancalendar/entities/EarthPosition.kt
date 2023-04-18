package com.byagowi.persiancalendar.entities

import android.location.Location
import java.util.Locale
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class EarthPosition(val latitude: Double, val longitude: Double) {
    class EarthHeading(metres: Float, val heading: Float) {
        val km = "%,d km".format(Locale.ENGLISH, (metres / 1000).roundToInt())
    }

    fun toEarthHeading(target: EarthPosition): EarthHeading {
        val result = FloatArray(3)
        Location.distanceBetween(latitude, longitude, target.latitude, target.longitude, result)
        return EarthHeading(result[0], result[1])
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
            val φ3 = atan2(z, hypot(x, y))
            val λ3 = atan2(y, x)
            EarthPosition(Math.toDegrees(φ3), Math.toDegrees(λ3))
        }
    }

    /**
     * rectangular bounds of a certain point
     * @param radius is in km
     */
    fun rectangularBoundsOfRadius(radius: Double): Pair<EarthPosition, EarthPosition> {
        // https://github.com/openstreetmap/openstreetmap-website/blob/e72acac/lib/osm.rb#L452
        val lat = Math.toRadians(latitude)
        val lon = Math.toRadians(longitude)
        val latRadius = 2 * asin(sqrt(sin(radius / (R / 1000) / 2).pow(2)))
        val lonRadius = runCatching {
            2 * asin(sqrt(sin(radius / (R / 1000) / 2).pow(2) / cos(lat).pow(2)))
        }.getOrNull() ?: PI
        return EarthPosition(
            Math.toDegrees(lat - latRadius).coerceAtLeast(-90.0),
            Math.toDegrees(lon - lonRadius).coerceAtLeast(-180.0)
        ) to EarthPosition(
            Math.toDegrees(lat + latRadius).coerceAtMost(90.0),
            Math.toDegrees(lon + lonRadius).coerceAtMost(180.0)
        )
    }

    companion object {
        // https://en.wikipedia.org/wiki/Earth_radius
        const val R = 6_378_137 // Earth radius
    }
}
