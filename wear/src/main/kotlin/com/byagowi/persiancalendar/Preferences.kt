package com.byagowi.persiancalendar

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val complicationWeekdayInitial = booleanPreferencesKey("ComplicationWeekdayInitial")
val complicationMonthNumber = booleanPreferencesKey("ComplicationMonthNumber")
val enabledEventsKey = stringSetPreferencesKey("Events")

val Context.dataStore by preferencesDataStore(name = "user_prefs")
suspend fun Context.editPreferences(action: (MutablePreferences) -> Unit) = dataStore.edit(action)
