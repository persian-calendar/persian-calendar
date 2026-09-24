package com.byagowi.persiancalendar.ui.map

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Direction
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.degreesToRadians
import io.github.cosinekitty.astronomy.elongation
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.horizon
import io.github.cosinekitty.astronomy.libration
import io.github.cosinekitty.astronomy.radiansToDegrees
import io.github.cosinekitty.astronomy.searchRiseSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.util.GregorianCalendar
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.time.Duration.Companion.seconds

// Source https://github.com/crescent-moon-visibility/crescent-moon-visibility
class CrescentVisibilityMap {
    private val scaleDown = 2
    val bitmap: Bitmap = createBitmap(360 / scaleDown, 180 / scaleDown)

    fun update(date: GregorianCalendar, mapType: MapType) {
        bitmap.eraseColor(Color.TRANSPARENT)
        render(date, mapType)
    }

    private data class GeoPoint(
        val ok: Boolean,
        val sunset: Double,
        val moonset: Double,
        val value: Double,
    )

    // The expensive part of a single map cell: the two rise/set searches plus the
    // astrometric value. Classification is deferred to `classify` so it can run
    // after bilinear interpolation on the coarse grid.
    private fun computeGeo(
        baseTime: Time,
        direction: Direction,
        multiplier: Int,
        isYallop: Boolean,
        latitude: Double,
        longitude: Double,
    ): GeoPoint {
        val observer = Observer(latitude, longitude, .0)
        val time = baseTime.addDays(-longitude / 360)
        val sunRiseSet = searchRiseSet(Body.Sun, observer, direction, time, 1.0)
            ?: return GeoPoint(ok = false, sunset = .0, moonset = .0, value = .0)
        val moonRiseSet = searchRiseSet(Body.Moon, observer, direction, time, 1.0)
            ?: return GeoPoint(ok = false, sunset = .0, moonset = .0, value = .0)
        val sunset = sunRiseSet.ut
        val moonset = moonRiseSet.ut
        val lagTime = (moonset - sunset) * multiplier
        // For lagTime < 0 the cell is colored red; still compute the value at
        // sunset so it stays continuous across the lag == 0 boundary.
        val bestTime =
            if (lagTime < 0) sunRiseSet else sunRiseSet.addDays(lagTime * 4.0 / 9 * multiplier)
        val sunEquator = equator(
            Body.Sun, bestTime, observer, EquatorEpoch.OfDate, Aberration.Corrected,
        )
        val sunHorizon =
            horizon(bestTime, observer, sunEquator.ra, sunEquator.dec, Refraction.None)
        val sunAz = sunHorizon.azimuth
        val moonEquator = equator(
            Body.Moon, bestTime, observer, EquatorEpoch.OfDate, Aberration.Corrected,
        )
        val liberation = libration(bestTime)
        val moonHorizon =
            horizon(bestTime, observer, moonEquator.ra, moonEquator.dec, Refraction.None)
        val moonAlt = moonHorizon.altitude
        val moonAz = moonHorizon.azimuth
        val SD = liberation.diamDeg * 60 / 2
        val lunarParallax = SD / 0.27245
        val SD_topo =
            SD * (1 + (sin(moonAlt.degreesToRadians()) * sin((lunarParallax / 60).degreesToRadians())))
        val ARCL = if (isYallop) elongation(Body.Moon, bestTime).elongation
        else sunEquator.vec.angleWith(moonEquator.vec)
        val DAZ = sunAz - moonAz
        val ARCV = acos(
            cos(ARCL.degreesToRadians()) / cos(DAZ.degreesToRadians()).coerceIn(-1.0, 1.0),
        ).radiansToDegrees()
        val W_topo = SD_topo * (1 - (cos(ARCL.degreesToRadians())))
        val value = if (isYallop) {
            (ARCV - (11.8371 - 6.3226 * W_topo + .7319 * W_topo.pow(2) - .1018 * W_topo.pow(3))) / 10
        } else {
            ARCV - (7.1651 - 6.3226 * W_topo + .7319 * W_topo.pow(2) - .1018 * W_topo.pow(3))
        }
        return GeoPoint(ok = true, sunset = sunset, moonset = moonset, value = value)
    }

