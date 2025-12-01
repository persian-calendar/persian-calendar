package com.byagowi.persiancalendar.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.byagowi.persiancalendar.global.language
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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

suspend fun Coordinates.geocode(context: Context): Address? = withContext(Dispatchers.IO) {
    val geocoder = Geocoder(context, language.value.asSystemLocale())
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) suspendCancellableCoroutine { cont ->
        runCatching {
            geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                if (cont.isActive) cont.resume(addresses.firstOrNull()) { _, _, _ -> }
            }
        }.onFailure(logException).onFailure {
            if (cont.isActive) cont.resume(null) { _, _, _ -> }
        }.getOrNull().debugAssertNotNull
    } else @Suppress("DEPRECATION") runCatching {
        geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
    }.onFailure(logException).getOrNull().debugAssertNotNull
}
