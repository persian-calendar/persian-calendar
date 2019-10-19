package com.byagowi.persiancalendar.ui.calendar.month

import android.app.Activity
import android.graphics.Paint
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.Utils
import com.byagowi.persiancalendar.utils.getCalendarFragmentFont

class DaysPaintResources(activity: Activity) {
    @StyleRes
    val style: Int
    @ColorInt
    internal val colorHoliday: Int
    @ColorInt
    internal val colorHolidaySelected: Int
    @ColorInt
    internal val colorTextHoliday: Int
    @ColorInt
    internal val colorTextDay: Int
    @ColorInt
    internal val colorTextDaySelected: Int
    @ColorInt
    internal val colorTextToday: Int
    @ColorInt
    internal val colorTextDayName: Int
    @ColorInt
    internal val colorSelectDay: Int
    @ColorInt
    internal val colorEventLine: Int
    internal val weekNumberTextSize: Int
    internal val weekDaysInitialTextSize: Int
    internal val arabicDigitsTextSize: Int
    internal val persianDigitsTextSize: Int
    internal val halfEventBarWidth: Int
    internal val appointmentYOffset: Int
    internal val eventYOffset: Int
    internal val textPaint: Paint
    internal val eventBarPaint: Paint
    internal val selectedPaint: Paint
    internal val todayPaint: Paint

    init {
        val theme = activity.theme
        val value = TypedValue()

        theme.resolveAttribute(R.attr.colorHoliday, value, true)
        colorHoliday = ContextCompat.getColor(activity, value.resourceId)

        theme.resolveAttribute(R.attr.colorHolidaySelected, value, true)
        colorHolidaySelected = ContextCompat.getColor(activity, value.resourceId)

        theme.resolveAttribute(R.attr.colorTextHoliday, value, true)
        colorTextHoliday = ContextCompat.getColor(activity, value.resourceId)

        theme.resolveAttribute(R.attr.colorTextDay, value, true)
        colorTextDay = ContextCompat.getColor(activity, value.resourceId)

        theme.resolveAttribute(R.attr.colorTextDaySelected, value, true)
        colorTextDaySelected = ContextCompat.getColor(activity, value.resourceId)

        theme.resolveAttribute(R.attr.colorTextToday, value, true)
        colorTextToday = ContextCompat.getColor(activity, value.resourceId)

        theme.resolveAttribute(R.attr.colorTextDayName, value, true)
        colorTextDayName = ContextCompat.getColor(activity, value.resourceId)

        theme.resolveAttribute(R.attr.colorEventLine, value, true)
        colorEventLine = ContextCompat.getColor(activity, value.resourceId)

        theme.resolveAttribute(R.attr.colorSelectDay, value, true)
        colorSelectDay = ContextCompat.getColor(activity, value.resourceId)

        style = Utils.getAppTheme()

        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        eventBarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        todayPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        val resources = activity.resources
        eventBarPaint.strokeWidth = resources.getDimensionPixelSize(R.dimen.day_item_event_bar_thickness).toFloat()

        todayPaint.style = Paint.Style.STROKE
        todayPaint.strokeWidth = resources.getDimensionPixelSize(R.dimen.day_item_today_indicator_thickness).toFloat()

        theme.resolveAttribute(R.attr.colorCurrentDay, value, true)
        todayPaint.color = ContextCompat.getColor(activity, value.resourceId)

        selectedPaint.style = Paint.Style.FILL
        selectedPaint.color = colorSelectDay

        halfEventBarWidth = resources.getDimensionPixelSize(R.dimen.day_item_event_bar_width) / 2
        eventYOffset = resources.getDimensionPixelSize(R.dimen.day_item_event_y_offset)
        appointmentYOffset = resources.getDimensionPixelSize(R.dimen.day_item_appointment_y_offset)
        weekNumberTextSize = resources.getDimensionPixelSize(R.dimen.day_item_week_number_text_size)
        weekDaysInitialTextSize = resources.getDimensionPixelSize(R.dimen.day_item_week_days_initial_text_size)
        arabicDigitsTextSize = resources.getDimensionPixelSize(R.dimen.day_item_arabic_digits_text_size)
        persianDigitsTextSize = resources.getDimensionPixelSize(R.dimen.day_item_persian_digits_text_size)

        textPaint.typeface = getCalendarFragmentFont(activity)
    }
}
