package com.byagowi.persiancalendar.ui.map

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.hardware.GeomagneticField
import androidx.annotation.RawRes
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.toPath
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.graphics.withRotation
import com.byagowi.persiancalendar.QIBLA_LATITUDE
import com.byagowi.persiancalendar.QIBLA_LONGITUDE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.EarthPosition
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.common.SolarDraw
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.toCivilDate
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Direction
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.RotationMatrix
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.Vector
import io.github.cosinekitty.astronomy.degreesToRadians
import io.github.cosinekitty.astronomy.elongation
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.geoVector
import io.github.cosinekitty.astronomy.horizon
import io.github.cosinekitty.astronomy.libration
import io.github.cosinekitty.astronomy.radiansToDegrees
import io.github.cosinekitty.astronomy.rotationEqdHor
import io.github.cosinekitty.astronomy.rotationEqjEqd
import io.github.cosinekitty.astronomy.searchRiseSet
import java.util.GregorianCalendar
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

class MapDraw(
    resources: Resources,
    private val scaleDegree: Int = 1,
    mapBackgroundColor: Int? = null,
    mapForegroundColor: Int? = null,
) {
    private val solarDraw = SolarDraw(resources)
    private val pinDrawable = resources.getDrawable(R.drawable.ic_pin, null)

    val mapScaleFactor = 16 // As the path bounds is 360x180 *16
    val mapWidth = 360 * mapScaleFactor
    val mapHeight = 180 * mapScaleFactor
    private val mapRect = Rect(0, 0, mapWidth, mapHeight)

    // Just a little wider to avoid hairline artifact between two maps in map screen
    private val backgroundMapRect = Rect(0, 0, mapWidth + 1, mapHeight)

    private fun createPathFromResourceText(resources: Resources, @RawRes id: Int): Path {
        val path = resources.openRawResource(id).readBytes().decodeToString()
        // In case Compose addPathNodes became private bring back
        // https://github.com/persian-calendar/persian-calendar/blob/5a7ff8a/PersianCalendar/src/main/kotlin/com/byagowi/persiancalendar/ui/map/PathParser.kt
        return addPathNodes(path).toPath().asAndroidPath()
    }

    private fun Path.translateBy(dx: Float, dy: Float) =
        Path().also { it.addPath(this, Matrix().apply { setTranslate(dx, dy) }) }

    private fun Path.scaleBy(sx: Float, sy: Float) =
        Path().also { it.addPath(this, Matrix().apply { setScale(sx, sy) }) }

    private val mapPath: Path = createPathFromResourceText(resources, R.raw.worldmap)
    private val timezones: Path by lazy(LazyThreadSafetyMode.NONE) {
        createPathFromResourceText(resources, R.raw.timezones)
            // `topojson['transform']` result, turn it to degrees scale
            .scaleBy(0.17586713f, 0.08793366f).translateBy(-180f, -90f)
            // Make it the same scale as mapPath
            .translateBy(180f, -90f).scaleBy(mapScaleFactor.toFloat(), -mapScaleFactor.toFloat())
    }
    private val tectonicPlates: Path by lazy(LazyThreadSafetyMode.NONE) {
        createPathFromResourceText(resources, R.raw.tectonicplates)
            // `topojson['transform']` result, turn it to degrees scale
            .scaleBy(0.17586713f, 0.074727945f)
            .translateBy(-180f, -66.1632f)
            // Make it the same scale as mapPath
            .translateBy(180f, -90f)
            .scaleBy(mapScaleFactor.toFloat(), -mapScaleFactor.toFloat())
    }
    // How the two above are created: https://gist.github.com/ebraminio/8313cff47813a5c9f98278c7ee8cde4e

    private val maskMap = createBitmap(360 / scaleDegree, 180 / scaleDegree)
    private val maskMapMoonScaleDown = 2
    private val maskMapCrescentVisibility =
        createBitmap(360 / maskMapMoonScaleDown, 180 / maskMapMoonScaleDown)
    private var maskSunX = .0f
    private var maskSunY = .0f
    private var maskMoonX = .0f
    private var maskMoonY = .0f
    var maskFormattedTime = ""
    var drawKaaba: Boolean = false

    private val kaabaIcon by lazy(LazyThreadSafetyMode.NONE) {
        resources.getDrawable(R.drawable.kaaba, null)
    }

    var markersScale = 1f

    private fun drawMask(canvas: Canvas, scale: Float) {
        when (currentMapType) {
            MapType.NONE -> Unit
            MapType.DAY_NIGHT, MapType.MOON_VISIBILITY -> {
                canvas.drawBitmap(maskMap, null, mapRect, null)
                solarDraw.plainMoon(
                    canvas,
                    maskMoonX * mapScaleFactor,
                    maskMoonY * mapScaleFactor,
                    24 * markersScale / scale,
                )
                solarDraw.sun(
                    canvas,
                    maskSunX * mapScaleFactor,
                    maskSunY * mapScaleFactor,
                    36 * markersScale / scale,
                )
            }

            MapType.MAGNETIC_INCLINATION, MapType.MAGNETIC_DECLINATION, MapType.MAGNETIC_FIELD_STRENGTH ->
                canvas.drawBitmap(maskMap, null, mapRect, null)

            MapType.TIME_ZONES -> canvas.drawPath(timezones, miscPaint)
            MapType.TECTONIC_PLATES -> canvas.drawPath(tectonicPlates, miscPaint)
            MapType.EVENING_YALLOP, MapType.EVENING_ODEH, MapType.MORNING_YALLOP, MapType.MORNING_ODEH ->
                canvas.drawBitmap(maskMapCrescentVisibility, null, mapRect, null)
        }
    }

    private val maskDateSink = GregorianCalendar().also { --it.timeInMillis }
    private var currentMapType = MapType.NONE

    fun updateMap(timeInMillis: Long, mapType: MapType) {
        if (mapType == MapType.NONE) {
            currentMapType = mapType
            maskFormattedTime = ""
            return
        }
        if (mapType == currentMapType && maskDateSink.timeInMillis == timeInMillis) return
        maskDateSink.timeInMillis = timeInMillis
        currentMapType = mapType
        when (mapType) {
            MapType.DAY_NIGHT, MapType.MOON_VISIBILITY -> {
                maskFormattedTime = maskDateSink.formatDateAndTime()
                maskMap.eraseColor(Color.TRANSPARENT)
                writeDayNightMask(timeInMillis)
            }

            MapType.MAGNETIC_FIELD_STRENGTH, MapType.MAGNETIC_DECLINATION, MapType.MAGNETIC_INCLINATION -> {
                maskFormattedTime = maskDateSink.formatDateAndTime()
                maskMap.eraseColor(Color.TRANSPARENT)
                writeMagneticMap(timeInMillis, mapType)
            }

            MapType.EVENING_YALLOP, MapType.EVENING_ODEH, MapType.MORNING_YALLOP, MapType.MORNING_ODEH -> {
                maskFormattedTime = formatDate(
                    Jdn(maskDateSink.toCivilDate()) on mainCalendar,
                    forceNonNumerical = true,
                )
                maskMapCrescentVisibility.eraseColor(Color.TRANSPARENT)
                writeCrescentVisibilityMap(maskDateSink, mapType)
            }

            else -> Unit
        }
    }

    private fun writeMagneticMap(timeInMillis: Long, mapType: MapType) {
        repeat(360) { x ->
            repeat(180) { y ->
                val latitude = 180 / 2f - y
                val longitude = x - 360 / 2f
                val field = GeomagneticField(latitude, longitude, 0f, timeInMillis)
                maskMap[x, y] = if (mapType != MapType.MAGNETIC_FIELD_STRENGTH) {
                    val value = when (mapType) {
                        MapType.MAGNETIC_DECLINATION -> field.declination
                        MapType.MAGNETIC_INCLINATION -> field.inclination
                        else -> 0f
                    }
                    when {
                        value > 1 -> ((value * 255 / 180).toInt() shl 24) + 0xFF0000
                        value < -1 -> ((-value + 255 / 180).toInt() shl 24) + 0xFF
                        else -> ((30 - abs(value) * 30).toInt() shl 24) + 0xFF00
                    }
                } else (field.fieldStrength / 68000/*25-65 μT*/ * 255).toInt() shl 24
            }
        }
    }

    private fun writeDayNightMask(timeInMillis: Long) {
        val time = Time.fromMillisecondsSince1970(timeInMillis)
        var sunMaxAltitude = .0
        var moonMaxAltitude = .0

        val geoSunEqj = geoVector(Body.Sun, time, Aberration.Corrected)
        val geoMoonEqj = geoVector(Body.Moon, time, Aberration.Corrected)
        val rot = rotationEqjEqd(time)
        val geoSunEqd = rot.rotate(geoSunEqj)
        val geoMoonEqd = rot.rotate(geoMoonEqj)

        val isMoonVisibility = currentMapType == MapType.MOON_VISIBILITY

        // https://github.com/cosinekitty/astronomy/blob/edcf9248/demo/c/worldmap.cpp
        (0..<360 step scaleDegree).forEach { x ->
            (0..<180 step scaleDegree).forEach { y ->
                val latitude = 180 / 2.0 - y
                val longitude = x - 360 / 2.0
                val observer = Observer(latitude, longitude, .0)
                val observerVec = observer.toVector(time, EquatorEpoch.OfDate)
                val observerRot = rotationEqdHor(time, observer)
                val sunAltitude = verticalComponent(observerRot, observerVec, geoSunEqd)
                val moonAltitude = verticalComponent(observerRot, observerVec, geoMoonEqd)

                if (isMoonVisibility) {
                    if (moonAltitude > 0) {
                        val value = ((moonAltitude * 90 * 7).toInt()).coerceAtMost(120)
                        // This moves the value to alpha channel so ARGB 0x0000007F becomes 0x7F000000
                        maskMap[x / scaleDegree, y / scaleDegree] = (value shl 24) + 0xF4F4F4
                    }
                } else {
                    if (sunAltitude < 0) {
                        val value = ((-sunAltitude * 90 * 7).toInt()).coerceAtMost(120)
                        // This moves the value to alpha channel so ARGB 0x0000007F becomes 0x7F000000
                        maskMap[x / scaleDegree, y / scaleDegree] = value shl 24
                    }
                }

                if (sunAltitude > sunMaxAltitude) { // find y/x of a point with maximum sun altitude
                    sunMaxAltitude = sunAltitude; maskSunX = x.toFloat(); maskSunY = y.toFloat()
                }
                if (moonAltitude > moonMaxAltitude) { // this time for moon
                    moonMaxAltitude = moonAltitude; maskMoonX = x.toFloat(); maskMoonY = y.toFloat()
                }
            }
        }
    }

    // https://github.com/cosinekitty/astronomy/blob/edcf9248/demo/c/worldmap.cpp#L122
    private fun verticalComponent(rot: RotationMatrix, oVec: Vector, bVec: Vector): Double =
        rot.rotate(bVec - oVec).let { it.z / it.length() }

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
        val bestTime = if (lagTime < 0) sunRiseSet else sunRiseSet.addDays(lagTime * 4.0 / 9 * multiplier)
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

    private fun writeCrescentVisibilityMap(date: GregorianCalendar, mapType: MapType) {
        val isYallop = mapType == MapType.MORNING_YALLOP || mapType == MapType.EVENING_YALLOP
        val isEvening = mapType == MapType.EVENING_YALLOP || mapType == MapType.EVENING_ODEH
        val baseTime = Time(
            date[GregorianCalendar.YEAR], date[GregorianCalendar.MONTH] + 1,
            date[GregorianCalendar.DAY_OF_MONTH], 0, 0, .0,
        )
        val direction = if (isEvening) Direction.Set else Direction.Rise
        val multiplier = if (isEvening) 1 else -1
        // Source https://github.com/crescent-moon-visibility/crescent-moon-visibility
        val outputWidth = 360 / maskMapMoonScaleDown
        val outputHeight = 180 / maskMapMoonScaleDown
        val heavyStep = 4 // Heavy astronomy runs once per `heavyStep` output cells, the rest are interpolated.
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
                val result = Array(count) { GeoPoint(ok = false, sunset = .0, moonset = .0, value = .0) }
                (0 until count step chunkSize).map { start ->
                    async {
                        val end = minOf(start + chunkSize, count)
                        for (index in start until end) {
                            ensureActive()
                            val gx = index % gw
                            val gy = index / gw
                            result[index] = computeGeo(
                                baseTime, direction, multiplier, isYallop,
                                latitude = 180 / 2.0 - (gy * heavyStep).coerceAtMost(outputHeight) * maskMapMoonScaleDown,
                                longitude = (gx * heavyStep).coerceAtMost(outputWidth) * maskMapMoonScaleDown - 360 / 2.0,
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
                maskMapCrescentVisibility[x, y] = classify(interpolated, multiplier, isYallop)
            }
        }
    }

    private val gridLinesWidth = mapWidth * .001f
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.strokeWidth = gridLinesWidth
        it.color = 0x80FFFFFF.toInt()
    }
    private val gridHalfPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.strokeWidth = gridLinesWidth
        it.color = 0x80808080.toInt()
    }
    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.strokeWidth = gridLinesWidth * 2
        it.style = Paint.Style.STROKE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.BLACK
        it.textSize = gridLinesWidth * 10
        it.textAlign = Paint.Align.CENTER
    }

    private val parallelsLatitudes = listOf(
        // Circles of latitude are often called parallels
        23.436806, // https://en.wikipedia.org/wiki/Tropic_of_Cancer
        -23.436806, // https://en.wikipedia.org/wiki/Tropic_of_Capricorn
        66.566667, // https://en.wikipedia.org/wiki/Arctic_Circle
        -66.566667, // https://en.wikipedia.org/wiki/Antarctic_Circle
    ).map { (90 - it.toFloat()) * mapScaleFactor }
    private val dp = resources.dp
    private val parallelsPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.strokeWidth = gridLinesWidth
        it.color = 0x80800000.toInt()
        val dashSize = 4 * dp
        it.pathEffect = DashPathEffect(floatArrayOf(dashSize, dashSize / 2), 0f)
    }

    companion object {
        val defaultBackground = androidx.compose.ui.graphics.Color(0xFF809DB5).toArgb()
        private val defaultForeground = androidx.compose.ui.graphics.Color(0xFFFBF8E5).toArgb()
        private val defaultMisc = androidx.compose.ui.graphics.Color(0x80393CC4).toArgb()
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mapBackgroundColor ?: defaultBackground
    }
    private val foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mapForegroundColor ?: defaultForeground
    }
    private val miscPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5 * dp
        color = mapForegroundColor ?: defaultMisc
    }

    private val drawEasterEggs by lazy(LazyThreadSafetyMode.NONE) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        class Entry(
            private val char: String, private val x: Float, private val y: Float,
            private val xOffset: Float = 0f, private val color: Int = Color.BLACK,
            private val textSize: Float = 10f,
        ) {
            fun draw(canvas: Canvas) {
                paint.color = color
                paint.textSize = textSize
                val userX = (x + 180) * mapScaleFactor
                val userY = (90 - y) * mapScaleFactor
                canvas.drawText(char, userX - xOffset, userY + 2.5f, paint)
            }
        }

        val list = listOf(
            Entry("🗿", -109.366f, -27.116f, xOffset = 1f, textSize = 1.5f), // Moai
            Entry("⛩", 129.3970f, 34.5897f, color = Color.RED, textSize = 1.5f), // Tsushima
            Entry("🗻", 86.92527f, 27.98833f), // Everest
            Entry("🐻‍❄️", -72.62f, 80.37f), // North geomagnetic pole
            Entry("🐧", 108.22f, -79.74f), // South geomagnetic pole
            Entry("🦘", 132.16666f, -23.03333f), // Australia pole of inaccessibility
        )
        ({ canvas: Canvas -> list.forEach { it.draw(canvas) } })
    }

    fun draw(
        canvas: Canvas,
        scale: Float,
        displayLocation: Boolean,
        coordinates: EarthPosition?,
        directPathDestination: EarthPosition?,
        displayGrid: Boolean,
    ) {
        canvas.drawRect(backgroundMapRect, backgroundPaint)
        canvas.drawPath(mapPath, foregroundPaint)
        drawMask(canvas, scale)
        if (drawKaaba) {
            val userX = (QIBLA_LONGITUDE.toFloat() + 180) * mapScaleFactor
            val userY = (90 - QIBLA_LATITUDE.toFloat()) * mapScaleFactor
            kaabaIcon.setBounds(
                (userX - 8).roundToInt(), (userY - 8).roundToInt(),
                (userX + 8).roundToInt(), (userY + 8).roundToInt(),
            )
            kaabaIcon.draw(canvas)
        }
        if (scale > 2) drawEasterEggs(canvas)
        if (coordinates != null && displayLocation) {
            val userX = (coordinates.longitude.toFloat() + 180) * mapScaleFactor
            val userY = (90 - coordinates.latitude.toFloat()) * mapScaleFactor
            pinDrawable.setBounds(
                (userX - 24 * markersScale / scale).roundToInt(),
                (userY - 44 * markersScale / scale).roundToInt(),
                (userX + 24 * markersScale / scale).roundToInt(),
                userY.toInt(),
            )
            pinDrawable.draw(canvas)
        }
        if (coordinates != null && directPathDestination != null) {
            val points = coordinates.intermediatePoints(
                directPathDestination, 24,
            ).map { (latitude, longitude) ->
                val userX = (longitude.toFloat() + 180) * mapScaleFactor
                val userY = (90 - latitude.toFloat()) * mapScaleFactor
                userX to userY
            }.toList()
            points.forEachIndexed { i, (x1, y1) ->
                if (i >= points.size - 1) return@forEachIndexed
                val (x2, y2) = points[i + 1]
                if (hypot(x2 - x1, y2 - y1) > 90 * mapScaleFactor) return@forEachIndexed
                pathPaint.color = lerp(
                    start = androidx.compose.ui.graphics.Color.Black,
                    stop = androidx.compose.ui.graphics.Color.Red,
                    fraction = i.toFloat() / points.size,
                ).toArgb()
                canvas.drawLine(x1, y1, x2, y2, pathPaint)
            }
            val center = points[points.size / 2]
            val centerPlus1 = points[points.size / 2 + 1]
            val textDegree = Math.toDegrees(
                atan2(centerPlus1.second - center.second, centerPlus1.first - center.first)
                    .toDouble(),
            ).toFloat() + if (centerPlus1.first < center.first) 180 else 0
            val heading = coordinates.toEarthHeading(directPathDestination)
            canvas.withRotation(textDegree, center.first, center.second) {
                drawText(heading.km, center.first, center.second - 2 * dp, textPaint)
            }
        }
        if (displayGrid) {
            (0..mapWidth step mapWidth / 24).forEachIndexed { i, x ->
                val paint = if (i % 12 == 0) gridHalfPaint else gridPaint
                canvas.drawLine(x.toFloat(), 0f, x.toFloat(), mapHeight.toFloat(), paint)
            }
            (0..<mapHeight step mapHeight / 12).forEachIndexed { i, y ->
                if (i == 0 || i == 6) return@forEachIndexed
                canvas.drawLine(0f, y.toFloat(), mapWidth.toFloat(), y.toFloat(), gridPaint)
            }
            canvas.drawLine(0f, mapHeight / 2f, mapWidth / 1f, mapHeight / 2f, gridHalfPaint)
            parallelsLatitudes.forEach { y ->
                canvas.drawLine(0f, y, mapWidth.toFloat(), y, parallelsPaint)
            }
        }
    }
}
