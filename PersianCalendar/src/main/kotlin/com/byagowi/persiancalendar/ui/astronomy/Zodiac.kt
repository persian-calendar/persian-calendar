package com.byagowi.persiancalendar.ui.astronomy

import android.content.res.Resources
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import io.github.persiancalendar.calendar.PersianDate
import kotlin.math.floor

/**
 * The following table is copied from https://en.wikipedia.org/wiki/Sidereal_and_tropical_astrology
 *
 * | Constellation |       Tropical date       |       Sidereal Date       |  Based on IAU boundaries  |
 * |---------------|---------------------------|---------------------------|---------------------------|
 * | Aries         | March 21 – April 19       | April 15 – May 15         | April 18 – May 13         |
 * | Taurus        | April 20 – May 20         | May 16 – June 15          | May 13 – June 21          |
 * | Gemini        | May 21 – June 20          | June 16 – July 16         | June 21 – July 20         |
 * | Cancer        | June 21 – July 22         | July 17 – August 16       | July 20 – August 10       |
 * | Leo           | July 23 – August 22       | August 17 – September 16  | August 10 – September 16  |
 * | Virgo         | August 23 – September 22  | September 17 – October 17 | September 16 – October 30 |
 * | Libra         | September 23 – October 22 | October 18 – November 16  | October 30 – November 23  |
 * | Scorpio       | October 23 – November 21  | November 17 – December 16 | November 23 – November 29 |
 * | Sagittarius   | November 22 – December 21 | December 17 – January 15  | December 17 – January 20  |
 * | Capricorn     | December 22 – January 19  | January 16 – February 14  | January 20 – February 16  |
 * | Aquarius      | January 21 – February 19  | February 15 – March 15    | February 16 – March 11    |
 * | Pisces        | February 19 – March 20    | March 16 – April 14       | March 11 – April 18       |
 *
 * Ours should match with tropical and IAU boundaries ones but we are interesting on having having
 * others types also such Vedic and Sidereal if is easy to do and we can find their degrees.
 *
 * iauRangeEnd values can be found on the following places:
 * * https://github.com/janczer/goMoonPhase/blob/0363844/MoonPhase.go#L363
 * * https://github.com/emvakar/EKAstrologyCalc/blob/ee0cd57/Sources/EKAstrologyCalc/Calculators/EKMoonZodiacSignCalculator.swift#L70
 * * https://www.scribd.com/doc/83082081/Moon-Phase-Calculator
 * * https://www.mail-archive.com/amibroker@yahoogroups.com/msg04288.html
 * * https://github.com/giboow/mooncalc/blob/5df85ab/mooncalc.js#L103
 * * https://github.com/mfzhang/AB.Formulae/blob/bbab96c/ZakirBoss-Mubarak/Formulas/Custom/Luna%20Phase.afl
 * * https://github.com/chan/vios/blob/3430b89/autoload/time/moon.vim
 * * https://github.com/BGCX262/zweer-gdr-svn-to-git/blob/6d85903/trunk/library/Zwe/Weather/Moon.php
 *
 */
enum class Zodiac(
    private val iauRangeEnd: Double, val emoji: String, @StringRes private val title: Int
) {
    ARIES(33.18, "♈", R.string.aries), // 0-30 (Tropical)
    TAURUS(51.16, "♉", R.string.taurus), // 30-60
    GEMINI(93.44, "♊", R.string.gemini), // 60-90
    CANCER(119.48, "♋", R.string.cancer), // 90-120
    LEO(135.30, "♌", R.string.leo), // 120-150
    VIRGO(173.34, "♍", R.string.virgo), // 150-180
    LIBRA(224.17, "♎", R.string.libra), // 180-210
    SCORPIO(242.57, "♏", R.string.scorpio), // 210-240
    SAGITTARIUS(271.26, "♐", R.string.sagittarius), // 240-270
    CAPRICORN(302.49, "♑", R.string.capricorn), // 270-300
    AQUARIUS(311.72, "♒", R.string.aquarius), // 300-330
    PISCES(348.58, "♓", R.string.pisces); // 330-360

    fun format(resources: Resources, withEmoji: Boolean, short: Boolean = false) = buildString {
        if (withEmoji) append("$emoji ")
        val result = resources.getString(title)
        append(if (short) result.split(" (")[0] else result)
    }

    private val iauPreviousRangeEnd: Double
        get() = entries.getOrNull(ordinal - 1)?.iauRangeEnd ?: (PISCES.iauRangeEnd - 360)

    val iauRange get() = listOf(iauPreviousRangeEnd, iauRangeEnd)
    val tropicalRange get() = listOf(ordinal * 30.0, (ordinal + 1) * 30.0)

    companion object {
        fun fromPersianCalendar(persianDate: PersianDate): Zodiac =
            entries.getOrNull(persianDate.month - 1) ?: ARIES

        fun fromIau(longitude: Double): Zodiac =
            entries.firstOrNull { longitude < it.iauRangeEnd } ?: ARIES

        fun fromTropical(longitude: Double): Zodiac =
            entries.getOrNull(floor((longitude - 15) / 30).toInt()) ?: PISCES
    }
}
