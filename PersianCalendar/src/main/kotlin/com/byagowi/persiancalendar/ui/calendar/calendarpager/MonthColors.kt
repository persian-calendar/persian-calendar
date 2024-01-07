package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils

data class MonthColors(
    @ColorInt val contentColor: Int,
    @ColorInt val colorAppointments: Int,
    @ColorInt val colorHolidays: Int,
    @ColorInt val colorEventIndicator: Int,
    @ColorInt val colorCurrentDay: Int,
    @ColorInt val colorTextDaySelected: Int,
) {
    @ColorInt
    val colorTextDayName = ColorUtils.setAlphaComponent(contentColor, 0xCC)
}
