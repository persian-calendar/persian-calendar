package com.byagowi.persiancalendar;

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val dataStore = dataStore
        val context = this
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            dataStore.data.collect {
                preferences_.value = it
                val component = ComponentName(context, MainComplicationService::class.java)
                ComplicationDataSourceUpdateRequester.create(context, component).requestUpdateAll()
            }
        }
    }
}

private val preferences_ = MutableStateFlow<Preferences?>(null)
val preferences: StateFlow<Preferences?> get() = preferences_

val complicationWeekdayInitial = booleanPreferencesKey("ComplicationWeekdayInitial")
val complicationMonthNumber = booleanPreferencesKey("ComplicationMonthNumber")
val enabledEventsKey = stringSetPreferencesKey("Events")

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")
suspend fun Context.editPreferences(action: (MutablePreferences) -> Unit) = dataStore.edit(action)
