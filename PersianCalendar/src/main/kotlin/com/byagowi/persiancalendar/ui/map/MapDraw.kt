package com.byagowi.persiancalendar.ui.map

import android.animation.ArgbEvaluator
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
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.toPath
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withRotation
import com.byagowi.persiancalendar.QIBLA_LATITUDE
import com.byagowi.persiancalendar.QIBLA_LONGITUDE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.EarthPosition
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.common.SolarDraw
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.scaleBy
import com.byagowi.persiancalendar.ui.utils.translateBy
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
import io.github.persiancalendar.praytimes.Coordinates
import java.util.GregorianCalendar
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

class MapDraw(
    resources: Resources, mapBackgroundColor: Int? = null, mapForegroundColor: Int? = null
) {
    private val solarDraw = SolarDraw(resources)
    private val pinDrawable = resources.getDrawable(R.drawable.ic_pin, null)

    val mapScaleFactor = 16 // As the path bounds is 360x180 *16
    val mapWidth = 360 * mapScaleFactor
    val mapHeight = 180 * mapScaleFactor
    private val mapRect = Rect(0, 0, mapWidth, mapHeight)

    private fun createPathFromResourceText(resources: Resources, @RawRes id: Int): Path {
        val path = resources.openRawResource(id).readBytes().decodeToString()
        // In case Compose addPathNodes became private bring back
        // https://github.com/persian-calendar/persian-calendar/blob/5a7ff8a/PersianCalendar/src/main/kotlin/com/byagowi/persiancalendar/ui/map/PathParser.kt
        return addPathNodes(path).toPath().asAndroidPath()
    }

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

    private val maskMap = createBitmap(360, 180)
    private val maskMapMoonScaleDown = 8
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

    private fun drawMask(canvas: Canvas, matrixScale: Float) {
        when (currentMapType) {
            MapType.None -> Unit
            MapType.DayNight, MapType.MoonVisibility -> {
                canvas.drawBitmap(maskMap, null, mapRect, null)
                val scale = mapWidth / maskMap.width
                solarDraw.simpleMoon(
                    canvas,
                    maskMoonX * scale,
                    maskMoonY * scale,
                    mapWidth * .02f * matrixScale * markersScale
                )
                solarDraw.sun(
                    canvas,
                    maskSunX * scale,
                    maskSunY * scale,
                    mapWidth * .025f * matrixScale * markersScale
                )
            }

            MapType.MagneticInclination, MapType.MagneticDeclination, MapType.MagneticFieldStrength ->
                canvas.drawBitmap(maskMap, null, mapRect, null)

            MapType.TimeZones -> canvas.drawPath(timezones, miscPaint)
            MapType.TectonicPlates -> canvas.drawPath(tectonicPlates, miscPaint)
            MapType.Yallop, MapType.Odeh ->
                canvas.drawBitmap(maskMapCrescentVisibility, null, mapRect, null)
        }
    }

    private val maskDateSink = GregorianCalendar().also { --it.timeInMillis }
    var currentMapType = MapType.None
        private set

    fun updateMap(timeInMillis: Long, mapType: MapType) {
        if (mapType == MapType.None) {
            currentMapType = mapType
            maskFormattedTime = ""
            return
        }
        if (mapType == currentMapType && maskDateSink.timeInMillis == timeInMillis) return
        maskDateSink.timeInMillis = timeInMillis
        currentMapType = mapType
        when (mapType) {
            MapType.DayNight, MapType.MoonVisibility -> {
                maskFormattedTime = maskDateSink.formatDateAndTime()
                maskMap.eraseColor(Color.TRANSPARENT)
                writeDayNightMask(timeInMillis)
            }

            MapType.MagneticFieldStrength,
            MapType.MagneticDeclination,
            MapType.MagneticInclination -> {
                maskFormattedTime = maskDateSink.formatDateAndTime()
                maskMap.eraseColor(Color.TRANSPARENT)
                writeMagneticMap(timeInMillis, mapType)
            }

            MapType.Yallop, MapType.Odeh -> {
                maskFormattedTime = formatDate(
                    Jdn(maskDateSink.toCivilDate()).toCalendar(mainCalendar),
                    forceNonNumerical = true
                )
                maskMapCrescentVisibility.eraseColor(Color.TRANSPARENT)
                writeCrescentVisibilityMap(maskDateSink, mapType)
            }

            else -> Unit
        }
    }

    private fun writeMagneticMap(timeInMillis: Long, mapType: MapType) {
        (0..<360).forEach { x ->
            (0..<180).forEach { y ->
                val latitude = 180 / 2f - y
                val longitude = x - 360 / 2f
                val field = GeomagneticField(latitude, longitude, 0f, timeInMillis)
                maskMap[x, y] = if (mapType != MapType.MagneticFieldStrength) {
                    val value = when (mapType) {
                        MapType.MagneticDeclination -> field.declination
                        MapType.MagneticInclination -> field.inclination
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

        val isMoonVisibility = currentMapType == MapType.MoonVisibility

        // https://github.com/cosinekitty/astronomy/blob/edcf9248/demo/c/worldmap.cpp
        (0..<360).forEach { x ->
            (0..<180).forEach { y ->
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
                        maskMap[x, y] = (value shl 24) + 0xF4F4F4
                    }
                } else {
                    if (sunAltitude < 0) {
                        val value = ((-sunAltitude * 90 * 7).toInt()).coerceAtMost(120)
                        // This moves the value to alpha channel so ARGB 0x0000007F becomes 0x7F000000
                        maskMap[x, y] = value shl 24
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

    private fun writeCrescentVisibilityMap(date: GregorianCalendar, mapType: MapType) {
        val isYallop = mapType == MapType.Yallop
        val baseTime = Time(
            date[GregorianCalendar.YEAR], date[GregorianCalendar.MONTH] + 1,
            date[GregorianCalendar.DAY_OF_MONTH] + 1, 0, 0, .0
        )
        // Source https://github.com/crescent-moon-visibility/crescent-moon-visibility
        (0..<360 / maskMapMoonScaleDown).forEach { x ->
            (0..<180 / maskMapMoonScaleDown).forEach heightForEach@{ y ->
                val latitude = 180 / 2.0 - y * maskMapMoonScaleDown
                val longitude = x * maskMapMoonScaleDown - 360 / 2.0
                val observer = Observer(latitude, longitude, .0)
                val time = baseTime.addDays(-longitude / 360)
                val sunset = searchRiseSet(Body.Sun, observer, Direction.Set, time, 1.0)
                val moonset = searchRiseSet(Body.Moon, observer, Direction.Set, time, 1.0)
                if (sunset == null || moonset == null) return@heightForEach
                val lagTime = moonset.ut - sunset.ut
                if (lagTime < 0) {
                    maskMapCrescentVisibility[x, y] = 0x70FF0000
                    return@heightForEach
                }
                val bestTime = sunset.addDays(lagTime * 4.0 / 9)
                val sunEquator = equator(
                    Body.Sun, bestTime, observer, EquatorEpoch.OfDate, Aberration.Corrected
                )
                val sunHorizon =
                    horizon(bestTime, observer, sunEquator.ra, sunEquator.dec, Refraction.None)
                val sunAz = sunHorizon.azimuth
                val moonEquator = equator(
                    Body.Moon, bestTime, observer, EquatorEpoch.OfDate, Aberration.Corrected
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
                    cos(ARCL.degreesToRadians()) / cos(DAZ.degreesToRadians()).coerceIn(-1.0, 1.0)
                ).radiansToDegrees()
                val W_topo = SD_topo * (1 - (cos(ARCL.degreesToRadians())))
                if (isYallop) {
                    val q = (ARCV - (11.8371 - 6.3226 * W_topo + .7319 * W_topo.pow(2)
                            - .1018 * W_topo.pow(3))) / 10
                    maskMapCrescentVisibility[x, y] = when {
                        q > +.216 -> 0x7F3EFF00 // Crescent easily visible
                        q > -.014 -> 0x7F3EFF6D // Crescent visible under perfect conditions
                        q > -.160 -> 0x7F00FF9E // May need optical aid to find crescent
                        q > -.232 -> 0x7F00FFFA // Will need optical aid to find crescent
                        q > -.293 -> 0x7F3C78FF // Crescent not visible with telescope
                        else -> Color.TRANSPARENT
                    }
                } else {
                    val V = ARCV - (7.1651 - 6.3226 * W_topo + .7319 * W_topo.pow(2)
                            - .1018 * W_topo.pow(3))
                    maskMapCrescentVisibility[x, y] = when {
                        V >= 5.65 -> 0x7F3EFF00 // Crescent is visible by naked eye
                        V >= 2.00 -> 0x7F00FF9E // Crescent is visible by optical aid
                        V >= -.96 -> 0x7F3C78FF // Crescent is visible only by optical aid
                        else -> Color.TRANSPARENT
                    }
                }
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
    private val textPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG).also {
        it.color = Color.BLACK
        it.textSize = gridLinesWidth * 10
        it.textAlign = Paint.Align.CENTER
    }
    private val moaiPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG).also {
        it.color = Color.BLACK
        it.textSize = 1.5f
        it.textAlign = Paint.Align.CENTER
    }

    private val parallelsLatitudes = listOf(
        // Circles of latitude are often called parallels
        23.436806, // https://en.wikipedia.org/wiki/Tropic_of_Cancer
        -23.436806, // https://en.wikipedia.org/wiki/Tropic_of_Capricorn
        66.566667, // https://en.wikipedia.org/wiki/Arctic_Circle
        -66.566667, // https://en.wikipedia.org/wiki/Antarctic_Circle
    ).map { (90 - it.toFloat()) * mapScaleFactor }
    private val parallelsPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.strokeWidth = gridLinesWidth
        it.color = 0x80800000.toInt()
        it.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mapBackgroundColor ?: 0xFF809DB5.toInt()
    }
    private val foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mapForegroundColor ?: 0xFFFBF8E5.toInt()
    }
    private val dp = resources.dp
    private val miscPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5 * dp
        color = mapForegroundColor ?: 0x80393CC4.toInt()
    }
    private val matrixProperties = FloatArray(9)
    private val argbEvaluator = ArgbEvaluator()

    fun draw(
        canvas: Canvas, matrix: Matrix,
        displayLocation: Boolean, directPathDestination: Coordinates?, displayGrid: Boolean
    ) {
        matrix.getValues(matrixProperties)
        // prevents sun/moon/pin unnecessary scale
        val scaleBack = 1 / matrixProperties[Matrix.MSCALE_X] / 5
        canvas.withMatrix(matrix) {
            drawRect(mapRect, backgroundPaint)
            drawPath(mapPath, foregroundPaint)

            drawMask(this, scaleBack)
            if (drawKaaba) {
                val userX = (QIBLA_LONGITUDE.toFloat() + 180) * mapScaleFactor
                val userY = (90 - QIBLA_LATITUDE.toFloat()) * mapScaleFactor
                kaabaIcon.setBounds(
                    (userX - 8).roundToInt(), (userY - 8).roundToInt(),
                    (userX + 8).roundToInt(), (userY + 8).roundToInt(),
                )
                kaabaIcon.draw(this)
            }
            if (scaleBack < .1) {
                val userX = (-109.366f + 180) * mapScaleFactor
                val userY = (90 - -27.116f) * mapScaleFactor
                drawText("🗿", userX - 1f, userY + 2.5f, moaiPaint)
            }
            val coordinates = coordinates.value
            if (coordinates != null && displayLocation) {
                val userX = (coordinates.longitude.toFloat() + 180) * mapScaleFactor
                val userY = (90 - coordinates.latitude.toFloat()) * mapScaleFactor
                pinDrawable.setBounds(
                    (userX - 240 * markersScale * scaleBack / 2).roundToInt(),
                    (userY - 220 * markersScale * scaleBack).roundToInt(),
                    (userX + 240 * markersScale * scaleBack / 2).roundToInt(),
                    userY.toInt()
                )
                pinDrawable.draw(this)
            }
            if (coordinates != null && directPathDestination != null) {
                val from = EarthPosition(coordinates.latitude, coordinates.longitude)
                val to = EarthPosition(
                    directPathDestination.latitude,
                    directPathDestination.longitude
                )
                val points = from.intermediatePoints(to, 24).map { (latitude, longitude) ->
                    val userX = (longitude.toFloat() + 180) * mapScaleFactor
                    val userY = (90 - latitude.toFloat()) * mapScaleFactor
                    userX to userY
                }.toList()
                points.forEachIndexed { i, (x1, y1) ->
                    if (i >= points.size - 1) return@forEachIndexed
                    val (x2, y2) = points[i + 1]
                    if (hypot(x2 - x1, y2 - y1) > 90 * mapScaleFactor) return@forEachIndexed
                    pathPaint.color = (argbEvaluator.evaluate(
                        i.toFloat() / points.size, Color.BLACK, Color.RED
                    ) as? Int) ?: 0
                    drawLine(x1, y1, x2, y2, pathPaint)
                }
                val center = points[points.size / 2]
                val centerPlus1 = points[points.size / 2 + 1]
                val textDegree = Math.toDegrees(
                    atan2(centerPlus1.second - center.second, centerPlus1.first - center.first)
                        .toDouble()
                ).toFloat() + if (centerPlus1.first < center.first) 180 else 0
                val heading = from.toEarthHeading(to)
                withRotation(textDegree, center.first, center.second) {
                    drawText(heading.km, center.first, center.second - 2 * dp, textPaint)
                }
            }
            if (displayGrid) {
                (0..<mapWidth step mapWidth / 24).forEachIndexed { i, x ->
                    if (i == 0 || i == 12) return@forEachIndexed
                    drawLine(x.toFloat(), 0f, x.toFloat(), mapHeight.toFloat(), gridPaint)
                }
                (0..<mapHeight step mapHeight / 12).forEachIndexed { i, y ->
                    if (i == 0 || i == 6) return@forEachIndexed
                    drawLine(0f, y.toFloat(), mapWidth.toFloat(), y.toFloat(), gridPaint)
                }
                drawLine(mapWidth / 2f, 0f, mapWidth / 2f, mapHeight / 1f, gridHalfPaint)
                drawLine(0f, mapHeight / 2f, mapWidth / 1f, mapHeight / 2f, gridHalfPaint)
                parallelsLatitudes.forEach { y ->
                    drawLine(0f, y, mapWidth.toFloat(), y, parallelsPaint)
                }
            }
        }
    }
}
