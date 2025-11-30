package com.byagowi.persiancalendar.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.byagowi.persiancalendar.global.language
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

// https://stackoverflow.com/a/62499553
// https://en.wikipedia.org/wiki/ISO_6709#Representation_at_the_human_interface_(Annex_D)
fun formatCoordinateISO6709(lat: Double, long: Double, alt: Double? = null) = listOf(
    abs(lat) to if (lat >= 0) "N" else "S", abs(long) to if (long >= 0) "E" else "W"
).joinToString(" ") { (degree: Double, direction: String) ->
    val minutes = ((degree - degree.toInt()) * 60).toInt()
    val seconds = ((degree - degree.toInt()) * 3600 % 60).toInt()
    "%d°%02d′%02d″%s".format(Locale.US, degree.toInt(), minutes, seconds, direction)
} + alt?.let { " %s%.1fm".format(Locale.US, if (alt < 0) "−" else "", abs(alt)) }.orEmpty()

val Address.friendlyName: String?
    get() = listOf(locality, subAdminArea, adminArea).firstOrNull { !it.isNullOrBlank() }

fun CoroutineScope.geocode(
    context: Context,
    latitude: Double,
    longitude: Double,
    onResult: (Address) -> Unit,
) {
    val geocoder = Geocoder(context, language.value.asSystemLocale())
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) runCatching {
        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
            addresses.firstOrNull()?.also(onResult)
        }
    }.onFailure(logException).getOrNull() else launch(Dispatchers.IO) {
        @Suppress("DEPRECATION") runCatching {
            val address = geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
            address?.also(onResult)
        }.onFailure(logException).getOrNull()
    }
}
