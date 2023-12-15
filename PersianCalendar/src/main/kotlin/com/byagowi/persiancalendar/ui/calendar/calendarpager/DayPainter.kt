package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ZWJ
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.isHighTextContrastEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendarDigits
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.secondaryCalendarDigits
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.sp
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.isMoonInScorpio
import kotlin.math.min

class DayPainter(
    context: Context,
    private val width: Int,
    private val height: Int,
    private val isRtl: Boolean,
    @ColorInt widgetTextColor: Int? = null
) {
    private val paints = Paints(context, min(width, height).toFloat(), widgetTextColor)
    private var text = ""
    private var today = false
    private var dayIsSelected = false
    private var indicators = emptyList<Paint>()
    private var holiday = false
    var jdn: Jdn? = null
        private set
    private var isWeekNumber = false
    private var header = ""

    fun drawDay(canvas: Canvas) {
        drawCircle(canvas) // background circle, if is needed
        drawText(canvas) // can be a day number, week day name abbr or week number of year
        drawIndicators(canvas) // whether a day has event or appointment
        drawHeader(canvas) // shift work header
    }

    private fun drawCircle(canvas: Canvas) {
        if (today) canvas.drawCircle(
            width / 2f, height / 2f, radius(width, height) - paints.circlesPadding,
            paints.todayPaint
        )
    }

    private val textBounds = Rect()
    private fun drawText(canvas: Canvas) {
        val textPaint = when {
            jdn != null -> when {
                holiday -> paints.dayOfMonthNumberTextHolidayPaint
                dayIsSelected -> paints.dayOfMonthNumberTextSelectedPaint
                else /*!dayIsSelected*/ -> paints.dayOfMonthNumberTextPaint
            }

            isWeekNumber -> paints.weekNumberTextPaint
            else -> paints.weekDayInitialsTextPaint
        }
        // Measure a sample text to find height for vertical center aligning of the text to draw
        val sample = if (jdn != null) text else if (paints.isArabicScript) "س" else "Yy"
        textPaint.getTextBounds(sample, 0, sample.length, textBounds)
        val yPos = (height + textBounds.height()) / 2f
        // Draw day number/label
        canvas.drawText(text, width / 2f, yPos + paints.dayOffset, textPaint)
    }

    private fun drawIndicators(canvas: Canvas) {
        val offsetDirection = if (isRtl) -1 else 1
        indicators.forEachIndexed { i, paint ->
            val xOffset = paints.eventIndicatorsCentersDistance *
                    (i - (indicators.size - 1) / 2f) * offsetDirection
            canvas.drawCircle(
                width / 2f + xOffset, height / 2 + paints.eventYOffset,
                paints.eventIndicatorRadius, when {
                    dayIsSelected -> paints.headerTextSelectedPaint
                    // use textPaint for holiday event when a11y's high contrast is enabled
                    isHighTextContrastEnabled && holiday && paint == paints.eventIndicatorPaint ->
                        paints.dayOfMonthNumberTextHolidayPaint

                    else -> paint
                }
            )
        }
    }

    private fun drawHeader(canvas: Canvas) {
        if (header.isEmpty()) return
        canvas.drawText(
            header, width / 2f, height / 2 + paints.headerYOffset,
            if (dayIsSelected) paints.headerTextSelectedPaint else paints.headerTextPaint
        )
    }

    private fun setAll(
        text: String, isToday: Boolean = false, isSelected: Boolean = false,
        hasEvent: Boolean = false, hasAppointment: Boolean = false, isHoliday: Boolean = false,
        jdn: Jdn? = null, header: String? = null, isWeekNumber: Boolean = false
    ) {
        this.text = text
        this.today = isToday
        this.dayIsSelected = isSelected
        this.holiday = isHoliday
        this.jdn = jdn
        this.isWeekNumber = isWeekNumber
        val secondaryCalendar = secondaryCalendar
        this.header = listOfNotNull(
            if (isAstronomicalExtraFeaturesEnabled && jdn != null && isMoonInScorpio(jdn))
                paints.scorpioSign else null,
            if (secondaryCalendar == null || jdn == null) null else
                formatNumber(jdn.toCalendar(secondaryCalendar).dayOfMonth, secondaryCalendarDigits),
            header,
        ).joinToString(" ")
        this.indicators = listOf(
            hasAppointment to paints.appointmentIndicatorPaint,
            (hasEvent || (isHighTextContrastEnabled && holiday)) to paints.eventIndicatorPaint
        ).mapNotNull { (condition, paint) -> paint.takeIf { condition } }
    }

    fun setDayOfMonthItem(
        isToday: Boolean, isSelected: Boolean,
        hasEvent: Boolean, hasAppointment: Boolean, isHoliday: Boolean,
        jdn: Jdn, dayOfMonth: Int, header: String?
    ) = setAll(
        text = formatNumber(dayOfMonth, mainCalendarDigits), isToday = isToday,
        isSelected = isSelected, hasEvent = hasEvent, hasAppointment = hasAppointment, jdn = jdn,
        header = header, isHoliday = isHoliday || jdn.isWeekEnd()
    )

    fun setInitialOfWeekDay(text: String) = setAll(text)
    fun setWeekNumber(text: String) = setAll(text, isWeekNumber = true)

    companion object {
        fun radius(width: Int, height: Int): Float = min(width, height) / 2f
    }
}

