package com.byagowi.persiancalendar.ui.calendar.month

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.isNonArabicScriptSelected
import kotlin.math.min

class ItemDayView : View {
    private lateinit var resource: DaysPaintResources
    private val bounds = Rect()
    private val drawingRect = RectF()
    private var text = ""
    private var today: Boolean = false
    private var IsSelected: Boolean = false
    private var hasEvent: Boolean = false
    private var hasAppointment: Boolean = false
    private var holiday: Boolean = false
    private var textSize: Int = 0
    var jdn: Long = -1
        private set
    var dayOfMonth = -1
        private set
    private var isNumber: Boolean = false
    private var header = ""

    constructor(context: Context, resource: DaysPaintResources) : super(context) {
        this.resource = resource
    }

    // This constructor shouldn't be used
    // as the first one reuses resource retrieval across the days
    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        if (context is Activity) {
            resource = DaysPaintResources(context)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        val height = height
        val radius = min(width, height) / 2

        val isModernTheme = resource.style == R.style.ModernTheme
        getDrawingRect(bounds)
        drawingRect.set(bounds)
        drawingRect.inset(radius * 0.1f, radius * 0.1f)
        val yOffsetToApply = if (isModernTheme) (-height * .07f).toInt() else 0

        if (IsSelected) {
            if (isModernTheme) {
                canvas.drawRoundRect(drawingRect, 0f, 0f, resource.selectedPaint)
            } else {
                canvas.drawCircle(
                    width / 2f, height / 2f, (radius - 5).toFloat(),
                    resource.selectedPaint
                )
            }
        }

        if (today) {
            if (isModernTheme) {
                canvas.drawRoundRect(drawingRect, 0f, 0f, resource.todayPaint)
            } else {
                canvas.drawCircle(
                    width / 2f, height / 2f, (radius - 5).toFloat(),
                    resource.todayPaint
                )
            }
        }

        val color: Int = if (isNumber) {
            if (holiday)
                if (IsSelected) resource.colorHolidaySelected else resource.colorHoliday
            else
                if (IsSelected) resource.colorTextDaySelected else resource.colorTextDay
            //            if (today && !selected) {
            //                color = resource.colorTextToday;
            //            }
        } else {
            resource.colorTextDayName
        }

        resource.eventBarPaint.color =
            if (IsSelected && !isModernTheme) color else resource.colorEventLine

        if (hasEvent) {
            canvas.drawLine(
                width / 2f - resource.halfEventBarWidth,
                (height - resource.eventYOffset + yOffsetToApply).toFloat(),
                width / 2f + resource.halfEventBarWidth,
                (height - resource.eventYOffset + yOffsetToApply).toFloat(), resource.eventBarPaint
            )
        }

        if (hasAppointment) {
            canvas.drawLine(
                width / 2f - resource.halfEventBarWidth,
                (height - resource.appointmentYOffset + yOffsetToApply).toFloat(),
                width / 2f + resource.halfEventBarWidth,
                (height - resource.appointmentYOffset + yOffsetToApply).toFloat(),
                resource.eventBarPaint
            )
        }

        // TODO: Better to not change resource's paint objects, but for now
        resource.textPaint.color = color
        resource.textPaint.textSize = textSize.toFloat()

        if (isModernTheme) {
            resource.textPaint.isFakeBoldText = today
            resource.textPaint.textSize = textSize * .8f
        }

        val xPos = (width - resource.textPaint.measureText(text).toInt()) / 2
        val textToMeasureHeight =
            if (isNumber) text else if (isNonArabicScriptSelected()) "Y" else "شچ"
        resource.textPaint.getTextBounds(textToMeasureHeight, 0, textToMeasureHeight.length, bounds)
        var yPos = (height + bounds.height()) / 2
        yPos += yOffsetToApply
        canvas.drawText(text, xPos.toFloat(), yPos.toFloat(), resource.textPaint)

        resource.textPaint.color =
            if (IsSelected) resource.colorTextDaySelected else resource.colorTextDay
        resource.textPaint.textSize = textSize / 2f
        if (header.isNotEmpty()) {
            val headerXPos = (width - resource.textPaint.measureText(header).toInt()) / 2
            canvas.drawText(
                header,
                headerXPos.toFloat(),
                yPos * 0.87f - bounds.height(),
                resource.textPaint
            )
        }
    }

    private fun setAll(
        text: String, isToday: Boolean, isSelected: Boolean,
        hasEvent: Boolean, hasAppointment: Boolean, isHoliday: Boolean,
        textSize: Int, jdn: Long, dayOfMonth: Int, isNumber: Boolean,
        header: String
    ) {
        this.text = text
        this.today = isToday
        this.IsSelected = isSelected
        this.hasEvent = hasEvent
        this.hasAppointment = hasAppointment
        this.holiday = isHoliday
        this.textSize = textSize
        this.jdn = jdn
        this.dayOfMonth = dayOfMonth
        this.isNumber = isNumber
        this.header = header
        postInvalidate()
    }

    fun setDayOfMonthItem(
        isToday: Boolean, isSelected: Boolean,
        hasEvent: Boolean, hasAppointment: Boolean, isHoliday: Boolean,
        textSize: Int, jdn: Long, dayOfMonth: Int, header: String
    ) {
        val dayOfMonthString = formatNumber(dayOfMonth)
        setAll(
            dayOfMonthString, isToday, isSelected, hasEvent, hasAppointment,
            isHoliday, textSize, jdn, dayOfMonth, true, header
        )
    }

    fun setNonDayOfMonthItem(text: String, textSize: Int) {
        setAll(
            text, false, false, false, false, false,
            textSize, -1, -1, false, ""
        )
    }
}
