package com.byagowi.persiancalendar

import android.content.ComponentName
import android.content.Context
import androidx.wear.tiles.TileService.getUpdater
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester

fun Context.requestComplicationUpdate() {
    val component = ComponentName(this, MainComplicationService::class.java)
    ComplicationDataSourceUpdateRequester.create(this, component).requestUpdateAll()
}

fun Context.requestTileUpdate() {
    val updater = getUpdater(this)
    updater.requestUpdate(MainTileService::class.java)
    updater.requestUpdate(MonthTileService::class.java)
}