    // Cheap: turns an (interpolated) point into a color, mirroring the old inline logic.
    private fun classify(point: GeoPoint, multiplier: Int, isYallop: Boolean): Int {
        if (!point.ok) return Color.TRANSPARENT
        val lagTime = (point.moonset - point.sunset) * multiplier
        if (lagTime < 0) return 0x70FF0000
        return if (isYallop) {
            when {
                point.value > .216 -> 0x7F3EFF00 // Crescent easily visible
                point.value > -.014 -> 0x7F3EFF6D // Crescent visible under perfect conditions
                point.value > -.160 -> 0x7F00FF9E // May need optical aid to find crescent
                point.value > -.232 -> 0x7F00FFFA // Will need optical aid to find crescent
                point.value > -.293 -> 0x7F3C78FF // Crescent not visible with telescope
                else -> Color.TRANSPARENT
            }
        } else {
            when {
                point.value >= 5.65 -> 0x7F3EFF00 // Crescent is visible by naked eye
                point.value >= 2.00 -> 0x7F00FF9E // Crescent is visible by optical aid
                point.value >= -.96 -> 0x7F3C78FF // Crescent is visible only by optical aid
                else -> Color.TRANSPARENT
            }
        }
    }

    private fun render(date: GregorianCalendar, mapType: MapType) {
        val isYallop = mapType == MapType.MORNING_YALLOP || mapType == MapType.EVENING_YALLOP
        val isEvening = mapType == MapType.EVENING_YALLOP || mapType == MapType.EVENING_ODEH
        val baseTime = Time(
            date[GregorianCalendar.YEAR], date[GregorianCalendar.MONTH] + 1,
            date[GregorianCalendar.DAY_OF_MONTH], 0, 0, .0,
        )
        val direction = if (isEvening) Direction.Set else Direction.Rise
        val multiplier = if (isEvening) 1 else -1
        val outputWidth = 360 / scaleDown
        val outputHeight = 180 / scaleDown
        val heavyStep =
            4 // Heavy astronomy runs once per `heavyStep` output cells, the rest are interpolated.
        val gw = outputWidth / heavyStep + 1
        val gh = outputHeight / heavyStep + 1

        // Evaluate the expensive astronomy on a coarse grid, in parallel like the C
        // version's OpenMP loop. The astronomy calls are pure, so this is thread-safe;
        // the interpolation below is cheap and stays on the calling thread.
        val grid = runBlocking(Dispatchers.Default) {
            withTimeoutOrNull(5.seconds) {
                val count = gw * gh
                val threads = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
                val chunkSize = (count + threads - 1) / threads
                val result =
                    Array(count) { GeoPoint(ok = false, sunset = .0, moonset = .0, value = .0) }
                (0..<count step chunkSize).map { start ->
                    async {
                        ensureActive()
                        val end = minOf(start + chunkSize, count)
                        (start..<end).forEach { index ->
                            val gx = index % gw
                            val gy = index / gw
                            result[index] = computeGeo(
                                baseTime, direction, multiplier, isYallop,
                                latitude = 180 / 2.0 - (gy * heavyStep).coerceAtMost(outputHeight) * scaleDown,
                                longitude = (gx * heavyStep).coerceAtMost(outputWidth) * scaleDown - 360 / 2.0,
                            )
                        }
                    }
                }.awaitAll()
                result
            }
        } ?: return

        repeat(outputHeight) { y ->
            val gy0 = y / heavyStep
            val fy = (y - gy0 * heavyStep) / heavyStep.toDouble()
            val row0 = gy0 * gw
            val row1 = (gy0 + 1).coerceAtMost(gh - 1) * gw
            repeat(outputWidth) { x ->
                val gx0 = x / heavyStep
                val gx1 = (gx0 + 1).coerceAtMost(gw - 1)
                val fx = (x - gx0 * heavyStep) / heavyStep.toDouble()
                val w00 = (1 - fx) * (1 - fy)
                val w10 = fx * (1 - fy)
                val w01 = (1 - fx) * fy
                val w11 = fx * fy
                val p00 = grid[row0 + gx0]
                val p10 = grid[row0 + gx1]
                val p01 = grid[row1 + gx0]
                val p11 = grid[row1 + gx1]
                val oksum = w00 * (if (p00.ok) 1.0 else 0.0) + w10 * (if (p10.ok) 1.0 else 0.0) +
                        w01 * (if (p01.ok) 1.0 else 0.0) + w11 * (if (p11.ok) 1.0 else 0.0)
                val interpolated = if (oksum > 0.0) GeoPoint(
                    ok = oksum >= 0.5,
                    sunset = (w00 * p00.sunset + w10 * p10.sunset + w01 * p01.sunset + w11 * p11.sunset) / oksum,
                    moonset = (w00 * p00.moonset + w10 * p10.moonset + w01 * p01.moonset + w11 * p11.moonset) / oksum,
                    value = (w00 * p00.value + w10 * p10.value + w01 * p01.value + w11 * p11.value) / oksum,
                ) else GeoPoint(ok = false, sunset = .0, moonset = .0, value = .0)
                bitmap[x, y] = classify(interpolated, multiplier, isYallop)
            }
        }
    }
}
