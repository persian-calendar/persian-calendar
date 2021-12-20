package com.byagowi.persiancalendar.entities

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R

enum class Zodiac(val emoji: String, @StringRes val title: Int) {
    ARIES("♈", R.string.aries),
    TAURUS("♉", R.string.taurus),
    GEMINI("♊", R.string.gemini),
    CANCER("♋", R.string.cancer),
    LEO("♌", R.string.leo),
    VIRGO("♍", R.string.virgo),
    LIBRA("♎", R.string.libra),
    SCORPIO("♏", R.string.scorpio),
    SAGITTARIUS("♐", R.string.sagittarius),
    CAPRICORN("♑", R.string.capricorn),
    AQUARIUS("♒", R.string.aquarius),
    PISCES("♓", R.string.pisces)
}
