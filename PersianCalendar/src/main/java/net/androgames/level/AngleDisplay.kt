package net.androgames.level

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.getCompatDrawable
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class AngleDisplay(context: Context) {

    private val displayBackgroundText = "88.8"
    private val lcd = Typeface.createFromAsset(context.assets, "fonts/lcd.ttf")
    private val lcdForegroundPaint = Paint().also {
        it.color = context.resources.getColor(R.color.lcd_front)
        it.isAntiAlias = true
        it.textSize = context.resources.getDimensionPixelSize(R.dimen.lcd_text).toFloat()
        it.typeface = lcd
        it.textAlign = Paint.Align.CENTER
    }
    private val lcdBackgroundPaint = Paint().also {
        it.color = context.resources.getColor(R.color.lcd_back)
        it.isAntiAlias = true
        it.textSize = context.resources.getDimensionPixelSize(R.dimen.lcd_text).toFloat()
        it.typeface = lcd
        it.textAlign = Paint.Align.CENTER
    }
    private val displayRect = Rect().also {
        lcdBackgroundPaint.getTextBounds(displayBackgroundText, 0, displayBackgroundText.length, it)
    }
    private val lcdWidth = displayRect.width()
    val lcdHeight = displayRect.height()
    private val display = context.getCompatDrawable(R.drawable.display)
    private val displayFormat = DecimalFormat("00.0").also {
        it.decimalFormatSymbols = DecimalFormatSymbols(Locale.ENGLISH)
    }
    private val displayPadding = context.resources.getDimensionPixelSize(R.dimen.display_padding)
    private val displayGap = context.resources.getDimensionPixelSize(R.dimen.display_gap)

    fun updatePlacement(x: Int, y: Int) {
        displayRect.set(
            x - lcdWidth / 2 - displayPadding,
            y - displayGap - 2 * displayPadding - lcdHeight,
            x + lcdWidth / 2 + displayPadding,
            y - displayGap
        )
    }

    fun draw(canvas: Canvas, angle: Float, offsetXFactor: Int = 0) {
        val offsetX = offsetXFactor * (displayRect.width() + displayGap) / 2
        displayRect.left += offsetX
        displayRect.right += offsetX
        display.bounds = displayRect
        display.draw(canvas)
        canvas.drawText(
            displayBackgroundText, displayRect.exactCenterX(),
            displayRect.centerY() + lcdHeight / 2f, lcdBackgroundPaint
        )
        canvas.drawText(
            displayFormat.format(angle), displayRect.exactCenterX(),
            displayRect.centerY() + lcdHeight / 2f, lcdForegroundPaint
        )
        displayRect.left -= offsetX
        displayRect.right -= offsetX
    }
}
