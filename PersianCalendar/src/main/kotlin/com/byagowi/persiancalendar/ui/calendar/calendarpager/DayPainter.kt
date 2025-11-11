package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isHighTextContrastEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendarNumeral
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.showMoonInScorpio
import com.byagowi.persiancalendar.ui.astronomy.Zodiac
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.utils.getSecondaryCalendarNumeral
import com.byagowi.persiancalendar.utils.isMoonInScorpio
import java.io.File
import kotlin.math.min

class DayPainter(
    context: Context,
    resources: Resources,
    val width: Float,
    val height: Float,
    private val isRtl: Boolean,
    colors: MonthColors,
    fontFile: File? = null,
    isBoldFont: Boolean = false,
    isWidget: Boolean = false,
    isYearView: Boolean = false,
    selectedDayColor: Int? = null,
    holidayCircleColor: Int? = null,
) {
    private val paints = Paints(
        resources, min(width, height), colors, isWidget, isYearView,
        selectedDayColor, holidayCircleColor,
        typeface = fontFile?.let(Typeface::createFromFile).let {
            if (isBoldFont) Typeface.create(it, Typeface.BOLD) else it
        },
        isBoldFont = isBoldFont,
        zodiacFont = ResourcesCompat.getFont(
            context,
            R.font.notosanssymbolsregularzodiacsubset
        )
    )
    private var text = ""
    private var today = false
    private var dayIsSelected = false
    private var indicators = emptyList<Paint>()
    private var holiday = false
    var jdn: Jdn? = null
        private set
    private var isWeekNumber = false
    private var isMoonInScorpio = false
    private var header = ""

    fun drawDay(canvas: Canvas) {
        drawCircle(canvas) // background circle, if is needed
        drawText(canvas) // can be a day number, week day name abbr or week number of year
        drawIndicators(canvas) // whether a day has event or appointment
        drawHeader(canvas) // shift work header
    }

    private fun drawCircle(canvas: Canvas) {
        if (holiday) paints.holidayPaint?.let {
            canvas.drawCircle(
                width / 2,
                height / 2,
                min(width, height) / 2 - paints.circlePadding,
                it,
            )
        }
        if (dayIsSelected) paints.selectedDayPaint?.let { selectedDayPaint ->
            canvas.drawCircle(
                width / 2,
                height / 2,
                min(width, height) / 2 - paints.circlePadding,
                selectedDayPaint
            )
        }
        if (today) canvas.drawCircle(
            width / 2,
            height / 2,
            min(width, height) / 2 - paints.circlePadding,
            paints.todayPaint,
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
        canvas.drawText(text, width / 2f, yPos + paints.dayOffsetY, textPaint)
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
                    isHighTextContrastEnabled.value && holiday && paint == paints.eventIndicatorPaint ->
                        paints.dayOfMonthNumberTextHolidayPaint

                    else -> paint
                }
            )
        }
    }

    private fun drawHeader(canvas: Canvas) {
        // The logic has become tedious as it tries to renders with two different fonts…
        val spaceWidth = if (header.isNotEmpty() && isMoonInScorpio) width / 25f else 0f
        val headerWidth = if (header.isNotEmpty()) {
            paints.headerTextSelectedPaint.measureText(header)
        } else 0f
        val zodiacWidth = if (isMoonInScorpio) run {
            paints.zodiacHeaderTextSelectedPaint.measureText(Zodiac.SCORPIO.symbol)
        } else 0f
        val lineWidth = headerWidth + zodiacWidth + spaceWidth
        if (header.isNotEmpty()) canvas.drawText(
            header,
            (width - lineWidth) / 2 + if (isRtl) -spaceWidth else zodiacWidth + spaceWidth,
            height / 2 + paints.headerYOffset,
            if (dayIsSelected) paints.headerTextSelectedPaint else paints.headerTextPaint
        )
        if (isMoonInScorpio) canvas.drawText(
            Zodiac.SCORPIO.symbol,
            (width - lineWidth) / 2 + if (isRtl) headerWidth + spaceWidth else -spaceWidth,
            height / 2 + paints.headerScorpioYOffset,
            if (dayIsSelected) paints.zodiacHeaderTextSelectedPaint else paints.zodiacHeaderTextPaint,
        )
    }

    private fun setAll(
        text: String, isToday: Boolean = false, isSelected: Boolean = false,
        hasEvent: Boolean = false, hasAppointment: Boolean = false, isHoliday: Boolean = false,
        jdn: Jdn? = null, header: String? = null, isWeekNumber: Boolean = false,
        secondaryCalendar: Calendar? = null,
    ) {
        this.text = text
        this.today = isToday
        this.dayIsSelected = isSelected
        this.holiday = isHoliday
        this.jdn = jdn
        this.isWeekNumber = isWeekNumber
        this.isMoonInScorpio = showMoonInScorpio.value && jdn != null && isMoonInScorpio(jdn)
        this.header = listOfNotNull(
            if (secondaryCalendar == null || jdn == null) null
            else getSecondaryCalendarNumeral(secondaryCalendar).format(
                (jdn on secondaryCalendar).dayOfMonth
            ),
            header,
        ).joinToString(" ")
        this.indicators = listOf(
            hasAppointment to paints.appointmentIndicatorPaint,
            (hasEvent || (isHighTextContrastEnabled.value && holiday)) to paints.eventIndicatorPaint
        ).mapNotNull { (condition, paint) -> paint.takeIf { condition } }
    }

    fun setDayOfMonthItem(
        isToday: Boolean, isSelected: Boolean,
        hasEvent: Boolean, hasAppointment: Boolean, isHoliday: Boolean,
        jdn: Jdn, dayOfMonth: String, header: String?, secondaryCalendar: Calendar?,
    ) = setAll(
        text = dayOfMonth, isToday = isToday,
        isSelected = isSelected, hasEvent = hasEvent, hasAppointment = hasAppointment, jdn = jdn,
        header = header, isHoliday = isHoliday,
        secondaryCalendar = secondaryCalendar,
    )

    fun setInitialOfWeekDay(text: String) = setAll(text)
    fun setWeekNumber(text: String) = setAll(text, isWeekNumber = true)
}

