package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.compose.ui.graphics.Color
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha

data class MonthColors(
    val contentColor: Color,
    val appointments: Color,
    val holidays: Color,
    val eventIndicator: Color,
    val todayOutline: Color,
    val indicatorFill: Color,
    val holidaysFill: Color,
) {
    val colorTextDayName = contentColor.copy(alpha = AppBlendAlpha)
}
