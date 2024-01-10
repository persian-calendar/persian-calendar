package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils

data class MonthColors(
    @ColorInt val contentColor: Int,
    @ColorInt val appointments: Int,
    @ColorInt val holidays: Int,
    @ColorInt val eventIndicator: Int,
    @ColorInt val currentDay: Int,
    @ColorInt val textDaySelected: Int,
) {
    @ColorInt
    val colorTextDayName = ColorUtils.setAlphaComponent(contentColor, 0xCC)
}
