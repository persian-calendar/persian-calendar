package com.byagowi.persiancalendar.ui.astronomy

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R

enum class AstronomyMode(@get:StringRes val title: Int, @get:DrawableRes val icon: Int) {
    EARTH(R.string.earth, R.drawable.ic_earth),
    MOON(R.string.moon, R.drawable.ic_moon),
    SUN(R.string.sun, R.drawable.ic_sun),
}
