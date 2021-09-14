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
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.sp
import com.byagowi.persiancalendar.utils.getCalendarFragmentFont
import com.byagowi.persiancalendar.utils.isArabicDigitSelected

class SharedDayViewData(context: Context, @ColorInt private val widgetTextColor: Int? = null) {

    val eventYOffset = 7.sp
    val eventIndicatorRadius = 2.sp
    private val eventIndicatorsGap = 2.sp
    val eventIndicatorsCentersDistance = 2 * eventIndicatorRadius + eventIndicatorsGap

    val layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, 40.sp.toInt()
    )

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
        it.color = context.resolveColor(com.google.android.material.R.attr.colorSecondary)
    }
    val eventIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = widgetTextColor ?: context.resolveColor(R.attr.colorEventIndicator)
    }

    val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = context.resolveColor(R.attr.colorSelectDay)
    }

    val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = 1.dp
        it.color = widgetTextColor ?: context.resolveColor(R.attr.colorCurrentDay)
    }

    private val typeface = getCalendarFragmentFont(context)

    val dayOfMonthNumberTextHolidayPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.typeface = typeface
        it.textSize = if (isArabicDigitSelected) 18.sp else 25.sp
        it.color = context.resolveColor(R.attr.colorHoliday)
        addShadowIfNeeded(it)
    }

    private val colorTextDay = widgetTextColor ?: context.resolveColor(R.attr.colorTextDay)
    val dayOfMonthNumberTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.typeface = typeface
        it.textSize = if (isArabicDigitSelected) 18.sp else 25.sp
        it.color = colorTextDay
        addShadowIfNeeded(it)
    }
    val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.typeface = typeface
        it.textSize = 12.sp
        it.color = colorTextDay
        addShadowIfNeeded(it)
    }

    private val colorTextDaySelected =
        widgetTextColor ?: context.resolveColor(R.attr.colorTextDaySelected)
    val dayOfMonthNumberTextSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.typeface = typeface
        it.textSize = if (isArabicDigitSelected) 18.sp else 25.sp
        it.color = colorTextDaySelected
        addShadowIfNeeded(it)
    }
    val headerTextSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.typeface = typeface
        it.textSize = 12.sp
        it.color = colorTextDaySelected
        addShadowIfNeeded(it)
    }

    private val colorTextDayName = widgetTextColor ?: context.resolveColor(R.attr.colorTextDayName)
    val weekNumberTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.typeface = typeface
        it.textSize = 12.sp
        it.color = colorTextDayName
        addShadowIfNeeded(it)
    }
    val weekDayInitialsTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.typeface = typeface
        it.textSize = 20.sp
        it.color = colorTextDayName
        addShadowIfNeeded(it)
    }
}
