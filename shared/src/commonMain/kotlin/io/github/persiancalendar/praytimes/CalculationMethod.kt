package io.github.persiancalendar.praytimes

enum class CalculationMethod(
    internal val fajr: MinuteOrAngleDouble,
    internal val isha: MinuteOrAngleDouble,
    internal val maghrib: MinuteOrAngleDouble = 0.min,
    val defaultMidnight: MidnightMethod = MidnightMethod.MidSunsetToSunrise
) {
    /** Muslim World League */
    MWL(fajr = 18.deg, isha = 17.deg),

    /** Islamic Society of North America (ISNA) */
    ISNA(fajr = 15.deg, isha = 15.deg),

    /** Egyptian General Authority of Survey */
    Egypt(fajr = 19.5.deg, isha = 17.5.deg),

    /** Umm Al-Qura University, Makkah */
    Makkah(fajr = 18.5.deg, isha = 90.min),

    /** University of Islamic Sciences, Karachi */
    Karachi(fajr = 18.deg, isha = 18.deg),

    /** Institute of Geophysics, University of Tehran */
    Tehran(
        fajr = 17.7.deg,
        isha = 14.deg,
        maghrib = 4.5.deg,
        defaultMidnight = MidnightMethod.MidSunsetToFajr
    ),

    /** Shia Ithna-Ashari, Leva Institute, Qum */
    Jafari(
        fajr = 16.deg,
        isha = 14.deg,
        maghrib = 4.deg,
        defaultMidnight = MidnightMethod.MidSunsetToFajr
    ),

    /** France */
    France(fajr = 12.deg, isha = 12.deg),

    /** Russia */
    Russia(fajr = 16.deg, isha = 15.deg),

    /** Singapore */
    Singapore(fajr = 20.deg, isha = 18.deg);

    /** Is the calculation method a Jafari one */
    val isJafari = defaultMidnight == MidnightMethod.MidSunsetToFajr

}
