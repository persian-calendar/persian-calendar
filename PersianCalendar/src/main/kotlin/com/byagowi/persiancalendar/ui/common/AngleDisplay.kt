package com.byagowi.persiancalendar.ui.common

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.core.graphics.withTranslation
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.dp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class AngleDisplay(
    resources: Resources, defaultFormat: String = "00.0",
    private val backgroundText: String = "88.8"
) {
    private val lcd = Typeface.createFromAsset(resources.assets, "fonts/lcd.ttf")
    private val lcdTextSize = 20 * resources.dp
    private val lcdForegroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0xFF00FF00.toInt()
        it.textSize = lcdTextSize
        it.typeface = lcd
        it.textAlign = Paint.Align.CENTER
    }
    private val lcdBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = 0x44FFFFFF
        it.textSize = lcdTextSize
        it.typeface = lcd
        it.textAlign = Paint.Align.CENTER
    }
    private val displayRect = Rect().also {
        lcdBackgroundPaint.getTextBounds(backgroundText, 0, backgroundText.length, it)
    }
    private val lcdWidth = displayRect.width()
    val lcdHeight = displayRect.height()
    private val displayDrawable = resources.getDrawable(R.drawable.display, null)
    private val displayFormat = DecimalFormat(defaultFormat).also {
        it.decimalFormatSymbols = DecimalFormatSymbols(Locale.ENGLISH)
    }
    private val displayPadding = (8 * resources.dp).toInt()
    val displayGap = (24 * resources.dp).toInt()

    fun updatePlacement(x: Int, y: Int) {
        displayRect.set(
            x - lcdWidth / 2 - displayPadding,
            y - displayGap - 2 * displayPadding - lcdHeight,
            x + lcdWidth / 2 + displayPadding,
            y - displayGap
        )
    }

    fun draw(canvas: Canvas, angle: Float, offsetXFactor: Int = 0) {
        canvas.withTranslation(x = offsetXFactor * (displayRect.width() + displayGap) / 2f) {
            displayDrawable.bounds = displayRect
            displayDrawable.draw(this)
            drawText(
                backgroundText,
                displayRect.exactCenterX(), displayRect.centerY() + lcdHeight / 2f,
                lcdBackgroundPaint
            )
            drawText(
                displayFormat.format(angle).padStart(backgroundText.length),
                displayRect.exactCenterX(), displayRect.centerY() + lcdHeight / 2f,
                lcdForegroundPaint
            )
        }
    }
}
