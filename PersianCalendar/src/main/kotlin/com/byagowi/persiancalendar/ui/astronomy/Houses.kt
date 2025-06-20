package com.byagowi.persiancalendar.ui.astronomy

import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.rotationEctEqd
import io.github.cosinekitty.astronomy.siderealTime
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

// As https://github.com/cosinekitty/astronomy/discussions/340#discussioncomment-8966532
// See also Swiss Ephemeris' https://github.com/jwmatthys/pd-swisseph/blob/master/swehouse.h
// Compatibility with Placidus is desired even though it doesn't work with high altitudes
class Houses(latitude: Double, longitude: Double, time: Time) {
    private fun hoursToDegrees(hours: Double) = hours * 15
    private fun degreesToHours(degrees: Double) = degrees / 15
    private val localSiderealRadians = run {
        val greenwichSiderealTime = siderealTime(time)
        val localSiderealTime = greenwichSiderealTime + degreesToHours(longitude)
        val localSiderealDegrees = hoursToDegrees(localSiderealTime)
        Math.toRadians((localSiderealDegrees + 360) % 360)
    }
    private val rotationMatrix = rotationEctEqd(time).rot
    private val eclipticObliquityCos = rotationMatrix[1][1]
    private val eclipticObliquitySin = rotationMatrix[1][2]
    val descendant = run {
        val x = sin(localSiderealRadians) * eclipticObliquityCos +
                tan(Math.toRadians(latitude)) * eclipticObliquitySin
        val y = -cos(localSiderealRadians)
        val celestialLongitudeRadians = atan2(y, x)
        (Math.toDegrees(celestialLongitudeRadians) + 360) % 360
    }
    val ascendant = (descendant + 180) % 360
    val midheaven = run {
        val numerator = tan(localSiderealRadians)
        var midheavenDegrees = Math.toDegrees(atan2(numerator, eclipticObliquityCos))
        // Correcting the quadrant
        if (midheavenDegrees < 0) midheavenDegrees += 180
        if (midheavenDegrees < 180 && localSiderealRadians >= PI) midheavenDegrees += 180
        (midheavenDegrees + 360) % 360
    }

    // Nadir or Imum Coeli (IC)
    val ic = (midheaven + 180) % 360
}
