package com.byagowi.persiancalendar.ui.map

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.core.graphics.PathParser
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withRotation
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentMapBinding
import com.byagowi.persiancalendar.entities.EarthPosition
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.astronomy.AstronomyViewModel
import com.byagowi.persiancalendar.ui.common.ArrowView
import com.byagowi.persiancalendar.ui.common.SolarDraw
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showCoordinatesDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showGPSLocationDialog
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupLayoutTransition
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.byagowi.persiancalendar.ui.utils.viewKeeper
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.RotationMatrix
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.Vector
import io.github.cosinekitty.astronomy.geoVector
import io.github.cosinekitty.astronomy.rotationEqdHor
import io.github.cosinekitty.astronomy.rotationEqjEqd
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.util.*
import java.util.zip.GZIPInputStream
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt


class MapScreen : Fragment(R.layout.fragment_map) {
    private val binding by viewKeeper(FragmentMapBinding::bind)
    private val directPathButton by viewKeeper { binding.appBar.toolbar.menu.findItem(R.id.menu_direct_path) }
    private val gridButton by viewKeeper { binding.appBar.toolbar.menu.findItem(R.id.menu_grid) }
    private val myLocationButton by viewKeeper { binding.appBar.toolbar.menu.findItem(R.id.menu_my_location) }
    private val locationButton by viewKeeper { binding.appBar.toolbar.menu.findItem(R.id.menu_location) }
    private val maskTypeButton by viewKeeper { binding.appBar.toolbar.menu.findItem(R.id.menu_night_mask) }
    private val globeViewButton by viewKeeper { binding.appBar.toolbar.menu.findItem(R.id.menu_globe_view) }

    private val viewModel by navGraphViewModels<MapViewModel>(R.id.map)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Don't set the title as we got lots of icons
        // binding.appBar.toolbar.setTitle(R.string.map)
        binding.appBar.toolbar.setupUpNavigation()