private class Paints(
    resources: Resources,
    diameter: Float,
    colors: MonthColors,
    isWidget: Boolean,
    isYearView: Boolean,
    @ColorInt selectedDayColor: Int?,
    @ColorInt holidayCircleColor: Int?,
    typeface: Typeface?,
    zodiacFont: Typeface?,
    isBoldFont: Boolean,
) {
    private val dp = resources.dp
    val circlePadding = .5f * dp
    val isArabicScript = language.value.isArabicScript
    val eventYOffset = diameter * 12 / 40
    val eventIndicatorRadius = diameter * 2 / 40
    private val eventIndicatorsGap = diameter * 2 / 40
    val eventIndicatorsCentersDistance = 2 * eventIndicatorRadius + eventIndicatorsGap

    private fun addShadowIfNeeded(paint: Paint) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            paint.setShadowLayer(1f, 1f, 1f, Color.BLACK)
    }

    val appointmentIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = colors.appointments.toArgb()
        if (typeface != null) it.typeface = typeface
        if (isBoldFont) it.isFakeBoldText = true
    }
    val eventIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = colors.eventIndicator.toArgb()
        if (typeface != null) it.typeface = typeface
    }

    val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = (if (isBoldFont || isHighTextContrastEnabled.value) 3 else 1) * dp
        it.color = colors.currentDay.toArgb()
        if (typeface != null) it.typeface = typeface
    }
    val selectedDayPaint = selectedDayColor?.let {
        Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.style = Paint.Style.FILL
            it.color = selectedDayColor
            if (typeface != null) it.typeface = typeface
        }
    }
    val holidayPaint = holidayCircleColor?.let { color ->
        Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.style = Paint.Style.FILL
            it.color = color
        }
    }

    private val textSize =
        diameter * (if (!mainCalendarNumeral.isArabicIndicVariants || typeface != null) 18 else 25) / 40

    // https://developer.android.com/about/versions/15/behavior-changes-15
    private val mayElegantIsForced = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
    val dayOffsetY =
        if (!mainCalendarNumeral.isArabicIndicVariants || typeface != null || mayElegantIsForced) 0f
        else diameter * 3f / 40

    private val secondaryCalendarNumeralIsArabicIndicVariants =
        getSecondaryCalendarNumeral(secondaryCalendar).isArabicIndicVariants
    private val headerTextSize =
        diameter / 40 * (if (secondaryCalendarNumeralIsArabicIndicVariants) 15 else 11)
    private val zodiacHeaderTextSize = diameter / 40 * 10
    val headerYOffset =
        -diameter * (if (secondaryCalendarNumeralIsArabicIndicVariants) 7 else 10) / 40
    val headerScorpioYOffset = -diameter * 10 / 40

    val dayOfMonthNumberTextHolidayPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = textSize
        it.color = colors.holidays.toArgb()
        if (isWidget) addShadowIfNeeded(it)
        if (typeface != null) it.typeface = typeface
    }

    val dayOfMonthNumberTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = textSize
        it.color = colors.contentColor.toArgb()
        if (isWidget) addShadowIfNeeded(it)
        if (typeface != null) it.typeface = typeface
    }

    val dayOfMonthNumberTextSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = textSize
        it.color = colors.textDaySelected.toArgb()
        if (isWidget) addShadowIfNeeded(it)
        if (typeface != null) it.typeface = typeface
    }

    val headerTextSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textSize = headerTextSize
        it.color = colors.textDaySelected.toArgb()
        if (isWidget) addShadowIfNeeded(it)
        if (typeface != null) it.typeface = typeface
    }
    val zodiacHeaderTextSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textSize = zodiacHeaderTextSize
        it.color = colors.textDaySelected.toArgb()
        if (isWidget) addShadowIfNeeded(it)
        it.typeface = zodiacFont
        if (isBoldFont) it.isFakeBoldText = true
    }
    val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textSize = headerTextSize
        it.color = colors.colorTextDayName.toArgb()
        if (isWidget) addShadowIfNeeded(it)
        if (typeface != null) it.typeface = typeface
    }
    val zodiacHeaderTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textSize = zodiacHeaderTextSize
        it.color = colors.colorTextDayName.toArgb()
        if (isWidget) addShadowIfNeeded(it)
        it.typeface = zodiacFont
        if (isBoldFont) it.isFakeBoldText = true
    }

    val weekNumberTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = if (isYearView) textSize else headerTextSize
        it.color = colors.colorTextDayName.toArgb()
        if (typeface != null) it.typeface = typeface
        if (isWidget) addShadowIfNeeded(it)
    }
    val weekDayInitialsTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = diameter * 20 / 40
        it.color = colors.colorTextDayName.toArgb()
        if (typeface != null) it.typeface = typeface
        if (isWidget) addShadowIfNeeded(it)
    }
}
