package io.github.persiancalendar.praytimes

/** Adjust Methods for Higher Latitudes, e.g. Scandinavian countries */
enum class HighLatitudesMethod {
    /** Middle of night, the default */
    NightMiddle,

    /** Angle/60th of night */
    AngleBased,

    /** 1/7th of night */
    OneSeventh,

    /** No adjustment */
    None
}
