package com.byagowi.persiancalendar.ui.map

import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.crescent_evening_visibility_odeh
import com.byagowi.persiancalendar.shared.generated.resources.crescent_evening_visibility_yallop
import com.byagowi.persiancalendar.shared.generated.resources.crescent_morning_visibility_odeh
import com.byagowi.persiancalendar.shared.generated.resources.crescent_morning_visibility_yallop
import com.byagowi.persiancalendar.shared.generated.resources.magnetic_declination
import com.byagowi.persiancalendar.shared.generated.resources.magnetic_field_strength
import com.byagowi.persiancalendar.shared.generated.resources.magnetic_inclination
import com.byagowi.persiancalendar.shared.generated.resources.moon_visibility
import com.byagowi.persiancalendar.shared.generated.resources.none
import com.byagowi.persiancalendar.shared.generated.resources.show_night_mask_label
import com.byagowi.persiancalendar.shared.generated.resources.tectonic_plates
import com.byagowi.persiancalendar.shared.generated.resources.time_zones
import org.jetbrains.compose.resources.StringResource

enum class MapType(val title: StringResource, val isCrescentVisibility: Boolean = false) {
    NONE(Res.string.none),
    DAY_NIGHT(Res.string.show_night_mask_label),
    MOON_VISIBILITY(Res.string.moon_visibility),
    MAGNETIC_FIELD_STRENGTH(Res.string.magnetic_field_strength),
    MAGNETIC_DECLINATION(Res.string.magnetic_declination),
    MAGNETIC_INCLINATION(Res.string.magnetic_inclination),
    TIME_ZONES(Res.string.time_zones),
    TECTONIC_PLATES(Res.string.tectonic_plates),
    EVENING_YALLOP(Res.string.crescent_evening_visibility_yallop, isCrescentVisibility = true),
    EVENING_ODEH(Res.string.crescent_evening_visibility_odeh, isCrescentVisibility = true),
    MORNING_YALLOP(Res.string.crescent_morning_visibility_yallop, isCrescentVisibility = true),
    MORNING_ODEH(Res.string.crescent_morning_visibility_odeh, isCrescentVisibility = true)
}
