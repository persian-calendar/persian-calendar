package com.byagowi.persiancalendar.entities

import android.os.Parcelable
import com.byagowi.persiancalendar.global.language
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.parcelize.Parcelize
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

@Parcelize
data class EarthPosition(val latitude: Double, val longitude: Double) : Parcelable {
    class EarthHeading(val metres: Float, val heading: Float) {
        val km = language.formatKm((metres / 1000).roundToLong())
    }

    fun toCoordinates(): Coordinates = Coordinates(latitude, longitude, .0)

    private fun Double.toRadians(): Double = this * PI / 180.0
    private fun Double.toDegrees(): Double = this * 180.0 / PI

    fun toEarthHeading(target: EarthPosition): EarthHeading {
        val lat1 = this.latitude.toRadians()
        val lat2 = target.latitude.toRadians()
        val lon1 = this.longitude.toRadians()
        val lon2 = target.longitude.toRadians()

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        // Haversine distance on a spherical Earth (radius ~6,371 km)
        val a = sin(dLat / 2.0).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2.0).pow(2)
        val d = 2.0 * asin(sqrt(a.coerceIn(0.0, 1.0)))
        val distanceMeters = (d * 6_371_000.0).toFloat()

        // Great circle initial bearing formula
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)

        // Normalize bearing to 0.0 .. 360.0 degrees
        val bearingDegrees = ((atan2(y, x).toDegrees() + 360.0) % 360.0).toFloat()

        return EarthHeading(distanceMeters, bearingDegrees)
    }

    // Ported from https://www.movable-type.co.uk/scripts/latlong.html MIT License
    fun intermediatePoints(
        target: EarthPosition, pointsCount: Int,
    ): Sequence<EarthPosition> = sequence {
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
        repeat(pointsCount + 1) {
            val fraction = it.toDouble() / pointsCount
            val A = sin((1 - fraction) * δ) / sinδ
            val B = sin(fraction * δ) / sinδ
            val x = A * cosφ1 * cosλ1 + B * cosφ2 * cosλ2
            val y = A * cosφ1 * sinλ1 + B * cosφ2 * sinλ2
            val z = A * sinφ1 + B * sinφ2
            val φ3 = atan2(z, hypot(x, y))
            val λ3 = atan2(y, x)
            yield(EarthPosition(Math.toDegrees(φ3), Math.toDegrees(λ3)))
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
            Math.toDegrees(lon - lonRadius).coerceAtLeast(-180.0),
        ) to EarthPosition(
            Math.toDegrees(lat + latRadius).coerceAtMost(90.0),
            Math.toDegrees(lon + lonRadius).coerceAtMost(180.0),
        )
    }

    companion object {
        // https://en.wikipedia.org/wiki/Earth_radius
        const val R = 6_378_137 // Earth radius
    }
}
