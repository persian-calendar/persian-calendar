package com.byagowi.persiancalendar.ui.astronomy

import androidx.annotation.DrawableRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.earth
import com.byagowi.persiancalendar.shared.generated.resources.moon
import com.byagowi.persiancalendar.shared.generated.resources.sun
import org.jetbrains.compose.resources.StringResource

enum class AstronomyMode(val titleRes: StringResource, @get:DrawableRes val icon: Int) {
    EARTH(Res.string.earth, R.drawable.ic_earth),
    MOON(Res.string.moon, R.drawable.ic_moon),
    SUN(Res.string.sun, R.drawable.ic_sun),
}
