package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.util.TypedValue
import android.view.ViewGroup
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.sp
import com.byagowi.persiancalendar.utils.getCalendarFragmentFont
import com.byagowi.persiancalendar.utils.isArabicDigitSelected

class SharedDayViewData(context: Context) {

    val colorHoliday = context.resolveColor(R.attr.colorHoliday)
    val colorHolidaySelected = context.resolveColor(R.attr.colorHolidaySelected)

    // private val colorTextHoliday = context.resolveColor(R.attr.colorTextHoliday)
    val colorTextDay = context.resolveColor(R.attr.colorTextDay)
    val colorTextDaySelected = context.resolveColor(R.attr.colorTextDaySelected)

    // private val colorTextToday = context.resolveColor(R.attr.colorTextToday)
    val colorTextDayName = context.resolveColor(R.attr.colorTextDayName)

    val appointmentIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = context.resolveColor(com.google.android.material.R.attr.colorSecondary)
    }
    val eventIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = context.resolveColor(R.attr.colorEventIndicator)
    }

    val eventYOffset = 7.sp
    val eventIndicatorRadius = 2.sp
    private val eventIndicatorsGap = 2.sp
    val eventIndicatorsCentersDistance = 2 * eventIndicatorRadius + eventIndicatorsGap

    val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = context.resolveColor(R.attr.colorSelectDay)
    }

    val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = 1.dp
        it.color = context.resolveColor(R.attr.colorCurrentDay)
    }

    private val typeface = getCalendarFragmentFont(context)
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.typeface = typeface
    }
    val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.typeface = typeface
    }

    val layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, 40.sp.toInt()
    )

    val weekNumberTextSize = 12.sp.toInt()
    val weekDaysInitialTextSize = 20.sp.toInt()
    val digitsTextSize = if (isArabicDigitSelected()) 18.sp.toInt() else 25.sp.toInt()

    val selectableItemBackground = TypedValue().also {
        context.theme.resolveAttribute(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                android.R.attr.selectableItemBackgroundBorderless
            else android.R.attr.selectableItemBackground,
            it, true
        )
    }.resourceId
}