private class Paints(
    context: Context, diameter: Float,
    @ColorInt private val widgetTextColor: Int? = null
) {
    private val dp = context.resources.dp
    val isArabicScript = language.isArabicScript
    val circlesPadding = 1 * dp
    val eventYOffset = diameter * 12 / 40
    val eventIndicatorRadius = diameter * 2 / 40
    private val eventIndicatorsGap = diameter * 2 / 40
    val eventIndicatorsCentersDistance = 2 * eventIndicatorRadius + eventIndicatorsGap
    val scorpioSign = context.getString(R.string.scorpio).first() + if (isArabicScript) ZWJ else ""

    private fun addShadowIfNeeded(paint: Paint) {
        if (widgetTextColor == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return
        paint.setShadowLayer(1f, 1f, 1f, Color.BLACK)
    }

    val appointmentIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = context.resolveColor(R.attr.colorAppointment)
    }
    val eventIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = widgetTextColor ?: context.resolveColor(R.attr.colorEventIndicator)
    }

    val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = 1 * dp
        it.color = widgetTextColor ?: context.resolveColor(R.attr.colorCurrentDay)
    }

    private val mainCalendarDigitsIsArabic = mainCalendarDigits === Language.ARABIC_DIGITS
    private val textSize = diameter * (if (mainCalendarDigitsIsArabic) 18 else 25) / 40
    val dayOffset = if (mainCalendarDigitsIsArabic) 0f else context.resources.sp(3f)

    private val secondaryCalendarDigitsIsArabic = secondaryCalendarDigits === Language.ARABIC_DIGITS
    private val headerTextSize = diameter / 40 * (if (secondaryCalendarDigitsIsArabic) 11 else 15)
    val headerYOffset = -diameter * (if (secondaryCalendarDigitsIsArabic) 10 else 7) / 40

    val dayOfMonthNumberTextHolidayPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = textSize
        it.color = context.resolveColor(R.attr.colorHoliday)
        addShadowIfNeeded(it)
    }

    private val colorTextDay = widgetTextColor ?: context.resolveColor(R.attr.colorOnAppBar)
    val dayOfMonthNumberTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = textSize
        it.color = colorTextDay
        addShadowIfNeeded(it)
    }

    private val colorTextDaySelected =
        widgetTextColor ?: context.resolveColor(R.attr.colorTextDaySelected)
    val dayOfMonthNumberTextSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = textSize
        it.color = colorTextDaySelected
        addShadowIfNeeded(it)
    }
    val headerTextSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = headerTextSize
        it.color = colorTextDaySelected
        addShadowIfNeeded(it)
    }

    private val colorTextDayName = ColorUtils.setAlphaComponent(
        widgetTextColor ?: context.resolveColor(R.attr.colorOnAppBar),
        0xCC
    )
    val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = headerTextSize
        it.color = colorTextDayName
        addShadowIfNeeded(it)
    }
    val weekNumberTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = headerTextSize
        it.color = colorTextDayName
        addShadowIfNeeded(it)
    }
    val weekDayInitialsTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = diameter * 20 / 40
        it.color = colorTextDayName
        addShadowIfNeeded(it)
    }
}
