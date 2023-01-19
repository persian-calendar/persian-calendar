package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.isHighTextContrastEnabled
import com.byagowi.persiancalendar.global.mainCalendarDigits
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.secondaryCalendarDigits
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.isMoonInScorpio
import com.byagowi.persiancalendar.variants.debugAssertNotNull
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
    private var isWeekNumber = false
    private var header = ""

    var sharedDayViewData: SharedDayViewData? = null

    override fun onDraw(canvas: Canvas) {
        val shared = sharedDayViewData ?: return

        val radius = min(width, height) / 2f
        drawCircle(canvas, shared, radius) // background circle, if is needed
        drawText(canvas, shared) // can be a day number, week day name abbr or week number of year
        drawIndicators(canvas, shared) // whether a day has event or appointment
        drawHeader(canvas, shared) // shift work header
    }

    private fun drawCircle(canvas: Canvas, shared: SharedDayViewData, radius: Float) {
        if (dayIsSelected) canvas.drawCircle(
            width / 2f, height / 2f, radius - shared.circlesPadding, shared.selectedPaint
        )
        if (today) canvas.drawCircle(
            width / 2f, height / 2f, radius - shared.circlesPadding, shared.todayPaint
        )
    }

    private val textBounds = Rect()
    private fun drawText(canvas: Canvas, shared: SharedDayViewData) {
        val textPaint = when {
            jdn != null -> when {
                holiday -> shared.dayOfMonthNumberTextHolidayPaint
                dayIsSelected -> shared.dayOfMonthNumberTextSelectedPaint
                else /*!dayIsSelected*/ -> shared.dayOfMonthNumberTextPaint
            }
            isWeekNumber -> shared.weekNumberTextPaint
            else -> shared.weekDayInitialsTextPaint
        }
        // Measure a sample text to find height for vertical center aligning of the text to draw
        val sample = if (jdn != null) text else if (shared.isArabicScript) "س" else "Yy"
        textPaint.getTextBounds(sample, 0, sample.length, textBounds)
        val yPos = (height + textBounds.height()) / 2f
        // Draw day number/label
        canvas.drawText(text, width / 2f, yPos + shared.dayOffset, textPaint)
    }

    private fun drawIndicators(canvas: Canvas, shared: SharedDayViewData) {
        val offsetDirection = if (layoutDirection == LAYOUT_DIRECTION_RTL) -1 else 1
        indicators.forEachIndexed { i, paint ->
            val xOffset = shared.eventIndicatorsCentersDistance *
                    (i - (indicators.size - 1) / 2f) * offsetDirection
            canvas.drawCircle(
                width / 2f + xOffset, height / 2 + shared.eventYOffset,
                shared.eventIndicatorRadius, when {
                    dayIsSelected -> shared.headerTextSelectedPaint
                    // use textPaint for holiday event when a11y's high contrast is enabled
                    isHighTextContrastEnabled && holiday && paint == shared.eventIndicatorPaint ->
                        shared.dayOfMonthNumberTextHolidayPaint
                    else -> paint
                }
            )
        }
    }

    private fun drawHeader(canvas: Canvas, shared: SharedDayViewData) {
        if (header.isEmpty()) return
        canvas.drawText(
            header, width / 2f, height / 2 + shared.headerYOffset,
            if (dayIsSelected) shared.headerTextSelectedPaint else shared.headerTextPaint
        )
    }

    private fun setAll(
        text: String, isToday: Boolean = false, isSelected: Boolean = false,
        hasEvent: Boolean = false, hasAppointment: Boolean = false, isHoliday: Boolean = false,
        jdn: Jdn? = null, dayOfMonth: Int = -1, header: String = "",
        isWeekNumber: Boolean = false
    ) {
        this.text = text
        this.today = isToday
        this.dayIsSelected = isSelected
        this.holiday = isHoliday
        this.jdn = jdn
        this.dayOfMonth = dayOfMonth
        this.isWeekNumber = isWeekNumber
        val secondaryCalendar = secondaryCalendar
        this.header = listOfNotNull(
            if (isAstronomicalExtraFeaturesEnabled && jdn != null && isMoonInScorpio(jdn))
                sharedDayViewData?.scorpioSign else null,
            if (secondaryCalendar != null && jdn != null)
                formatNumber(jdn.toCalendar(secondaryCalendar).dayOfMonth, secondaryCalendarDigits)
            else null,
            header,
        ).joinToString(" ")
        sharedDayViewData.debugAssertNotNull?.also { shared ->
            this.indicators = listOf(
                hasAppointment to shared.appointmentIndicatorPaint,
                (hasEvent || (isHighTextContrastEnabled && holiday)) to shared.eventIndicatorPaint
            ).mapNotNull { (condition, paint) -> paint.takeIf { condition } }
            if (jdn != null) setBackgroundResource(shared.selectableItemBackground)
        }
        invalidate()
    }

    fun setDayOfMonthItem(
        isToday: Boolean, isSelected: Boolean,
        hasEvent: Boolean, hasAppointment: Boolean, isHoliday: Boolean,
        jdn: Jdn, dayOfMonth: Int, header: String
    ) = setAll(
        text = formatNumber(dayOfMonth, mainCalendarDigits), isToday = isToday,
        isSelected = isSelected, hasEvent = hasEvent, hasAppointment = hasAppointment, jdn = jdn,
        dayOfMonth = dayOfMonth, header = header, isHoliday = isHoliday || jdn.isWeekEnd()
    )

    fun setInitialOfWeekDay(text: String) = setAll(text)
    fun setWeekNumber(text: String) = setAll(text, isWeekNumber = true)
}
