/* Copyright (C) 2009 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0.
 * https://www.apache.org/licenses/LICENSE-2.0
 * Port of android-16.0.0_r1 GeomagneticField.java (WMM-2020).
 * Provided AS IS, without warranties or conditions of any kind.
 */
package com.byagowi.persiancalendar.shared

import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

internal data class MagneticField(val north: Double, val east: Double, val down: Double) {
    val declination get() = atan2(east, north) * 180 / PI
    val inclination get() = atan2(down, hypot(north, east)) * 180 / PI
    val strength get() = sqrt(north * north + east * east + down * down)
}

internal fun magneticField(latitude: Double, longitude: Double, altitude: Double, epochMillis: Double): MagneticField {
    val lat = latitude.coerceIn(-89.99999, 89.99999) * PI / 180
    val lon = longitude * PI / 180
    val h = altitude / 1000
    val a2 = 6378.137.pow(2)
    val b2 = 6356.7523142.pow(2)
    val c = cos(lat)
    val s = sin(lat)
    val radiusAtLatitude = sqrt(a2 * c * c + b2 * s * s)
    val gcLat = atan(tan(lat) * (radiusAtLatitude * h + b2) / (radiusAtLatitude * h + a2))
    val radius = sqrt(h * h + 2 * h * radiusAtLatitude + (a2 * a2 * c * c + b2 * b2 * s * s) / (a2 * c * c + b2 * s * s))
    val count = G_COEFF.size
    val p = Array(count) { DoubleArray(it + 1) }
    val derivative = Array(count) { DoubleArray(it + 1) }
    val norm = Array(count) { DoubleArray(it + 1) }
    p[0][0] = 1.0
    norm[0][0] = 1.0
    val ct = cos(PI / 2 - gcLat)
    val st = sin(PI / 2 - gcLat)
    for (n in 1 until count) {
        norm[n][0] = norm[n - 1][0] * (2 * n - 1) / n
        for (m in 0..n) {
            if (m > 0) norm[n][m] = norm[n][m - 1] * sqrt((n - m + 1) * (if (m == 1) 2.0 else 1.0) / (n + m))
            when {
                n == m -> { p[n][m] = st * p[n - 1][m - 1]; derivative[n][m] = ct * p[n - 1][m - 1] + st * derivative[n - 1][m - 1] }
                n == 1 || m == n - 1 -> { p[n][m] = ct * p[n - 1][m]; derivative[n][m] = -st * p[n - 1][m] + ct * derivative[n - 1][m] }
                else -> {
                    val k = ((n - 1) * (n - 1) - m * m).toDouble() / ((2 * n - 1) * (2 * n - 3))
                    p[n][m] = ct * p[n - 1][m] - k * p[n - 2][m]
                    derivative[n][m] = -st * p[n - 1][m] + ct * derivative[n - 1][m] - k * derivative[n - 2][m]
                }
            }
        }
    }
    val years = (epochMillis - 1577836800000.0) / (365 * 86400000.0)
    var x = 0.0; var y = 0.0; var z = 0.0
    for (n in 1 until count) {
        for (m in 0..n) {
        val g = G_COEFF[n][m] + years * DELTA_G[n][m]
        val hh = H_COEFF[n][m] + years * DELTA_H[n][m]
        val power = (6371.2 / radius).pow(n + 2)
        val sum = g * cos(m * lon) + hh * sin(m * lon)
        x += power * sum * derivative[n][m] * norm[n][m]
        y += power * m * (g * sin(m * lon) - hh * cos(m * lon)) * p[n][m] * norm[n][m] / cos(gcLat)
        z -= (n + 1) * power * sum * p[n][m] * norm[n][m]
    }
    }
    val difference = lat - gcLat
    return MagneticField(x * cos(difference) + z * sin(difference), y, -x * sin(difference) + z * cos(difference))
}
