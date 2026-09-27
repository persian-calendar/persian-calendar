package com.byagowi.persiancalendar.ui.astronomy

import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.earth
import com.byagowi.persiancalendar.shared.generated.resources.ic_earth
import com.byagowi.persiancalendar.shared.generated.resources.ic_moon
import com.byagowi.persiancalendar.shared.generated.resources.ic_sun
import com.byagowi.persiancalendar.shared.generated.resources.moon
import com.byagowi.persiancalendar.shared.generated.resources.sun
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class AstronomyMode(val title: StringResource, val icon: DrawableResource) {
    EARTH(title = Res.string.earth, icon = Res.drawable.ic_earth),
    MOON(title = Res.string.moon, icon = Res.drawable.ic_moon),
    SUN(title = Res.string.sun, icon = Res.drawable.ic_sun),
}
