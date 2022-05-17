package com.byagowi.persiancalendar.ui.level

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.core.graphics.get
import androidx.core.graphics.withTranslation
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

fun showColorPickerDialog(activity: FragmentActivity) {
    val view = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val colorCircle = CircleColorPickerView(activity).also { it.layoutParams = layoutParams }
        addView(colorCircle)
        addView(Slider(activity).also {
            it.layoutParams = layoutParams
            it.addOnChangeListener { _, value, _ -> colorCircle.setBrightness(value) }
            it.valueFrom = 0f
            it.valueTo = 100f
        })
    }
    MaterialAlertDialogBuilder(activity)
        .setView(view)
        .show()
}

class CircleColorPickerView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private var lastX = -1f
    private var lastY = -1f
    private val fillPaint = Paint().also {
        it.style = Paint.Style.FILL
    }
    private val strokePaint = Paint().also {
        it.style = Paint.Style.STROKE
        it.color = Color.WHITE
    }
    private val shadowPaint = Paint().also {
        it.style = Paint.Style.FILL
    }
    private var brightness = 0f

    fun setBrightness(value: Float) {
        brightness = value
        generateReferenceCircle()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        generateReferenceCircle()
        lastX = lastX.takeIf { it != -1f } ?: (bitmap.width / 2f)
        lastY = lastY.takeIf { it != -1f } ?: (bitmap.height / 2f)

        strokePaint.strokeWidth = bitmap.width / 100f
        shadowPaint.shader = RadialGradient(
            0f, 0f, bitmap.height / 15f,
            Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
    }

    private fun generateReferenceCircle() {
        val min = min(width, height)
        val radius = min / 2f
        if (bitmap.width != min || bitmap.height != min)
            bitmap = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888)
        val paint = Paint()
        val radialGradient = RadialGradient(
            radius, radius, radius * PADDING_FACTOR, Color.WHITE,
            0x00FFFFFF, Shader.TileMode.CLAMP
        )
        val saturation = (100 - brightness) / 100f
        val colors = (0 until 360 step 30)
            .map { Color.HSVToColor(floatArrayOf(it.toFloat(), saturation, 1f)) }
            .let { it + listOf(it[0]) } // Adds the first element at the end
            .toIntArray()
        val sweepGradient = SweepGradient(radius, radius, colors, null)
        paint.shader = ComposeShader(sweepGradient, radialGradient, PorterDuff.Mode.SRC_OVER)
        Canvas(bitmap).drawCircle(radius, radius, radius * PADDING_FACTOR, paint)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        fillPaint.color = bitmap[lastX.toInt(), lastY.toInt()]
        canvas.withTranslation(lastX, lastY) {
            canvas.drawCircle(0f, 0f, bitmap.width / 8f, shadowPaint)
            canvas.drawCircle(0f, 0f, bitmap.width / 20f, fillPaint)
            canvas.drawCircle(0f, 0f, bitmap.width / 20f, strokePaint)
        }
    }

    var onColorPicked = fun (@ColorInt _: Int) {}

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val r = bitmap.width / 2
        val radius = hypot(event.x - r, event.y - r).coerceAtMost(r * PADDING_FACTOR - 2f)
        val angle = atan2(event.y - r, event.x - r)
        lastX = radius * cos(angle) + r
        lastY = radius * sin(angle) + r
        onColorPicked(bitmap[lastX.toInt(), lastY.toInt()])
        invalidate()
        return true
    }

    companion object {
        private const val PADDING_FACTOR = 87f / 100
    }
}
