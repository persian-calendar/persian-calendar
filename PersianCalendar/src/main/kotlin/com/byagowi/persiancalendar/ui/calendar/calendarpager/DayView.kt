package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.byagowi.persiancalendar.ReleaseDebugDifference.debugAssertNotNull
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.isHighTextContrastEnabled
import com.byagowi.persiancalendar.utils.isNonArabicScriptSelected
import com.byagowi.persiancalendar.utils.otherCalendars
import kotlin.math.min

class DayView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var text = ""
    private var today = false
    private var dayIsSelected = false
    private var indicators = emptyList<Paint>()
    private var holiday = false
    var jdn: Jdn? = null
        private set
    var dayOfMonth = -1
        private set
    private var isDayOfMonth = false
    private var header = ""
    private var textSize = 0

    var sharedDayViewData: SharedDayViewData? = null

    private val textBounds = Rect()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val shared = sharedDayViewData ?: return
        shared.textPaint.textSize = textSize.toFloat()
        shared.headerTextPaint.textSize = textSize / 2f

        // Draw circle around day
        val radius = min(width, height) / 2f
        if (dayIsSelected)
            canvas.drawCircle(width / 2f, height / 2f, radius - 5, shared.selectedPaint)
        if (today) canvas.drawCircle(width / 2f, height / 2f, radius - 5, shared.todayPaint)

        // Draw day number/label
        shared.textPaint.color = when {
            isDayOfMonth && holiday && dayIsSelected -> shared.colorHolidaySelected
            isDayOfMonth && holiday && !dayIsSelected -> shared.colorHoliday
            isDayOfMonth && !holiday && dayIsSelected -> shared.colorTextDaySelected
            isDayOfMonth && !holiday && !dayIsSelected -> shared.colorTextDay
            else -> shared.colorTextDayName
        }
        val textToMeasureHeight =
            if (isDayOfMonth) text else if (isNonArabicScriptSelected) "Y" else "شچ"
        shared.textPaint.getTextBounds(
            textToMeasureHeight, 0, textToMeasureHeight.length, textBounds
        )
        val yPos = (height + textBounds.height()) / 2f
        canvas.drawText(text, width / 2f, yPos, shared.textPaint)

        // Draw indicators, whether a day has event or appointment
        val offsetDirection = if (layoutDirection == LAYOUT_DIRECTION_RTL) -1 else 1
        indicators.forEachIndexed { i, paint ->
            val xOffset = shared.eventIndicatorsCentersDistance *
                    (i - (indicators.size - 1) / 2f) * offsetDirection
            val overrideByTextColor = dayIsSelected ||
                    // use textPaint for holiday event when a11y's high contrast is enabled
                    (isHighTextContrastEnabled && holiday && paint == shared.eventIndicatorPaint)
            canvas.drawCircle(
                width / 2f + xOffset, height - shared.eventYOffset,
                shared.eventIndicatorRadius, if (overrideByTextColor) shared.textPaint else paint
            )
        }

        // Draw day header which is used for shit work
        if (header.isNotEmpty()) {
            shared.headerTextPaint.color =
                if (dayIsSelected) shared.colorTextDaySelected else shared.colorTextDay
            canvas.drawText(
                header, width / 2f, yPos * 0.87f - textBounds.height(),
                shared.headerTextPaint
            )
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
                    shared.headerTextPaint
                )
            }
        }
    }

    private fun setAll(
        text: String, isToday: Boolean, isSelected: Boolean,
        hasEvent: Boolean, hasAppointment: Boolean, isHoliday: Boolean,
        textSize: Int, jdn: Jdn?, dayOfMonth: Int, isDayOfMonth: Boolean,
        header: String
    ) {
        this.text = text
        this.today = isToday
        this.dayIsSelected = isSelected
        this.holiday = isHoliday
        this.jdn = jdn
        this.dayOfMonth = dayOfMonth
        this.isDayOfMonth = isDayOfMonth
        this.header = header
        this.textSize = textSize
        sharedDayViewData.debugAssertNotNull?.also { shared ->
            this.indicators = listOf(
                hasAppointment to shared.appointmentIndicatorPaint,
                hasEvent to shared.eventIndicatorPaint
            ).mapNotNull { (condition, paint) -> paint.takeIf { condition } }
            if (isDayOfMonth) setBackgroundResource(shared.selectableItemBackground)
        }
        postInvalidate()
    }

    fun setDayOfMonthItem(
        isToday: Boolean, isSelected: Boolean,
        hasEvent: Boolean, hasAppointment: Boolean, isHoliday: Boolean,
        jdn: Jdn, dayOfMonth: Int, header: String
    ) = setAll(
        formatNumber(dayOfMonth), isToday, isSelected, hasEvent, hasAppointment,
        isHoliday, sharedDayViewData?.digitsTextSize ?: 0, jdn, dayOfMonth, true, header
    )

    private fun setNonDayOfMonthItem(text: String, textSize: Int) = setAll(
        text, isToday = false, isSelected = false, hasEvent = false, hasAppointment = false,
        isHoliday = false, textSize = textSize, jdn = null, dayOfMonth = -1,
        isDayOfMonth = false, header = ""
    )

    fun setWeekOfYearNumber(text: String) =
        setNonDayOfMonthItem(text, sharedDayViewData?.weekNumberTextSize ?: 0)

    fun setInitialOfWeekDay(text: String) =
        setNonDayOfMonthItem(text, sharedDayViewData?.weekDaysInitialTextSize ?: 0)
}
