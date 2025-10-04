package com.byagowi.persiancalendar.utils

import android.location.Address
import java.util.Locale
import kotlin.math.*

/**
 * Coordinate formatting and address helpers.
 *
 * This file provides several commonly-needed coordinate formatting styles
 * (ISO 6709 DMS, decimal degrees, degrees + decimal minutes) plus
 * simple geographic utilities (distance, bearing, validation).
 */

// https://stackoverflow.com/a/62499553
// https://en.wikipedia.org/wiki/ISO_6709#Representation_at_the_human_interface_(Annex_D)
fun formatCoordinateISO6709(lat: Double, lon: Double, alt: Double? = null): String {
    fun dms(value: Double): Triple<Int, Int, Int> {
        val deg = value.toInt()
        val minutes = ((abs(value - deg) * 60)).toInt()
        val seconds = ((abs(value - deg) * 3600) % 60).toInt()
        return Triple(deg, minutes, seconds)
    }

    val (latDeg, latMin, latSec) = dms(abs(lat))
    val (lonDeg, lonMin, lonSec) = dms(abs(lon))
    val latDir = if (lat >= 0) "N" else "S"
    val lonDir = if (lon >= 0) "E" else "W"

    val base = "%d°%02d′%02d″%s %d°%02d′%02d″%s".format(
        Locale.US,
        latDeg, latMin, latSec, latDir,
        lonDeg, lonMin, lonSec, lonDir
    )

    return if (alt == null) base else base + " %s%.1fm".format(Locale.US, if (alt < 0) "−" else "", abs(alt))
}

/** Friendly short name for an Address (locality > subAdminArea > adminArea). */
val Address.friendlyName: String?
    get() = listOf(locality, subAdminArea, adminArea).firstOrNull { !it.isNullOrBlank() }

/** Format coordinates as decimal degrees with 6 fractional digits. */
fun formatCoordinateDecimal(lat: Double, lon: Double): String =
    "%.6f, %.6f".format(Locale.US, lat, lon)

/** Format coordinates as Degrees and Decimal Minutes (DDM). */
fun formatCoordinateDDM(lat: Double, lon: Double): String {
    fun ddm(value: Double): Pair<Int, Double> {
        val deg = value.toInt()
        val minutes = abs((value - deg) * 60.0)
        return deg to minutes
    }

    val (latDeg, latMin) = ddm(lat)
    val (lonDeg, lonMin) = ddm(lon)
    val latDir = if (lat >= 0) "N" else "S"
    val lonDir = if (lon >= 0) "E" else "W"

    return "%d°%.3f′%s %d°%.3f′%s".format(Locale.US, abs(latDeg), latMin, latDir, abs(lonDeg), lonMin, lonDir)
}

/** Full, human-readable address description built from available Address fields. */
fun Address.fullDescription(): String = listOfNotNull(
    featureName?.takeIf { it.isNotBlank() },
    thoroughfare?.takeIf { it.isNotBlank() },
    subLocality?.takeIf { it.isNotBlank() },
    locality?.takeIf { it.isNotBlank() },
    subAdminArea?.takeIf { it.isNotBlank() },
    adminArea?.takeIf { it.isNotBlank() },
    countryName?.takeIf { it.isNotBlank() }
).joinToString(", ")

/** Country and city of Address if both present (City, Country). */
fun Address.countryAndCity(): String? {
    val city = locality ?: subAdminArea
    val country = countryName
    return if (!city.isNullOrBlank() && !country.isNullOrBlank()) "$city, $country" else null
}

/** Mixed presentation: DMS + (decimal) */
fun formatCoordinateMixed(lat: Double, lon: Double, alt: Double? = null): String {
    val dms = formatCoordinateISO6709(lat, lon, alt)
    val dec = formatCoordinateDecimal(lat, lon)
    return "$dms ($dec)"
}

/** Haversine distance between two coordinates in meters. */
fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6_371_000.0 // radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

/** Initial bearing (forward azimuth) in degrees from point A to B. */
fun bearingBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val φ1 = Math.toRadians(lat1)
    val φ2 = Math.toRadians(lat2)
    val Δλ = Math.toRadians(lon2 - lon1)
    val y = sin(Δλ) * cos(φ2)
    val x = cos(φ1) * sin(φ2) - sin(φ1) * cos(φ2) * cos(Δλ)
    return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
}

/** Validate latitude range [-90, 90]. */
fun isValidLatitude(lat: Double) = lat in -90.0..90.0

/** Validate longitude range [-180, 180]. */
fun isValidLongitude(lon: Double) = lon in -180.0..180.0

/**
 * Returns true when two coordinates are within the provided tolerance (meters).
 * Useful for fuzzy equality checks (e.g. compare device vs stored location).
 */
fun isNearby(lat1: Double, lon1: Double, lat2: Double, lon2: Double, toleranceMeters: Double = 50.0): Boolean =
    distanceBetween(lat1, lon1, lat2, lon2) <= toleranceMeters

/** Simple debug logger used by utilities (prints to stdout). */
fun debugLog(message: String) {
    println("[coords-utils] $message")
} 
