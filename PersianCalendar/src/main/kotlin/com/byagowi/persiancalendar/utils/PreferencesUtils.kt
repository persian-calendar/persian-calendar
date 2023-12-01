package com.byagowi.persiancalendar.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.preferencesDataStore
import com.byagowi.persiancalendar.DEFAULT_CITY
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_ASR_HANAFI_JURISTIC
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
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.overrideCoordinatesGlobalVariable
import io.github.persiancalendar.praytimes.Coordinates
import java.util.Locale

// Instead of:
//   androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
// Per https://stackoverflow.com/a/62897591
val Context.appPrefs: SharedPreferences
    get() = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)

//val Context.dataStore by preferencesDataStore(
//    name = "preferences",
//    produceMigrations = { context ->
//        listOf(SharedPreferencesMigration(context, "${context.packageName}_preferences"))
//    }
//)

fun SharedPreferences.Editor.putJdn(key: String, jdn: Jdn) {
    putLong(key, jdn.value)
}

fun SharedPreferences.getJdnOrNull(key: String): Jdn? =
    getLong(key, -1).takeIf { it != -1L }?.let { Jdn(it) }

val SharedPreferences.storedCity: CityItem?
    get() = getString(PREF_SELECTED_LOCATION, null)
        ?.takeIf { it.isNotEmpty() && it != DEFAULT_CITY }?.let { citiesStore[it] }

val SharedPreferences.cityName: String?
    get() = this.storedCity?.let(language::getCityName)
        ?: this.getString(PREF_GEOCODED_CITYNAME, null)?.takeIf { it.isNotEmpty() }

// Ignore offset if it isn't set in less than month ago
val SharedPreferences.isIslamicOffsetExpired
    get() = getJdnOrNull(PREF_ISLAMIC_OFFSET_SET_DATE)?.let { Jdn.today() - it > 30 } ?: true

fun SharedPreferences.saveCity(city: CityItem) = edit {
    listOf(PREF_GEOCODED_CITYNAME, PREF_LATITUDE, PREF_LONGITUDE, PREF_ALTITUDE).forEach(::remove)
    putString(PREF_SELECTED_LOCATION, city.key)
}

fun SharedPreferences.saveLocation(
    coordinates: Coordinates, cityName: String, countryCode: String = "IR"
) {
    edit {
        putString(PREF_LATITUDE, "%f".format(Locale.ENGLISH, coordinates.latitude))
        putString(PREF_LONGITUDE, "%f".format(Locale.ENGLISH, coordinates.longitude))
        // Don't store elevation on Iranian cities, it degrades the calculations quality
        val elevation = if (countryCode == "IR") .0 else coordinates.elevation
        putString(PREF_ALTITUDE, "%f".format(Locale.ENGLISH, elevation))
        putString(PREF_GEOCODED_CITYNAME, cityName)
        putString(PREF_SELECTED_LOCATION, DEFAULT_CITY)
    }
    overrideCoordinatesGlobalVariable(coordinates)
}

// Preferences changes be applied automatically when user requests a language change
fun SharedPreferences.saveLanguage(language: Language) = edit {
    putString(PREF_APP_LANGUAGE, language.code)
    putBoolean(PREF_LOCAL_DIGITS, language.prefersLocalDigits)

    when {
        language.isAfghanistanExclusive -> {
            val enabledHolidays = EventsRepository(this@saveLanguage, language, emptySet())
            if (enabledHolidays.isEmpty || enabledHolidays.onlyIranHolidaysIsEnabled)
                putStringSet(PREF_HOLIDAY_TYPES, EventsRepository.afghanistanDefault)
        }

        language.isIranExclusive -> {
            val enabledHolidays = EventsRepository(this@saveLanguage, language, emptySet())
            if (enabledHolidays.isEmpty || enabledHolidays.onlyAfghanistanHolidaysIsEnabled)
                putStringSet(PREF_HOLIDAY_TYPES, EventsRepository.iranDefault)
        }

        language.isNepali -> {
            putStringSet(PREF_HOLIDAY_TYPES, EventsRepository.nepalDefault)
        }
    }

    putString(PREF_MAIN_CALENDAR_KEY, language.defaultMainCalendar)
    putString(PREF_OTHER_CALENDARS_KEY, language.defaultOtherCalendars)
    putString(PREF_WEEK_START, language.defaultWeekStart)
    putStringSet(PREF_WEEK_ENDS, language.defaultWeekEnds)

    putString(PREF_PRAY_TIME_METHOD, language.preferredCalculationMethod.name)
    putBoolean(PREF_ASR_HANAFI_JURISTIC, language.isHanafiMajority)
}
