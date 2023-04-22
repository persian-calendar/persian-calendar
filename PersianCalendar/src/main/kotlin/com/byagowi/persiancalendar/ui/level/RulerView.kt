package com.byagowi.persiancalendar.ui.level

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.byagowi.persiancalendar.ui.utils.dp

class RulerView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val textSize = 12.dp
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.GRAY
        it.strokeWidth = 1.dp
        it.textSize = textSize
    }
    private val textSideOffset = 30.dp
    private val firstLevel = 25.dp
    private val secondLevel = 15.dp
    private val thirdLevel = 8.dp
    private val topTextOffset = 9.dp
    private val textOffset = 10.dp - textSize / 2
    var cmInchFlip = false
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        val dpi = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            resources.displayMetrics.ydpi else resources.displayMetrics.xdpi

        // Inch
        paint.textAlign = if (cmInchFlip) Paint.Align.RIGHT else Paint.Align.LEFT
        val steps = dpi / 4f
        (0..(height / steps).toInt()).forEach { i ->
            val y = steps * i
            val w = when {
                i % 4 == 0 -> {
                    val label = if (i == 0) "0 in" else "${i / 4}"
                    canvas.drawText(
                        label, if (cmInchFlip) width - textSideOffset else textSideOffset,
                        if (i == 0) topTextOffset else y + textOffset, paint
                    )
                    firstLevel
                }

                i % 2 == 0 -> secondLevel
                else -> thirdLevel
            }
            canvas.drawLine(
                if (cmInchFlip) width * 1f else 0f, y,
                if (cmInchFlip) width - w else w, y, paint
            )
        }

        // Centimeter
        paint.textAlign = if (cmInchFlip) Paint.Align.LEFT else Paint.Align.RIGHT
        val cmSteps = dpi / 2.54 / 10
        (0..(height / cmSteps).toInt()).forEach { i ->
            val y = cmSteps.toFloat() * i
            val w = when {
                i % 10 == 0 -> {
                    val label = if (i == 0) "0 cm" else "${i / 10}"
                    canvas.drawText(
                        label, if (cmInchFlip) textSideOffset else width - textSideOffset,
                        if (i == 0) topTextOffset else y + textOffset, paint
                    )
                    firstLevel
                }

                i % 5 == 0 -> secondLevel
                else -> thirdLevel
            }
            canvas.drawLine(
                if (cmInchFlip) 0f else width * 1f, y,
                if (cmInchFlip) w else width - w, y, paint
            )
        }
    }
}
