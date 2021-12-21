package com.byagowi.persiancalendar.utils

import android.content.Context
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.spacedColon
import io.github.persiancalendar.praytimes.Coordinates
import java.util.*
import kotlin.math.abs

fun formatCoordinate(context: Context, coordinates: Coordinates, separator: String) =
    "%s$spacedColon%.2f%s%s$spacedColon%.7f".format(
        Locale.getDefault(),
        context.getString(R.string.latitude), coordinates.latitude, separator,
        context.getString(R.string.longitude), coordinates.longitude
    )

// https://stackoverflow.com/a/62499553
// https://en.wikipedia.org/wiki/ISO_6709#Representation_at_the_human_interface_(Annex_D)
fun formatCoordinateISO6709(lat: Double, long: Double, alt: Double? = null) = listOf(
    abs(lat) to if (lat >= 0) "N" else "S", abs(long) to if (long >= 0) "E" else "W"
).joinToString(" ") { (degree: Double, direction: String) ->
    val minutes = ((degree - degree.toInt()) * 60).toInt()
    val seconds = ((degree - degree.toInt()) * 3600 % 60).toInt()
    "%d°%02d′%02d″%s".format(Locale.US, degree.toInt(), minutes, seconds, direction)
} + (alt?.let { " %s%.1fm".format(Locale.US, if (alt < 0) "−" else "", abs(alt)) } ?: "")
