package io.github.persiancalendar.praytimes

data class Coordinates(
    /** Observer latitude, [0-90) */
    val latitude: Double,
    /** Observer longitude, [0-180) */
    val longitude: Double,
    /** Observer height/elevation/altitude in meters, 0 is global seas level */
    val elevation: Double
)
