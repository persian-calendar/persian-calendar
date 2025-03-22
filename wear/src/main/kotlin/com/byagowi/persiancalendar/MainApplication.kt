package com.byagowi.persiancalendar

import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.wear.tiles.TileService.getUpdater
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val dataStore = dataStore
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            dataStore.data.collect {
                preferences_.value = it
                requestComplicationUpdate()
            }
        }
    }
}

class BroadcastReceivers : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.applicationContext.run {
                requestTileUpdate()
                requestComplicationUpdate()
                enqueueUpdateWorker()
            }
        }
    }
}

fun Context.enqueueUpdateWorker() {
    val workRequest = PeriodicWorkRequestBuilder<UpdateWorker>(15, TimeUnit.MINUTES).build()
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "PeriodicUpdateWork",
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}

fun Context.requestComplicationUpdate() {
    val component = ComponentName(this, MainComplicationService::class.java)
    ComplicationDataSourceUpdateRequester.create(this, component).requestUpdateAll()
}

fun Context.requestTileUpdate() {
    getUpdater(this).requestUpdate(MainTileService::class.java)
}

private val preferences_ = MutableStateFlow<Preferences?>(null)
val preferences: StateFlow<Preferences?> get() = preferences_

val complicationWeekdayInitial = booleanPreferencesKey("ComplicationWeekdayInitial")
val complicationMonthNumber = booleanPreferencesKey("ComplicationMonthNumber")
val enabledEventsKey = stringSetPreferencesKey("Events")

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")
suspend fun Context.editPreferences(action: (MutablePreferences) -> Unit) = dataStore.edit(action)
