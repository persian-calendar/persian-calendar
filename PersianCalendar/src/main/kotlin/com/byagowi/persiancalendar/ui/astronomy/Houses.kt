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
// The implementation initially was started with what was provided in
// https://github.com/cosinekitty/astronomy/discussions/340#discussioncomment-8966532
// to calculate ascendant and midheaven but changed and simplified and now supports all the 12 houses.
fun houses(latitude: Double, longitude: Double, time: Time): List<Double> {
    val rotationMatrix = rotationEctEqd(time).rot
    val cosOb = rotationMatrix[1][1]
    val sinOb = rotationMatrix[1][2]
    val tanPhi = tan(Math.toRadians(latitude))
    // Right Ascension of the Midheaven (mc)
    val ramcRad = Math.toRadians((siderealTime(time) * 15 + longitude + 360) % 360)
    val sinRamc = sin(ramcRad)
    val cosRamc = cos(ramcRad)
    val houses = DoubleArray(12)
    run {
        val mc = (Math.toDegrees(atan2(sinRamc, cosRamc * cosOb)) + 360) % 360 // Midheaven
        val dsc = (Math.toDegrees(atan2(-cosRamc, sinRamc * cosOb + tanPhi * sinOb)) + 360) % 360
        houses[1 - 1] = (dsc + 180) % 360 // Ascendant, the first house and the most important one
        houses[10 - 1] = mc
        houses[4 - 1] = (mc + 180) % 360 // Nadir or Imum Coeli (IC)
        houses[7 - 1] = dsc
    }
    houses[11 - 1] = solvePlacidusCusp(tanPhi, ramcRad, cosOb, sinOb, 1.0 / 3, false)
    houses[12 - 1] = solvePlacidusCusp(tanPhi, ramcRad, cosOb, sinOb, 2.0 / 3, false)
    houses[2 - 1] = solvePlacidusCusp(tanPhi, ramcRad, cosOb, sinOb, 2.0 / 3, true)
    houses[3 - 1] = solvePlacidusCusp(tanPhi, ramcRad, cosOb, sinOb, 1.0 / 3, true)
    houses[5 - 1] = (houses[11 - 1] + 180) % 360
    houses[6 - 1] = (houses[12 - 1] + 180) % 360
    houses[8 - 1] = (houses[2 - 1] + 180) % 360
    houses[9 - 1] = (houses[3 - 1] + 180) % 360
    return houses.toList()
}

private fun solvePlacidusCusp(
    tanPhi: Double,
    ramcRad: Double,
    cosOb: Double,
    sinOb: Double,
    cuspRatio: Double,
    isNocturnalCusp: Boolean,
): Double {
    val referenceRaRad = if (isNocturnalCusp) ramcRad + PI else ramcRad
    var cuspLonRad = atan2(sin(referenceRaRad), cos(referenceRaRad) * cosOb)
    for (i in 0..9) {
        val prevCuspLonRad = cuspLonRad
        val ad = asin((tan(asin(sin(cuspLonRad) * sinOb)) * tanPhi).coerceIn(-1.0, 1.0))
        val requiredRa = referenceRaRad + (PI / (if (isNocturnalCusp) -2 else 2) + ad) * cuspRatio
        cuspLonRad = atan2(sin(requiredRa), cos(requiredRa) * cosOb)
        if (abs(cuspLonRad - prevCuspLonRad) < 1e-6) break
    }
    return (Math.toDegrees(cuspLonRad) + 360) % 360
}
