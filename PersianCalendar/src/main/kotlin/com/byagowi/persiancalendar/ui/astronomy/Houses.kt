package com.byagowi.persiancalendar.ui.astronomy

import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.rotationEctEqd
import io.github.cosinekitty.astronomy.siderealTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

// See also Swiss Ephemeris' https://github.com/jwmatthys/pd-swisseph/blob/master/swehouse.h
// Compatibility with Placidus is desired for now even though it doesn't work with high altitudes.
fun houses(latitude: Double, longitude: Double, time: Time): DoubleArray {
    val rotationMatrix = rotationEctEqd(time).rot
    val cosOb = rotationMatrix[1][1]
    val sinOb = rotationMatrix[1][2]

    // As https://github.com/cosinekitty/astronomy/discussions/340#discussioncomment-8966532
    fun hoursToDegrees(hours: Double) = hours * 15
    fun degreesToHours(degrees: Double) = degrees / 15
    val localSiderealRadians = run {
        val greenwichSiderealTime = siderealTime(time)
        val localSiderealTime = greenwichSiderealTime + degreesToHours(longitude)
        val localSiderealDegrees = hoursToDegrees(localSiderealTime)
        Math.toRadians((localSiderealDegrees + 360) % 360)
    }

    val mc = run {
        val numerator = tan(localSiderealRadians)
        var midheavenDegrees = Math.toDegrees(atan2(numerator, cosOb))
        if (midheavenDegrees < 0) midheavenDegrees += 180
        if (midheavenDegrees < 180 && localSiderealRadians >= PI) midheavenDegrees += 180
        (midheavenDegrees + 360) % 360
    }

    val dsc = run {
        val x = sin(localSiderealRadians) * cosOb + tan(Math.toRadians(latitude)) * sinOb
        val y = -cos(localSiderealRadians)
        val celestialLongitudeRadians = atan2(y, x)
        (Math.toDegrees(celestialLongitudeRadians) + 360) % 360
    }

    val ic = (mc + 180) % 360
    val asc = (dsc + 180) % 360

    val houses = DoubleArray(12)
    houses[1 - 1] = asc
    houses[10 - 1] = mc

    houses[11 - 1] = solvePlacidusCusp(latitude, localSiderealRadians, cosOb, sinOb, 1.0 / 3, false)
    houses[12 - 1] = solvePlacidusCusp(latitude, localSiderealRadians, cosOb, sinOb, 2.0 / 3, false)
    houses[2 - 1] = solvePlacidusCusp(latitude, localSiderealRadians, cosOb, sinOb, 2.0 / 3, true)
    houses[3 - 1] = solvePlacidusCusp(latitude, localSiderealRadians, cosOb, sinOb, 1.0 / 3, true)

    houses[4 - 1] = ic
    houses[7 - 1] = dsc
    houses[5 - 1] = (houses[11 - 1] + 180) % 360
    houses[6 - 1] = (houses[12 - 1] + 180) % 360
    houses[8 - 1] = (houses[2 - 1] + 180) % 360
    houses[9 - 1] = (houses[3 - 1] + 180) % 360

    return houses
}

private fun solvePlacidusCusp(
    latitudeDegrees: Double,
    ramcRad: Double,
    cosOb: Double,
    sinOb: Double,
    cuspRatio: Double,
    isNocturnalCusp: Boolean,
): Double {
    val phi = Math.toRadians(latitudeDegrees)

    val referenceRaRad = if (isNocturnalCusp) (ramcRad + PI) else ramcRad

    var cuspLonRad = atan2(tan(referenceRaRad), cosOb)
    if (cos(referenceRaRad) < 0) cuspLonRad += PI

    for (i in 0 until 20) {
        val prevCuspLonRad = cuspLonRad

        val sinDec = sin(cuspLonRad) * sinOb
        val decRad = asin(sinDec)

        var adArg = tan(decRad) * tan(phi)
        if (adArg > 1.0) adArg = 1.0
        if (adArg < -1.0) adArg = -1.0
        val ascensionalDifference = asin(adArg)

        val requiredRa = if (isNocturnalCusp) {
            val semiNocturnalArc = PI / 2.0 - ascensionalDifference
            referenceRaRad - semiNocturnalArc * cuspRatio
        } else {
            val semiDiurnalArc = PI / 2.0 + ascensionalDifference
            referenceRaRad + semiDiurnalArc * cuspRatio
        }

        cuspLonRad = atan2(tan(requiredRa), cosOb)

        if (cos(requiredRa) < 0) cuspLonRad += PI

        cuspLonRad = (cuspLonRad + 2 * PI) % (2 * PI)

        val diff = abs(cuspLonRad - prevCuspLonRad)
        if (diff < 1e-6 || abs(diff - 2 * PI) < 1e-6) break
    }

    return (Math.toDegrees(cuspLonRad) + 360) % 360
}
