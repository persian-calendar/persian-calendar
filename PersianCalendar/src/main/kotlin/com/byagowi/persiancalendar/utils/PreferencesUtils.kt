package com.byagowi.persiancalendar.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.byagowi.persiancalendar.DEFAULT_CITY
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET_SET_DATE
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.entities.CityItem
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.generated.citiesStore

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
