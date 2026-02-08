package com.byagowi.persiancalendar.ui.converter

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R

enum class ConverterScreenMode(@get:StringRes val title: Int, val backspaceReset: Boolean = false) {
    CONVERTER(R.string.date_converter),
    DISTANCE(R.string.days_distance),
    CALCULATOR(R.string.calculator, backspaceReset = true),
    TIME_ZONES(R.string.time_zones),
    QR_CODE(R.string.qr_code, backspaceReset = true),
}