        // Set time from Astronomy screen state if we are brought from the screen to here directly
        if (findNavController().previousBackStackEntry?.destination?.id == R.id.astronomy) {
            val astronomyViewModel by navGraphViewModels<AstronomyViewModel>(R.id.astronomy)
            viewModel.changeToTime(astronomyViewModel.astronomyState.value.date.time)
            // Let's apply changes here to astronomy screen's view model also
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.state
                    .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                    .collectLatest { state -> astronomyViewModel.changeToTime(state.time) }
            }
        }

        mapMask.solarDraw = SolarDraw(view.context)
        val zippedMapPath = resources.openRawResource(R.raw.worldmap).use { it.readBytes() }
        val mapPathBytes = GZIPInputStream(ByteArrayInputStream(zippedMapPath)).readBytes()
        val mapPath = PathParser.createPathFromPathData(mapPathBytes.decodeToString())

        pinDrawable = view.context.getCompatDrawable(R.drawable.ic_pin)

        binding.startArrow.rotateTo(ArrowView.Direction.START)
        binding.startArrow.setOnClickListener {
            binding.startArrow.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            viewModel.subtractOneHour()
        }
        binding.startArrow.setOnLongClickListener {
            viewModel.subtractTenDays()
            true
        }
        binding.endArrow.rotateTo(ArrowView.Direction.END)
        binding.endArrow.setOnClickListener {
            binding.endArrow.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            viewModel.addOneHour()
        }
        binding.endArrow.setOnLongClickListener {
            viewModel.addTenDays()
            true
        }

        binding.appBar.toolbar.inflateMenu(R.menu.map_menu)
        directPathButton.onClick {
            if (coordinates == null) bringGps() else viewModel.toggleDirectPathMode()
        }
        gridButton.onClick { viewModel.toggleDisplayGrid() }
        myLocationButton.onClick { bringGps() }
        locationButton.onClick {
            if (coordinates == null) bringGps() else viewModel.toggleDisplayLocation()
        }
        maskTypeButton.onClick {
            if (viewModel.state.value.maskType == MaskType.None) {
                MaterialAlertDialogBuilder(context ?: return@onClick)
                    .setItems(enumValues<MaskType>().map { it.toString() }.toTypedArray()) { d, i ->
                        viewModel.changeMaskType(enumValues<MaskType>()[i])
                        d.dismiss()
                    }
                    .show()
            } else {
                viewModel.changeMaskType(MaskType.None)
            }
        }
        globeViewButton.onClick {
            val textureSize = 1024
            val bitmap = createBitmap(textureSize, textureSize)
            val matrix = Matrix().also {
                it.setScale(textureSize.toFloat() / mapWidth, textureSize.toFloat() / mapHeight)
            }
            binding.map.onDraw(Canvas(bitmap), matrix)
            showGlobeDialog(activity ?: return@onClick, bitmap)
        }

        binding.root.setupLayoutTransition()
        view.context.appPrefs.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == PREF_LATITUDE) viewModel.turnOnDisplayLocation()
        }

        binding.map.onClick = { x: Float, y: Float ->
            val latitude = 90 - y / mapScaleFactor
            val longitude = x / mapScaleFactor - 180
            if (abs(latitude) < 90 && abs(longitude) < 180) onMapClick(latitude, longitude)
        }

        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF809DB5.toInt() }
        val foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFBF8E5.toInt() }
        val matrixValues = FloatArray(9)
        binding.map.onDraw = { canvas, matrix ->
            matrix.getValues(matrixValues)
            // prevents sun/moon/pin unnecessary scale
            val scaleBack = 1 / matrixValues[Matrix.MSCALE_X] / 5
            canvas.withMatrix(matrix) {
                drawRect(mapRect, backgroundPaint)
                drawPath(mapPath, foregroundPaint)

                mapMask.draw(this, scaleBack)
                val coordinates = coordinates
                if (coordinates != null && viewModel.state.value.displayLocation) {
                    val userX = (coordinates.longitude.toFloat() + 180) * mapScaleFactor
                    val userY = (90 - coordinates.latitude.toFloat()) * mapScaleFactor
                    pinDrawable.setBounds(
                        (userX - 240 * scaleBack / 2).roundToInt(),
                        (userY - 220 * scaleBack).roundToInt(),
                        (userX + 240 * scaleBack / 2).roundToInt(),
                        userY.toInt()
                    )
                    pinDrawable.draw(this)
                }
                val directPathDestination = viewModel.state.value.directPathDestination
                if (coordinates != null && directPathDestination != null) {
                    val from = EarthPosition(coordinates.latitude, coordinates.longitude)
                    val to = EarthPosition(
                        directPathDestination.latitude,
                        directPathDestination.longitude
                    )
                    val points = from.intermediatePoints(to, 24).map { point ->
                        val userX = (point.longitude.toFloat() + 180) * mapScaleFactor
                        val userY = (90 - point.latitude.toFloat()) * mapScaleFactor
                        userX to userY
                    }
                    points.forEachIndexed { i, (x1, y1) ->
                        if (i >= points.size - 1) return@forEachIndexed
                        val (x2, y2) = points[i + 1]
                        if (hypot(x2 - x1, y2 - y1) > 90 * mapScaleFactor) return@forEachIndexed
                        pathPaint.color = ArgbEvaluatorCompat.getInstance().evaluate(
                            i.toFloat() / points.size, Color.BLACK, Color.RED
                        )
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
                        drawText(heading.km, center.first, center.second - 2.dp, textPaint)
                    }
                }
                if (viewModel.state.value.displayGrid) {
                    (0 until mapWidth step mapWidth / 24).forEachIndexed { i, x ->
                        if (i == 0 || i == 12) return@forEachIndexed
                        drawLine(x.toFloat(), 0f, x.toFloat(), mapHeight.toFloat(), gridPaint)
                    }
                    (0 until mapHeight step mapHeight / 12).forEachIndexed { i, y ->
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
        binding.map.contentWidth = mapWidth.toFloat()
        binding.map.contentHeight = mapHeight.toFloat()
        binding.map.maxScale = 512f

        // Setup view model change listener
        // https://developer.android.com/topic/libraries/architecture/coroutines#lifecycle-aware
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest { state ->
                    mapMask.update(state.time, state.maskType)
                    binding.map.invalidate()
                    binding.date.text = mapMask.formattedTime
                    binding.timeBar.isVisible = mapMask.formattedTime.isNotEmpty()
                    directPathButton.icon?.alpha = if (state.isDirectPathMode) 127 else 255
                }
        }
    }

    private fun onMapClick(latitude: Float, longitude: Float) {
        // Easter egg like feature, bring sky renderer fragment
        if (latitude.absoluteValue < 2 && longitude.absoluteValue < 2 && viewModel.state.value.displayGrid) {
            findNavController().navigateSafe(MapScreenDirections.actionMapToSkyRenderer())
            return
        }
        activity?.also {
            val coordinates = Coordinates(latitude.toDouble(), longitude.toDouble(), 0.0)
            if (viewModel.state.value.isDirectPathMode) {
                viewModel.changeDirectPathDestination(coordinates)
            } else {
                showCoordinatesDialog(it, viewLifecycleOwner, coordinates)
            }
        }
    }

    private fun bringGps() {
        showGPSLocationDialog(activity ?: return, viewLifecycleOwner)
    }

    private val mapScaleFactor = 16 // As the path bounds is 360*16 x 180*16
    private val mapWidth = 360 * mapScaleFactor
    private val mapHeight = 180 * mapScaleFactor
    private val mapRect = Rect(0, 0, mapWidth, mapHeight)

    inner class MapMask {
        private val nightMaskScale = 1
        private val nightMask = createBitmap(360 / nightMaskScale, 180 / nightMaskScale)
        private var sunX = .0f
        private var sunY = .0f
        private var moonX = .0f
        private var moonY = .0f
        var solarDraw: SolarDraw? = null
        var formattedTime = ""

        fun draw(canvas: Canvas, matrixScale: Float) {
            if (currentMaskType == MaskType.None) return
            canvas.drawBitmap(nightMask, null, mapRect, null)
            val scale = mapWidth / nightMask.width
            val solarDraw = solarDraw ?: return
            solarDraw.simpleMoon(
                canvas, moonX * scale, moonY * scale, mapWidth * .02f * matrixScale
            )
            solarDraw.sun(canvas, sunX * scale, sunY * scale, mapWidth * .025f * matrixScale)
        }

        private val dateSink =
            GregorianCalendar().also { it.add(Calendar.DATE, -1) } // for initial update
        private var currentMaskType = MaskType.None
        fun update(timeInMillis: Long, maskType: MaskType) {
            if (maskType == MaskType.None) {
                currentMaskType = maskType
                formattedTime = ""
                return
            }
            if (maskType == currentMaskType && dateSink.timeInMillis == timeInMillis) return
            dateSink.timeInMillis = timeInMillis
            formattedTime = dateSink.formatDateAndTime()
            currentMaskType = maskType

            val time = Time.fromMillisecondsSince1970(timeInMillis)
            nightMask.eraseColor(Color.TRANSPARENT)
            var sunMaxAltitude = .0
            var moonMaxAltitude = .0

            val geoSunEqj = geoVector(Body.Sun, time, Aberration.Corrected)
            val geoMoonEqj = geoVector(Body.Moon, time, Aberration.Corrected)
            val rot = rotationEqjEqd(time)
            val geoSunEqd = rot.rotate(geoSunEqj)
            val geoMoonEqd = rot.rotate(geoMoonEqj)

            // https://github.com/cosinekitty/astronomy/blob/edcf9248/demo/c/worldmap.cpp
            (0 until nightMask.width).forEach { x ->
                (0 until nightMask.height).forEach { y ->
                    val latitude = ((nightMask.height / 2 - y) * nightMaskScale).toDouble()
                    val longitude = ((x - nightMask.width / 2) * nightMaskScale).toDouble()
                    val observer = Observer(latitude, longitude, .0)
                    val observerVec = observer.toVector(time, EquatorEpoch.OfDate)
                    val observerRot = rotationEqdHor(time, observer)
                    val sunAltitude = verticalComponent(observerRot, observerVec, geoSunEqd)
                    val moonAltitude = verticalComponent(observerRot, observerVec, geoMoonEqd)

                    if (sunAltitude < 0) {
                        val value = ((-sunAltitude * 90 * 7).toInt()).coerceAtMost(120)
                        // This move the value to alpha channel so ARGB 0x0000007F becomes 0x7F000000
                        nightMask[x, y] = value shl 24
                    }

                    if (sunAltitude > sunMaxAltitude) { // find y/x of a point with maximum sun altitude
                        sunMaxAltitude = sunAltitude; sunX = x.toFloat(); sunY = y.toFloat()
                    }
                    if (moonAltitude > moonMaxAltitude) { // this time for moon
                        moonMaxAltitude = moonAltitude; moonX = x.toFloat(); moonY = y.toFloat()
                    }
                }
            }
        }

        // https://github.com/cosinekitty/astronomy/blob/edcf9248/demo/c/worldmap.cpp#L122
        private fun verticalComponent(rot: RotationMatrix, oVec: Vector, bVec: Vector): Double =
            rot.rotate(bVec - oVec).let { it.z / it.length() }
    }

    private val mapMask = MapMask()

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

    private var pinDrawable: Drawable = ShapeDrawable()
}
