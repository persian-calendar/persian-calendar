package com.byagowi.persiancalendar.entities

import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.cepmuvakkit.times.posAlgo.Ecliptic
import io.github.persiancalendar.calendar.PersianDate
import kotlin.math.floor

enum class Zodiac(
    private val endOfRange: Double, private val emoji: String, @StringRes private val title: Int
) {
    ARIES(33.18, "♈", R.string.aries),
    TAURUS(51.16, "♉", R.string.taurus),
    GEMINI(93.44, "♊", R.string.gemini),
    CANCER(119.48, "♋", R.string.cancer),
    LEO(135.30, "♌", R.string.leo),
    VIRGO(173.34, "♍", R.string.virgo),
    LIBRA(224.17, "♎", R.string.libra),
    SCORPIO(242.57, "♏", R.string.scorpio),
    SAGITTARIUS(271.26, "♐", R.string.sagittarius),
    CAPRICORN(302.49, "♑", R.string.capricorn),
    AQUARIUS(311.72, "♒", R.string.aquarius),
    PISCES(348.58, "♓", R.string.pisces);

    fun format(context: Context, withEmoji: Boolean, short: Boolean = false) = buildString {
        if (withEmoji && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) append("$emoji ")
        val result = context.getString(title)
        append(if (short) result.split(" (")[0] else result)
    }

    private val startOfRange get() =
        ((values().getOrNull(ordinal - 1)?.endOfRange) ?: PISCES.endOfRange - 360)

    val naturalRange get() = startOfRange..endOfRange
    val formalRange get() = ordinal * 30.0..(ordinal + 1) * 30.0

    companion object {
        fun fromPersianCalendar(persianDate: PersianDate) =
            values().getOrNull(persianDate.month - 1) ?: ARIES

        // https://github.com/janczer/goMoonPhase/blob/0363844/MoonPhase.go#L363
        fun fromNaturalEcliptic(ecliptic: Ecliptic) =
            values().firstOrNull { ecliptic.λ < it.endOfRange } ?: ARIES

        fun fromFormalEcliptic(ecliptic: Ecliptic) =
            values().getOrNull(floor(ecliptic.λ / 30).toInt()) ?: ARIES
    }
}
