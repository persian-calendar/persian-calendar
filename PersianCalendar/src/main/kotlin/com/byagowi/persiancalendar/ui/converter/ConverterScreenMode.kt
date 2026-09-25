package com.byagowi.persiancalendar.ui.converter

import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.calculator
import com.byagowi.persiancalendar.shared.generated.resources.date_converter
import com.byagowi.persiancalendar.shared.generated.resources.days_distance
import com.byagowi.persiancalendar.shared.generated.resources.qr_code
import com.byagowi.persiancalendar.shared.generated.resources.time_zones
import org.jetbrains.compose.resources.StringResource

enum class ConverterScreenMode(val title: StringResource, val backspaceReset: Boolean = false) {
    CONVERTER(Res.string.date_converter),
    DISTANCE(Res.string.days_distance),
    CALCULATOR(Res.string.calculator, backspaceReset = true),
    TIME_ZONES(Res.string.time_zones),
    QR_CODE(Res.string.qr_code, backspaceReset = true),
}
