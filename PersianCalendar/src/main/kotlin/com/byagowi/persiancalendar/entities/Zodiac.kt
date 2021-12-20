package com.byagowi.persiancalendar.entities

import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.cepmuvakkit.times.posAlgo.Ecliptic
import io.github.persiancalendar.calendar.PersianDate

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
    PISCES("♓", R.string.pisces);

    companion object {
        fun fromPersianCalendar(persianDate: PersianDate) =
            values().getOrNull(persianDate.month - 1) ?: ARIES

        // https://github.com/janczer/goMoonPhase/blob/0363844/MoonPhase.go#L363
        fun fromEcliptic(ecliptic: Ecliptic) = when {
            ecliptic.λ < 33.18 -> ARIES
            ecliptic.λ < 51.16 -> TAURUS
            ecliptic.λ < 93.44 -> GEMINI
            ecliptic.λ < 119.48 -> CANCER
            ecliptic.λ < 135.30 -> LEO
            ecliptic.λ < 173.34 -> VIRGO
            ecliptic.λ < 224.17 -> LIBRA
            ecliptic.λ < 242.57 -> SCORPIO
            ecliptic.λ < 271.26 -> SAGITTARIUS
            ecliptic.λ < 302.49 -> CAPRICORN
            ecliptic.λ < 311.72 -> AQUARIUS
            ecliptic.λ < 348.58 -> PISCES
            else -> ARIES
        }
    }
}
