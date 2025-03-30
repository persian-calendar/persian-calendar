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
import java.util.concurrent.TimeUnit

class MainApplication : Application()

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

val complicationWeekdayInitial = booleanPreferencesKey("ComplicationWeekdayInitial")
val complicationMonthNumber = booleanPreferencesKey("ComplicationMonthNumber")
val enabledEventsKey = stringSetPreferencesKey("Events")

val Context.dataStore by preferencesDataStore(name = "user_prefs")
suspend fun Context.editPreferences(action: (MutablePreferences) -> Unit) = dataStore.edit(action)
