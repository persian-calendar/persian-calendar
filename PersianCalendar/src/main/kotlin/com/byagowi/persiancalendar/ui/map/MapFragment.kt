package com.byagowi.persiancalendar.ui.map

import android.animation.LayoutTransition
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.BitmapCompat
import androidx.core.graphics.PathParser
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.set
import androidx.core.graphics.withScale
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentMapBinding
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.preferences.locationathan.location.showGPSLocationDialog
import com.byagowi.persiancalendar.ui.shared.ArrowView
import com.byagowi.persiancalendar.ui.shared.SolarDraw
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.cepmuvakkit.times.posAlgo.SunMoonPositionForMap
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.util.*
import java.util.zip.GZIPInputStream

class MapFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMapBinding.inflate(inflater)
        binding.appBar.toolbar.let {
            it.setTitle(R.string.map)
            it.setupUpNavigation()
        }

        solarDraw = SolarDraw(layoutInflater.context)
        pinBitmap = inflater.context.getCompatDrawable(R.drawable.ic_pin).toBitmap(120, 110)

        val args by navArgs<MapFragmentArgs>()
        val date = GregorianCalendar().also { it.add(Calendar.MINUTE, args.minutesOffset) }

        update(binding, date)

        binding.startArrow.rotateTo(ArrowView.Direction.START)
        binding.startArrow.setOnClickListener {
            binding.startArrow.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            date.add(Calendar.HOUR, -1)
            update(binding, date)
        }
        binding.startArrow.setOnLongClickListener {
            date.add(Calendar.DATE, -1)
            update(binding, date)
            true
        }
        binding.endArrow.rotateTo(ArrowView.Direction.END)
        binding.endArrow.setOnClickListener {
            binding.endArrow.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            date.add(Calendar.HOUR, 1)
            update(binding, date)
        }
        binding.endArrow.setOnLongClickListener {
            date.add(Calendar.DATE, 1)
            update(binding, date)
            true
        }

        binding.appBar.toolbar.menu.add("Grid").also {
            it.icon = binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_grid_3x3)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }.onClick {
            displayGrid = !displayGrid
            update(binding, date)
        }
        fun bringGps() {
            showGPSLocationDialog(activity ?: return, viewLifecycleOwner)
        }
        binding.appBar.toolbar.menu.add("GPS").also {
            it.icon = binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_my_location)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }.onClick { bringGps() }
        binding.appBar.toolbar.menu.add("Location").also {
            it.icon = binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_location_on)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }.onClick {
            if (coordinates == null) bringGps()
            displayLocation = !displayLocation; update(binding, date)
        }
        binding.appBar.toolbar.menu.add("Night Mask").also {
            it.icon = binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_nightlight)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }.onClick {
            displayNightMask = !displayNightMask
            binding.timeBar.isVisible = displayNightMask
            update(binding, date)
        }
        binding.root.layoutTransition = LayoutTransition().also {
            it.enableTransitionType(LayoutTransition.APPEARING)
            it.setAnimateParentHierarchy(false)
        }
        inflater.context.appPrefs.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == PREF_LATITUDE) {
                displayLocation = true
                update(binding, date)
            }
        }

        return binding.root
    }

    private var displayNightMask = true
    private var displayLocation = true
    private var displayGrid = false

    private fun update(binding: FragmentMapBinding, date: GregorianCalendar) {
        binding.map.setImageBitmap(createMap(date))
        binding.date.text = date.formatDateAndTime()
    }

    private val scaleDownFactor = 4
    private val mapScaleFactor = 16 / scaleDownFactor
    private val sinkWidth = 360 * mapScaleFactor
    private val sinkHeight = 180 * mapScaleFactor
    private val sinkBitmap = Bitmap.createBitmap(sinkWidth, sinkHeight, Bitmap.Config.ARGB_8888)
    private var referenceBuffer: ByteBuffer? = null
    private fun createReferenceBuffer(): ByteBuffer {
        val zippedMapPath = resources.openRawResource(R.raw.worldmap).use { it.readBytes() }
        val mapPathString = String(GZIPInputStream(ByteArrayInputStream(zippedMapPath)).readBytes())
        val mapPath = PathParser.createPathFromPathData(mapPathString)
        // We assume creating reference map will be first use of sink also.
        Canvas(sinkBitmap).also {
            it.drawColor(0xFF809DB5.toInt())
            it.withScale(1f / scaleDownFactor, 1f / scaleDownFactor) {
                it.drawPath(mapPath, Paint().apply { color = 0xFFFBF8E5.toInt() })
            }
        }
        val buffer = ByteBuffer.allocate(BitmapCompat.getAllocationByteCount(sinkBitmap))
        sinkBitmap.copyPixelsToBuffer(buffer)
        return buffer
    }

    private fun getSinkBitmap(): Bitmap {
        val src = referenceBuffer ?: createReferenceBuffer().also { referenceBuffer = it }
        sinkBitmap.copyPixelsFromBuffer(src.also { it.rewind() })
        return sinkBitmap
    }

    private var solarDraw: SolarDraw? = null

    private val nightMaskScale = 2
    private val nightMask = Bitmap.createBitmap(
        360 / nightMaskScale, 180 / nightMaskScale, Bitmap.Config.ARGB_8888
    )

    private fun createMap(date: GregorianCalendar): Bitmap {
        val sink = getSinkBitmap()
        nightMask.eraseColor(Color.TRANSPARENT)
        val sunPosition = SunMoonPositionForMap(date)
        var sunX = .0f
        var sunY = .0f
        var sunAlt = .0
        var moonX = .0f
        var moonY = .0f
        var moonAlt = .0
        (0 until nightMask.width).forEach { x ->
            if (!displayNightMask) return@forEach
            (0 until nightMask.height).forEach { y ->
                val latitude = ((nightMask.height / 2 - y) * nightMaskScale).toDouble()
                val longitude = ((x - nightMask.width / 2) * nightMaskScale).toDouble()
                val sunAltitude = sunPosition.sunAltitude(latitude, longitude)
                if (sunAltitude < 0) nightMask[x, y] =
                    (-sunAltitude.toInt()).coerceAtMost(17) * 7 shl 24
                if (sunAltitude > sunAlt) { // find y/x of a point with maximum sun altitude
                    sunAlt = sunAltitude; sunX = x.toFloat(); sunY = y.toFloat()
                }
                val moonAltitude = sunPosition.moonAltitude(latitude, longitude)
                if (moonAltitude > moonAlt) { // this time for moon
                    moonAlt = moonAltitude; moonX = x.toFloat(); moonY = y.toFloat()
                }
            }
        }
        val userX = coordinates?.run { (longitude.toFloat() + 180) * mapScaleFactor }
        val userY = coordinates?.run { (90 - latitude.toFloat()) * mapScaleFactor }
        Canvas(sink).also {
            it.drawBitmap(nightMask, null, Rect(0, 0, sink.width, sink.height), null)
            val scale = sink.width / nightMask.width
            if (displayGrid) {
                (0 until sink.width step sink.width / 24).forEachIndexed { i, x ->
                    if (i == 0 || i == 12) return@forEachIndexed
                    it.drawLine(x.toFloat(), 0f, x.toFloat(), sink.height.toFloat(), gridPaint)
                }
                (0 until sink.height step sink.height / 12).forEachIndexed { i, y ->
                    if (i == 0 || i == 6) return@forEachIndexed
                    it.drawLine(0f, y.toFloat(), sink.width.toFloat(), y.toFloat(), gridPaint)
                }
                it.drawLine(sink.width / 2f, 0f, sink.width / 2f, sink.height / 1f, gridHalfPaint)
                it.drawLine(0f, sink.height / 2f, sink.width / 1f, sink.height / 2f, gridHalfPaint)
            }
            val solarDraw = solarDraw ?: return@also
            if (displayNightMask) {
                solarDraw.simpleMoon(it, moonX * scale, moonY * scale, sink.width * .02f)
                solarDraw.sun(it, sunX * scale, sunY * scale, sink.width * .025f)
            }
            if (userX != null && userY != null && displayLocation) {
                pinRect.set(
                    userX - pinBitmap.width / 2f / pinScaleDown,
                    userY - pinBitmap.height / pinScaleDown,
                    userX + pinBitmap.width / 2f / pinScaleDown,
                    userY
                )
                it.drawBitmap(pinBitmap, null, pinRect, null)
            }
        }
        return sink
    }

    private val gridPaint = Paint().also {
        it.strokeWidth = sinkWidth * .002f
        it.color = 0x80FFFFFF.toInt()
    }
    private val gridHalfPaint = Paint().also {
        it.strokeWidth = sinkWidth * .002f
        it.color = 0x80808080.toInt()
    }
    private val pinScaleDown = 2
    private val pinRect = RectF()
    private var pinBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
}
