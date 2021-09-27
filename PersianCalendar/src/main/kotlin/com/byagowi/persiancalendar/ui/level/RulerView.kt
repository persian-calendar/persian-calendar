package com.byagowi.persiancalendar.ui.level;

import android.content.Context;
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet;
import android.view.View
import com.byagowi.persiancalendar.ui.utils.dp

class RulerView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val dpi = Resources.getSystem().displayMetrics.densityDpi
    private val textSize = 12.dp
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.GRAY
        it.strokeWidth = 1.dp
        it.textSize = textSize
    }
    private val topOffset = 10.dp.toInt()
    private val topTextOffset = topOffset - textSize / 2
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas ?: return)

        // Inch
        paint.textAlign = Paint.Align.LEFT
        val steps = dpi / 4f
        (topOffset..height step steps.toInt()).forEachIndexed { i, _ ->
            val y = topOffset + steps * i
            val w = when {
                i % 4 == 0 -> {
                    val label = "${i / 4}${if (i == 0) " in" else ""}"
                    canvas.drawText(label, 54f, y + topTextOffset, paint)
                    48f
                }
                i % 2 == 0 -> 24f
                else -> 12f
            }
            canvas.drawLine(0f, y, w, y, paint)
        }

        // Centimeter
        paint.textAlign = Paint.Align.RIGHT
        val cmSteps = dpi / 2.54 / 10
        val width = width.toFloat()
        (topOffset..height step cmSteps.toInt()).forEachIndexed { i, _ ->
            val y = topOffset + cmSteps.toFloat() * i
            val w = when {
                i % 10 == 0 -> {
                    val label = "${if (i == 0) "cm " else ""}${i / 10}"
                    canvas.drawText(label, width - 54f, y + topTextOffset, paint)
                    48f
                }
                i % 5 == 0 -> 24f
                else -> 12f
            }
            canvas.drawLine(width, y, width - w, y, paint)
        }
    }
}
