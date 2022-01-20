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
        val imageView = ZoomableImageView(inflater.context)

        val args by navArgs<MapFragmentArgs>()
        val date = GregorianCalendar().also { it.add(Calendar.MINUTE, args.minutesOffset) }
        lifecycleScope.launch {
            runCatching {
                val bitmap = withContext(Dispatchers.IO) { createMap() }
                val image = BitmapDrawable(resources, bitmap)
                imageView.setImageDrawable(image)
                withContext(Dispatchers.IO) { addDayNightMask(bitmap, date) }
                image.invalidateSelf()
            }.onFailure(logException).getOrNull().debugAssertNotNull // handle production OOM and so
        }

        return imageView
    }

    @WorkerThread
    private fun createMap(): Bitmap {
        val zippedMapPath = resources.openRawResource(R.raw.worldmap).use { it.readBytes() }
        val mapPathString = String(GZIPInputStream(ByteArrayInputStream(zippedMapPath)).readBytes())
        val mapPath = PathParser.createPathFromPathData(mapPathString)
        val scale = 4
        val bitmap = Bitmap.createBitmap(4378 / scale, 2435 / scale, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).also {
            it.withScale(1f / scale, 1f / scale) {
                it.drawPath(mapPath, Paint().apply { color = 0xffbcbcbc.toInt() })
            }
        }
        return bitmap
    }

    @WorkerThread
    private fun addDayNightMask(bitmap: Bitmap, date: GregorianCalendar) {
        val nightMask = Bitmap.createBitmap(360, 180, Bitmap.Config.ALPHA_8)
        val sunPosition = SunMoonPositionForMap(date)
        (-90 until 90).forEach { lat ->
            (-180 until 180).forEach { long ->
                // TODO: Calibrate it with the initial map
                if (sunPosition.isNight(lat.toDouble(), long.toDouble()))
                    nightMask[long + 180, 179 - (lat + 90)] = Color.BLACK
            }
        }
        Canvas(bitmap).also {
            val maskRect = Rect(0, 0, nightMask.width, nightMask.height)
            val originalRect = Rect(0, 0, bitmap.width, bitmap.height)
            it.drawBitmap(nightMask, maskRect, originalRect, Paint().apply { alpha = 0xB0 })
        }
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
