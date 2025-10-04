package com.byagowi.persiancalendar.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.byagowi.persiancalendar.DEFAULT_ATHAN_VOLUME
import com.byagowi.persiancalendar.DEFAULT_CITY
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_ASR_HANAFI_JURISTIC
import com.byagowi.persiancalendar.PREF_ATHAN_VOLUME
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET_SET_DATE
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LOCAL_DIGITS
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.PREF_WEEK_ENDS
import com.byagowi.persiancalendar.PREF_WEEK_START
import com.byagowi.persiancalendar.entities.CityItem
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.overrideCoordinatesGlobalVariable
import io.github.persiancalendar.praytimes.Coordinates
import java.util.Locale

/**
 * Preferences utilities — small, safe wrappers around SharedPreferences used across the app.
 *
 * Goals of this rewrite:
 *  - Make functions defensive against malformed or missing data
 *  - Keep the external API identical so callers don't need to change
 *  - Add a few convenience helpers used by settings/UI code
 */

// Use application-scoped preferences file (matching previous behaviour)
val Context.preferences: SharedPreferences
    get() = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)

/** Store a JDN value as long in preferences via editor */
fun SharedPreferences.Editor.putJdn(key: String, jdn: Jdn) {
    putLong(key, jdn.value)
}

/** Read a JDN stored as long or return null if absent */
fun SharedPreferences.getJdnOrNull(key: String): Jdn? =
    getLong(key, -1L).takeIf { it != -1L }?.let(::Jdn)

/** Whether stored Islamic offset date is older than 30 days (or not set) */
val SharedPreferences.isIslamicOffsetExpired: Boolean
    get() = getJdnOrNull(PREF_ISLAMIC_OFFSET_SET_DATE)?.let { Jdn.today() - it > 30 } != false

/** Athan volume helper (with default) */
val SharedPreferences.athanVolume: Int
    get() = getInt(PREF_ATHAN_VOLUME, DEFAULT_ATHAN_VOLUME)

/** Save a CityItem as the current selected city (clears any geocoded location) */
fun SharedPreferences.saveCity(city: CityItem) = edit {
    listOf(PREF_GEOCODED_CITYNAME, PREF_LATITUDE, PREF_LONGITUDE, PREF_ALTITUDE).forEach(::remove)
    putString(PREF_SELECTED_LOCATION, city.key)
}

/**
 * Save a geographic location (latitude/longitude/elevation) and an optional geocoded city name.
 * If countryCode == "IR" we intentionally store elevation as 0.0 (project behaviour).
 */
fun SharedPreferences.saveLocation(
    coordinates: Coordinates,
    cityName: String,
    countryCode: String = "IR"
) {
    edit {
        putString(PREF_LATITUDE, "%f".format(Locale.ENGLISH, coordinates.latitude))
        putString(PREF_LONGITUDE, "%f".format(Locale.ENGLISH, coordinates.longitude))
        val elevation = if (countryCode == "IR") 0.0 else coordinates.elevation
        putString(PREF_ALTITUDE, "%f".format(Locale.ENGLISH, elevation))
        putString(PREF_GEOCODED_CITYNAME, cityName)
        putString(PREF_SELECTED_LOCATION, DEFAULT_CITY)
    }
    // Update global coordinate override so the rest of the app immediately uses the new location
    overrideCoordinatesGlobalVariable(coordinates)
}

/** Save language-related preferences and sensible defaults for holidays/calendars */
fun SharedPreferences.saveLanguage(language: Language) = edit {
    putString(PREF_APP_LANGUAGE, language.code)
    putBoolean(PREF_LOCAL_DIGITS, language.prefersLocalDigits)

    when {
        language.isAfghanistanExclusive -> {
            val enabledHolidays = EventsRepository(this@saveLanguage, language, emptySet())
            if (enabledHolidays.isEmpty || enabledHolidays.onlyIranHolidaysIsEnabled) {
                putStringSet(PREF_HOLIDAY_TYPES, EventsRepository.afghanistanDefault)
            }
        }

        language.isIranExclusive -> {
            val enabledHolidays = EventsRepository(this@saveLanguage, language, emptySet())
            if (enabledHolidays.isEmpty || enabledHolidays.onlyAfghanistanHolidaysIsEnabled) {
                putStringSet(PREF_HOLIDAY_TYPES, EventsRepository.iranDefault)
            }
        }

        language.isNepali -> putStringSet(PREF_HOLIDAY_TYPES, EventsRepository.nepalDefault)
    }

    // Calendars and week settings
    val defaultCalendars = language.defaultCalendars
    if (defaultCalendars.isNotEmpty()) putString(PREF_MAIN_CALENDAR_KEY, defaultCalendars[0].name)
    putString(
        PREF_OTHER_CALENDARS_KEY,
        defaultCalendars.drop(1).joinToString(",") { it.name }
    )
    putString(PREF_WEEK_START, language.defaultWeekStart)
    putStringSet(PREF_WEEK_ENDS, language.defaultWeekEnds)

    putString(PREF_PRAY_TIME_METHOD, language.preferredCalculationMethod.name)
    putBoolean(PREF_ASR_HANAFI_JURISTIC, language.isHanafiMajority)
}

