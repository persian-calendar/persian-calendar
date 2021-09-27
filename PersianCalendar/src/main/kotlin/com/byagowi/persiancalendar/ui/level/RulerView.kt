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
    private val textSideOffset = 24.dp
    private val firstLevel = 20.dp
    private val secondLevel = 10.dp
    private val thirdLevel = 5.dp
    private val topOffset = 10.dp.toInt()
    private val topTextOffset = topOffset - textSize / 2
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas ?: return)

        // Inch
        paint.textAlign = Paint.Align.LEFT
        val steps = dpi / 4f
        (0..(height / steps).toInt()).forEach { i ->
            val y = topOffset + steps * i
            val w = when {
                i % 4 == 0 -> {
                    val label = if (i == 0) "0 in" else "${i / 4}"
                    canvas.drawText(label, textSideOffset, y + topTextOffset, paint)
                    firstLevel
                }
                i % 2 == 0 -> secondLevel
                else -> thirdLevel
            }
            canvas.drawLine(0f, y, w, y, paint)
        }

        // Centimeter
        paint.textAlign = Paint.Align.RIGHT
        val cmSteps = dpi / 2.54 / 10
        (0..(height / cmSteps).toInt()).forEach { i ->
            val y = topOffset + cmSteps.toFloat() * i
            val w = when {
                i % 10 == 0 -> {
                    val label = if (i == 0) "0 cm" else "${i / 10}"
                    canvas.drawText(label, width - textSideOffset, y + topTextOffset, paint)
                    firstLevel
                }
                i % 5 == 0 -> secondLevel
                else -> thirdLevel
            }
            canvas.drawLine(width.toFloat(), y, width - w, y, paint)
        }
    }
}
