package com.byagowi.persiancalendar.ui.astronomy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.annotation.ColorInt
import com.byagowi.persiancalendar.ui.common.BaseSlider
import com.byagowi.persiancalendar.ui.utils.dp
import kotlin.math.PI
import kotlin.math.cos

class SliderView(context: Context, attrs: AttributeSet? = null) : BaseSlider(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.strokeWidth = 2 * resources.dp
    }
    private val space = 10 * resources.dp

    fun setBarsColor(@ColorInt color: Int) {
        paint.color = color
    }

    override fun onDraw(canvas: Canvas) {
        val linesCount = width / space.toInt()
        repeat(linesCount) {
            val x = it * space + positionX.value % space
            val deviation = 2 * (it - linesCount / 2f) / linesCount
            paint.alpha = (cos(deviation * PI / 2) * 255).toInt()
            canvas.drawLine(x, 0f, x, height.toFloat(), paint)
        }
    }
}