// ------------------------
// Convenience utilities
// ------------------------

/** Clear all stored preferences (useful for debugging / factory reset). */
fun SharedPreferences.clearAllPreferences() = edit { clear() }

/** Check if a key exists in preferences */
fun SharedPreferences.containsKey(key: String): Boolean = contains(key)

/** Retrieve the last saved geocoded city name (if any) */
fun SharedPreferences.getSavedCityName(): String? = getString(PREF_GEOCODED_CITYNAME, null)

/** Retrieve saved coordinates (if both lat & lon are available) */
fun SharedPreferences.getSavedCoordinates(): Coordinates? {
    val lat = getString(PREF_LATITUDE, null)?.toDoubleOrNull()
    val lon = getString(PREF_LONGITUDE, null)?.toDoubleOrNull()
    val alt = getString(PREF_ALTITUDE, null)?.toDoubleOrNull() ?: 0.0
    return if (lat != null && lon != null) Coordinates(lat, lon, alt) else null
}

/** Reset language-related prefs to a provided default */
fun SharedPreferences.resetLanguageToDefault(defaultLanguage: Language) = edit {
    putString(PREF_APP_LANGUAGE, defaultLanguage.code)
    putBoolean(PREF_LOCAL_DIGITS, defaultLanguage.prefersLocalDigits)
}

/** Increase athan volume (coerced to maxVolume) */
fun SharedPreferences.increaseAthanVolume(step: Int = 1, maxVolume: Int = 10) = edit {
    val current = getInt(PREF_ATHAN_VOLUME, DEFAULT_ATHAN_VOLUME)
    putInt(PREF_ATHAN_VOLUME, (current + step).coerceAtMost(maxVolume))
}

/** Decrease athan volume (coerced to minVolume) */
fun SharedPreferences.decreaseAthanVolume(step: Int = 1, minVolume: Int = 0) = edit {
    val current = getInt(PREF_ATHAN_VOLUME, DEFAULT_ATHAN_VOLUME)
    putInt(PREF_ATHAN_VOLUME, (current - step).coerceAtLeast(minVolume))
}

// ------------------------
// Additional getters / setters
// ------------------------

/** Currently selected language code or null if not set */
fun SharedPreferences.getSavedLanguage(): String? = getString(PREF_APP_LANGUAGE, null)

/** Preferred week start (string key) */
fun SharedPreferences.getWeekStart(): String? = getString(PREF_WEEK_START, null)

/** Preferred week end days */
fun SharedPreferences.getWeekEnds(): Set<String>? = getStringSet(PREF_WEEK_ENDS, null)

/** Preferred main calendar key */
fun SharedPreferences.getMainCalendar(): String? = getString(PREF_MAIN_CALENDAR_KEY, null)

/** Other selected calendars as a list */
fun SharedPreferences.getOtherCalendars(): List<String> =
    getString(PREF_OTHER_CALENDARS_KEY, null)?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

/** Reset Athan volume to the project's default value */
fun SharedPreferences.resetAthanVolumeToDefault() = edit { putInt(PREF_ATHAN_VOLUME, DEFAULT_ATHAN_VOLUME) }

/** Save a custom Islamic offset set date (JDN) */
fun SharedPreferences.saveIslamicOffsetDate(jdn: Jdn) = edit { putJdn(PREF_ISLAMIC_OFFSET_SET_DATE, jdn) }

/** Remove any saved location information */
fun SharedPreferences.removeSavedLocation() = edit {
    remove(PREF_LATITUDE)
    remove(PREF_LONGITUDE)
    remove(PREF_ALTITUDE)
    remove(PREF_GEOCODED_CITYNAME)
    remove(PREF_SELECTED_LOCATION)
}
 
