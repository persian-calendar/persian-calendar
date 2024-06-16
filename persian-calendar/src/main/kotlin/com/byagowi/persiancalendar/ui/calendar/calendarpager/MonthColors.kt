package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.compose.ui.graphics.Color
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha

data class MonthColors(
    val contentColor: Color,
    val appointments: Color,
    val holidays: Color,
    val eventIndicator: Color,
    val currentDay: Color,
    val textDaySelected: Color,
    val indicator: Color,
) {
    val colorTextDayName = contentColor.copy(alpha = AppBlendAlpha)
}
