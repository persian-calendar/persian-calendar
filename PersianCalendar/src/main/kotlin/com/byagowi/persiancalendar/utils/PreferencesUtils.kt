package com.byagowi.persiancalendar.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.byagowi.persiancalendar.DEFAULT_CITY
import com.byagowi.persiancalendar.DEFAULT_WEEK_ENDS
import com.byagowi.persiancalendar.DEFAULT_WEEK_START
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET_SET_DATE
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_PERSIAN_DIGITS
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.PREF_WEEK_ENDS
import com.byagowi.persiancalendar.PREF_WEEK_START
import com.byagowi.persiancalendar.entities.CityItem
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.generated.citiesStore
import io.github.persiancalendar.praytimes.Coordinate
import java.util.*

val Context.appPrefs: SharedPreferences get() = PreferenceManager.getDefaultSharedPreferences(this)

fun SharedPreferences.Editor.putJdn(key: String, jdn: Jdn?) {
    if (jdn == null) remove(jdn) else putLong(key, jdn.value)
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
    get() = getJdnOrNull(PREF_ISLAMIC_OFFSET_SET_DATE)?.let { Jdn.today - it > 30 } ?: true

fun SharedPreferences.saveCity(city: CityItem) = edit {
    listOf(PREF_GEOCODED_CITYNAME, PREF_LATITUDE, PREF_LONGITUDE, PREF_ALTITUDE).forEach(::remove)
    putString(PREF_SELECTED_LOCATION, city.key)
}

fun SharedPreferences.saveLocation(
    coordinates: Coordinate, cityName: String, countryCode: String = "IR"
) = edit {
    putString(PREF_LATITUDE, "%f".format(Locale.ENGLISH, coordinates.latitude))
    putString(PREF_LONGITUDE, "%f".format(Locale.ENGLISH, coordinates.longitude))
    // Don't store elevation on Iranian cities, it degrades the calculations quality
    val elevation = if (countryCode == "IR") .0 else coordinates.elevation
    putString(PREF_ALTITUDE, "%f".format(Locale.ENGLISH, elevation))
    putString(PREF_GEOCODED_CITYNAME, cityName)
    putString(PREF_SELECTED_LOCATION, DEFAULT_CITY)
}

// Preferences changes be applied automatically when user requests a language change
fun SharedPreferences.saveLanguage(language: Language) = edit {
    putString(PREF_APP_LANGUAGE, language.code)
    putBoolean(PREF_PERSIAN_DIGITS, language.prefersLocalDigits)

    when {
        language.isAfghanistanExclusive -> {
            val enabledHolidays = EnabledHolidays(this@saveLanguage, emptySet())
            if (enabledHolidays.isEmpty || enabledHolidays.onlyIranHolidaysIsEnabled)
                putStringSet(PREF_HOLIDAY_TYPES, EnabledHolidays.afghanistanDefault)
        }
        language.isIranExclusive -> {
            val enabledHolidays = EnabledHolidays(this@saveLanguage, emptySet())
            if (enabledHolidays.isEmpty || enabledHolidays.onlyAfghanistanHolidaysIsEnabled)
                putStringSet(PREF_HOLIDAY_TYPES, EnabledHolidays.iranDefault)
        }
    }

    when {
        language.prefersGregorianCalendar -> {
            putString(PREF_MAIN_CALENDAR_KEY, "GREGORIAN")
            putString(PREF_OTHER_CALENDARS_KEY, "ISLAMIC,SHAMSI")
            putString(PREF_WEEK_START, "1")
            putStringSet(PREF_WEEK_ENDS, setOf("1"))
        }
        language.prefersIslamicCalendar -> {
            putString(PREF_MAIN_CALENDAR_KEY, "ISLAMIC")
            putString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,SHAMSI")
            putString(PREF_WEEK_START, DEFAULT_WEEK_START)
            putStringSet(PREF_WEEK_ENDS, DEFAULT_WEEK_ENDS)
        }
        language.prefersPersianCalendar -> {
            putString(PREF_MAIN_CALENDAR_KEY, "SHAMSI")
            putString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,ISLAMIC")
            putString(PREF_WEEK_START, DEFAULT_WEEK_START)
            putStringSet(PREF_WEEK_ENDS, DEFAULT_WEEK_ENDS)
        }
    }
}
