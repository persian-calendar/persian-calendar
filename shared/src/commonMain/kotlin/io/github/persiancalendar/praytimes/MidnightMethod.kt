package io.github.persiancalendar.praytimes

/** Different midnight calculation methods */
enum class MidnightMethod(
    /** Is the method can only be only meaningful in Jafari where Sunset and Maghrib are different */
    val isJafariOnly: Boolean = false
) {
    /** Mid Sunset to Sunrise, Non-Jafari's default */
    MidSunsetToSunrise,

    /** Mid Sunset to Fajr, Jafari's default */
    MidSunsetToFajr,

    /** Mid Maghrib to Sunrise */
    MidMaghribToSunrise(isJafariOnly = true),

    /** Mid Maghrib to Fajr */
    MidMaghribToFajr(isJafariOnly = true),
}