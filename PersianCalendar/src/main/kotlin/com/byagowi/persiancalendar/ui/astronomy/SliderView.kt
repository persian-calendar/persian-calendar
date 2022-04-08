package com.byagowi.persiancalendar.ui.astronomy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.ui.common.EmptySlider
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.resolveColor
import kotlin.math.PI
import kotlin.math.cos

class SliderView(context: Context, attrs: AttributeSet? = null) : EmptySlider(context, attrs) {
    private var positionOffset = 0

    init {
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                positionOffset -= dx
            }
        })
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.strokeWidth = 2.dp
        it.color = context.resolveColor(com.google.android.material.R.attr.colorAccent)
    }
    private val space = 10.dp

    override fun onDraw(canvas: Canvas) {
        val linesCount = width / space.toInt()
        (0..linesCount).forEachIndexed { index, it ->
            val x = it * space + positionOffset % space
            val deviation = 2 * (index - linesCount / 2f) / linesCount
            paint.alpha = (cos(deviation * PI / 2) * 255).toInt()
            canvas.drawLine(x, 0f, x, height.toFloat(), paint)
        }
    }
}
