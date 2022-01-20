package com.byagowi.persiancalendar.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import androidx.annotation.WorkerThread
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.PathParser
import androidx.core.graphics.set
import androidx.core.graphics.withScale
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.Variants.debugAssertNotNull
import com.byagowi.persiancalendar.databinding.FragmentMapBinding
import com.byagowi.persiancalendar.ui.shared.ArrowView
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.logException
import com.cepmuvakkit.times.posAlgo.SunMoonPositionForMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
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

        val args by navArgs<MapFragmentArgs>()
        val date = GregorianCalendar().also { it.add(Calendar.MINUTE, args.minutesOffset) }

        update(binding, date)

        binding.startArrow.rotateTo(ArrowView.Direction.START)
        binding.startArrow.setOnClickListener {
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
            date.add(Calendar.HOUR, 1)
            update(binding, date)
        }
        binding.endArrow.setOnLongClickListener {
            date.add(Calendar.DATE, 1)
            update(binding, date)
            true
        }

        return binding.root
    }

    private fun update(binding: FragmentMapBinding, date: GregorianCalendar) {
        lifecycleScope.launch {
            runCatching {
                val bitmap = withContext(Dispatchers.IO) { createDayNightMap(date) }
                binding.map.setImageDrawable(BitmapDrawable(resources, bitmap))
                binding.date.text = date.formatDateAndTime()
            }.onFailure(logException).getOrNull().debugAssertNotNull // handle production OOM and so
        }
    }

    private val cachedMap by lazy {
        val zippedMapPath = resources.openRawResource(R.raw.worldmap).use { it.readBytes() }
        val mapPathString = String(GZIPInputStream(ByteArrayInputStream(zippedMapPath)).readBytes())
        val mapPath = PathParser.createPathFromPathData(mapPathString)
        val scale = 4
        val bitmap = Bitmap.createBitmap(360 * 16 / scale, 180 * 16 / scale, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).also {
            it.withScale(1f / scale, 1f / scale) {
                it.drawPath(mapPath, Paint().apply { color = 0xffbcbcbc.toInt() })
            }
        }
        bitmap
    }

    @WorkerThread
    private fun createDayNightMap(date: GregorianCalendar): Bitmap {
        val nightMask = Bitmap.createBitmap(360, 180, Bitmap.Config.ALPHA_8)
        val sunPosition = SunMoonPositionForMap(date)
        (-90 until 90).forEach { lat ->
            (-180 until 180).forEach { long ->
                // TODO: Calibrate it with the initial map
                if (sunPosition.isNight(lat.toDouble(), long.toDouble()))
                    nightMask[long + 180, 179 - (lat + 90)] = Color.BLACK
            }
        }
        val result = cachedMap.copy(Bitmap.Config.ARGB_8888, true)
        Canvas(result).also {
            val maskRect = Rect(0, 0, nightMask.width, nightMask.height)
            val originalRect = Rect(0, 0, result.width, result.height)
            it.drawBitmap(nightMask, maskRect, originalRect, Paint().apply { alpha = 0xB0 })
        }
        return result
    }
}

// Stub
class ZoomableImageView(context: Context, attrs: AttributeSet? = null) :
    AppCompatImageView(context, attrs) {

    private var scaleFactor = 1f
    private val scaleGestureDetector = ScaleGestureDetector(
        context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                scaleFactor = (scaleFactor + (detector?.scaleFactor ?: 1f)).coerceIn(.9f, 1.1f)
                postInvalidate()
                return true
            }
        }
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }
}
