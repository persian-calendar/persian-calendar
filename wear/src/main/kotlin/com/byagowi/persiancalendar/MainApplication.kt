package com.byagowi.persiancalendar

import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.wear.tiles.TileService.getUpdater
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        enqueueUpdateWorker()
    }
}

class BroadcastReceivers : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Nothing right now
            let {}
        }
    }
}

fun Context.enqueueUpdateWorker() {
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "PeriodicUpdateWork",
        ExistingPeriodicWorkPolicy.KEEP,
        PeriodicWorkRequestBuilder<UpdateWorker>(15.minutes.toJavaDuration()).build()
    )
}

fun Context.requestComplicationUpdate() {
    val component = ComponentName(this, MainComplicationService::class.java)
    ComplicationDataSourceUpdateRequester.create(this, component).requestUpdateAll()
}

fun Context.requestTileUpdate() {
    getUpdater(this).requestUpdate(MainTileService::class.java)
}

val complicationWeekdayInitial = booleanPreferencesKey("ComplicationWeekdayInitial")
val complicationMonthNumber = booleanPreferencesKey("ComplicationMonthNumber")
val enabledEventsKey = stringSetPreferencesKey("Events")

val Context.dataStore by preferencesDataStore(name = "user_prefs")
suspend fun Context.editPreferences(action: (MutablePreferences) -> Unit) = dataStore.edit(action)
