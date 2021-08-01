package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.Jdn
import com.byagowi.persiancalendar.utils.dp
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.isHighTextContrastEnabled
import com.byagowi.persiancalendar.utils.isNonArabicScriptSelected
import com.byagowi.persiancalendar.utils.otherCalendars
import com.byagowi.persiancalendar.utils.resolveColor
import com.byagowi.persiancalendar.utils.sp
import kotlin.math.min

class DayView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val colorHoliday = context.resolveColor(R.attr.colorHoliday)
    private val colorHolidaySelected = context.resolveColor(R.attr.colorHolidaySelected)

    // private val colorTextHoliday = context.resolveColor(R.attr.colorTextHoliday)
    private val colorTextDay = context.resolveColor(R.attr.colorTextDay)
    private val colorTextDaySelected = context.resolveColor(R.attr.colorTextDaySelected)

    // private val colorTextToday = context.resolveColor(R.attr.colorTextToday)
    private val colorTextDayName = context.resolveColor(R.attr.colorTextDayName)

    private val appointmentIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.resolveColor(com.google.android.material.R.attr.colorSecondary)
    }
    private val eventIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.resolveColor(R.attr.colorEventIndicator)
    }
    private val eventYOffset = 7.sp
    private val eventIndicatorRadius = 2.sp
    private val eventIndicatorsGap = 2.sp
    private val eventIndicatorsCentersDistance = 2 * eventIndicatorRadius + eventIndicatorsGap
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.resolveColor(R.attr.colorSelectDay)
    }
    private val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.dp
        color = context.resolveColor(R.attr.colorCurrentDay)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    fun setTextTypeface(typeface: Typeface) {
        textPaint.typeface = typeface
        headerTextPaint.typeface = typeface
    }

    private var text = ""
    private var today = false
    private var dayIsSelected = false
    private var indicators = emptyList<Paint>()
    private var holiday = false
    var jdn: Jdn? = null
        private set
    var dayOfMonth = -1
        private set
    private var isNumber = false
    private var header = ""

    private val textBounds = Rect()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw circle around day
        val radius = min(width, height) / 2f
        if (dayIsSelected) canvas.drawCircle(width / 2f, height / 2f, radius - 5, selectedPaint)
        if (today) canvas.drawCircle(width / 2f, height / 2f, radius - 5, todayPaint)

        // Draw day number/label
        textPaint.color = when {
            isNumber && holiday && dayIsSelected -> colorHolidaySelected
            isNumber && holiday && !dayIsSelected -> colorHoliday
            isNumber && !holiday && dayIsSelected -> colorTextDaySelected
            isNumber && !holiday && !dayIsSelected -> colorTextDay
            else -> colorTextDayName
        }
        val textToMeasureHeight =
            if (isNumber) text else if (isNonArabicScriptSelected()) "Y" else "شچ"
        textPaint.getTextBounds(textToMeasureHeight, 0, textToMeasureHeight.length, textBounds)
        val yPos = (height + textBounds.height()) / 2f
        canvas.drawText(text, width / 2f, yPos, textPaint)

        // Draw indicators, whether a day has event or appointment
        val offsetDirection = if (layoutDirection == LAYOUT_DIRECTION_RTL) -1 else 1
        indicators.forEachIndexed { i, paint ->
            val xOffset = eventIndicatorsCentersDistance *
                    (i - (indicators.size - 1) / 2f) * offsetDirection
            val overrideByTextColor = dayIsSelected ||
                    // use textPaint for holiday event when a11y's high contrast is enabled
                    (isHighTextContrastEnabled && holiday && paint == eventIndicatorPaint)
            canvas.drawCircle(
                width / 2f + xOffset, height - eventYOffset, eventIndicatorRadius,
                if (overrideByTextColor) textPaint else paint
            )
        }

        // Draw day header which is used for shit work
        if (header.isNotEmpty()) {
            headerTextPaint.color = if (dayIsSelected) colorTextDaySelected else colorTextDay
            canvas.drawText(header, width / 2f, yPos * 0.87f - textBounds.height(), headerTextPaint)
        }

        // Experiment around what happens if we show other calendars day of month
        if ((false)) jdn?.also {
            otherCalendars.forEachIndexed { i, calendar ->
                val offset = (if (layoutDirection == LAYOUT_DIRECTION_RTL) -1 else 1) *
                        if (i == 1) -1 else 1
                canvas.drawText(
                    // better to not calculate this during onDraw
                    formatNumber(it.toCalendar(calendar).dayOfMonth),
                    (width - radius * offset) / 2f,
                    (height + textBounds.height() + radius) / 2f,
                    headerTextPaint
                )
            }
        }
    }

    private fun setAll(
        text: String, isToday: Boolean, isSelected: Boolean,
        hasEvent: Boolean, hasAppointment: Boolean, isHoliday: Boolean,
        textSize: Int, jdn: Jdn?, dayOfMonth: Int, isNumber: Boolean,
        header: String
    ) {
        this.text = text
        this.today = isToday
        this.dayIsSelected = isSelected
        this.holiday = isHoliday
        this.jdn = jdn
        this.dayOfMonth = dayOfMonth
        this.isNumber = isNumber
        this.header = header
        this.indicators = listOf(
            hasAppointment to appointmentIndicatorPaint,
            hasEvent to eventIndicatorPaint
        ).mapNotNull { (condition, paint) -> paint.takeIf { condition } }
        textPaint.textSize = textSize.toFloat()
        headerTextPaint.textSize = textSize / 2f
        postInvalidate()
    }

    fun setDayOfMonthItem(
        isToday: Boolean, isSelected: Boolean,
        hasEvent: Boolean, hasAppointment: Boolean, isHoliday: Boolean,
        textSize: Int, jdn: Jdn, dayOfMonth: Int, header: String
    ) = setAll(
        formatNumber(dayOfMonth), isToday, isSelected, hasEvent, hasAppointment,
        isHoliday, textSize, jdn, dayOfMonth, true, header
    )

    fun setNonDayOfMonthItem(text: String, textSize: Int) = setAll(
        text, isToday = false, isSelected = false, hasEvent = false, hasAppointment = false,
        isHoliday = false, textSize = textSize, jdn = null, dayOfMonth = -1,
        isNumber = false, header = ""
    )
}
