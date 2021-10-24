package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.TypedValue
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.resolveBoolean
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.sp
import com.byagowi.persiancalendar.utils.isArabicDigitSelected

class SharedDayViewData(
    context: Context, height: Float, @ColorInt private val widgetTextColor: Int? = null
) {

    val isArabicScript = language.isArabicScript
    val dayOffset = if (isArabicDigitSelected) 0f else 3.sp
    val circlesPadding = 1.dp
    val eventYOffset = height * 7 / 40
    val eventIndicatorRadius = height * 2 / 40
    private val eventIndicatorsGap = height * 2 / 40
    val eventIndicatorsCentersDistance = 2 * eventIndicatorRadius + eventIndicatorsGap

    val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height.toInt())

    private fun addShadowIfNeeded(paint: Paint) {
        if (widgetTextColor != null) paint.setShadowLayer(1f, 1f, 1f, Color.BLACK)
    }

    @IdRes
    val selectableItemBackground = if (widgetTextColor == null) TypedValue().also {
        context.theme.resolveAttribute(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                android.R.attr.selectableItemBackgroundBorderless
            else android.R.attr.selectableItemBackground,
            it, true
        )
    }.resourceId else 0

    val appointmentIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = context.resolveColor(R.attr.colorAppointment)
    }
    val eventIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = widgetTextColor ?: context.resolveColor(R.attr.colorEventIndicator)
    }

    val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = context.resolveColor(R.attr.colorSelectDay)
    }

    val isCurrentDayOutlineOnly = context.resolveBoolean(R.attr.colorCurrentDayIsOutlineOnly)
    val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = if (isCurrentDayOutlineOnly) Paint.Style.STROKE else Paint.Style.FILL
        it.strokeWidth = 1.dp
        it.color = widgetTextColor ?: context.resolveColor(R.attr.colorCurrentDay)
    }

    private val textSize = height * (if (isArabicDigitSelected) 18 else 25) / 40
    private val headerTextSize = height * 12 / 40

    val dayOfMonthNumberTextHolidayPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = textSize
        it.color = context.resolveColor(R.attr.colorHoliday)
        addShadowIfNeeded(it)
    }

    private val colorTextDay = widgetTextColor ?: context.resolveColor(R.attr.colorTextDay)
    val dayOfMonthNumberTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = textSize
        it.color = colorTextDay
        addShadowIfNeeded(it)
    }
    val dayOfMonthNumberCurrentTextPaint =
        if (isCurrentDayOutlineOnly) dayOfMonthNumberTextPaint
        else Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.textAlign = Paint.Align.CENTER
            it.textSize = textSize
            it.color = context.resolveColor(R.attr.colorTextDayCurrent)
            addShadowIfNeeded(it)
        }
    val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = headerTextSize
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

    private val colorTextDayName = widgetTextColor ?: context.resolveColor(R.attr.colorTextDayName)
    val weekNumberTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = headerTextSize
        it.color = colorTextDayName
        addShadowIfNeeded(it)
    }
    val weekDayInitialsTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = height * 20 / 40
        it.color = colorTextDayName
        addShadowIfNeeded(it)
    }

    val widgetFooterTextPaint = widgetTextColor?.let { widgetTextColor ->
        Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.textAlign = Paint.Align.CENTER
            it.textSize = height * 20 / 40
            it.color = widgetTextColor
            it.alpha = 90
        }
    }
}
