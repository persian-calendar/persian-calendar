package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.Jdn
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.isHighTextContrastEnabled
import com.byagowi.persiancalendar.utils.isLocaleRTL
import com.byagowi.persiancalendar.utils.isNonArabicScriptSelected
import com.byagowi.persiancalendar.utils.resolveColor
import kotlin.math.min

class DayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {

    private val colorHoliday = context.resolveColor(R.attr.colorHoliday)
    private val colorHolidaySelected = context.resolveColor(R.attr.colorHolidaySelected)

    // private val colorTextHoliday = context.resolveColor(R.attr.colorTextHoliday)
    private val colorTextDay = context.resolveColor(R.attr.colorTextDay)
    private val colorTextDaySelected = context.resolveColor(R.attr.colorTextDaySelected)

    // private val colorTextToday = context.resolveColor(R.attr.colorTextToday)
    private val colorTextDayName = context.resolveColor(R.attr.colorTextDayName)

    private val appointmentIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.resolveColor(R.attr.colorSecondary)
    }
    private val eventIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.resolveColor(R.attr.colorEventIndicator)
    }
    private val eventYOffset =
        resources.getDimensionPixelSize(R.dimen.day_item_event_y_offset)
    private val eventIndicatorRadius =
        resources.getDimensionPixelSize(R.dimen.day_item_event_indicator_radius).toFloat()
    private val eventIndicatorsGap =
        resources.getDimensionPixelSize(R.dimen.day_item_event_indicators_gap)
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.resolveColor(R.attr.colorSelectDay)
    }
    private val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth =
            resources.getDimensionPixelSize(R.dimen.day_item_today_indicator_thickness).toFloat()
        color = context.resolveColor(R.attr.colorCurrentDay)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setTextTypeface(typeface: Typeface) {
        textPaint.typeface = typeface
    }

    private val bounds = Rect()
    private val drawingRect = RectF()
    private var text = ""
    private var today = false
    private var dayIsSelected = false
    private var indicators = emptyList<Paint>()
    private var holiday = false
    private var textSize = 0
    var jdn: Jdn? = null
        private set
    var dayOfMonth = -1
        private set
    private var isNumber = false
    private var header = ""

    private val isRTL = isLocaleRTL()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        val height = height
        val radius = min(width, height) / 2f

        getDrawingRect(bounds)
        drawingRect.set(bounds)
        drawingRect.inset(radius * 0.1f, radius * 0.1f)

        if (dayIsSelected) {
            canvas.drawCircle(width / 2f, height / 2f, radius - 5, selectedPaint)
        }

        if (today) {
            canvas.drawCircle(width / 2f, height / 2f, radius - 5, todayPaint)
        }

        textPaint.color = when {
            isNumber && holiday && dayIsSelected -> colorHolidaySelected
            isNumber && holiday && !dayIsSelected -> colorHoliday
            isNumber && !holiday && dayIsSelected -> colorTextDaySelected
            isNumber && !holiday && !dayIsSelected -> colorTextDay
            else -> colorTextDayName
        }
        textPaint.textSize = textSize.toFloat()

        indicators.forEachIndexed { i, paint ->
            val xOffset = eventIndicatorsGap * (i - (indicators.size - 1) / 2f)
            val overrideByTextColor = dayIsSelected ||
                    // use textPaint for holiday event when a11y's high contrast is enabled
                    (isHighTextContrastEnabled && holiday && paint == eventIndicatorPaint)
            canvas.drawCircle(
                width / 2f + xOffset * if (isRTL) -1 else 1, (height - eventYOffset).toFloat(),
                eventIndicatorRadius,
                if (overrideByTextColor) textPaint else paint
            )
        }

        val xPos = (width - textPaint.measureText(text).toInt()) / 2
        val textToMeasureHeight =
            if (isNumber) text else if (isNonArabicScriptSelected()) "Y" else "شچ"
        textPaint.getTextBounds(textToMeasureHeight, 0, textToMeasureHeight.length, bounds)
        val yPos = (height + bounds.height()) / 2
        canvas.drawText(text, xPos.toFloat(), yPos.toFloat(), textPaint)

        textPaint.color = if (dayIsSelected) colorTextDaySelected else colorTextDay
        textPaint.textSize = textSize / 2f
        if (header.isNotEmpty()) {
            val headerXPos = (width - textPaint.measureText(header).toInt()) / 2F
            canvas.drawText(header, headerXPos, yPos * 0.87f - bounds.height(), textPaint)
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
        this.textSize = textSize
        this.jdn = jdn
        this.dayOfMonth = dayOfMonth
        this.isNumber = isNumber
        this.header = header
        this.indicators = listOf(
            hasAppointment to appointmentIndicatorPaint,
            hasEvent to eventIndicatorPaint
        ).mapNotNull { (condition, paint) -> paint.takeIf { condition } }
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
