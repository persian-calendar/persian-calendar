package com.byagowi.persiancalendar

import android.content.ComponentName
import android.content.Context
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.types.LayoutColor
import androidx.wear.tiles.TileService.getUpdater
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester

fun Context.requestComplicationsUpdate() {
    listOf(
        MainComplicationService::class.java,
        MonthComplicationService::class.java,
    ).forEach {
        val component = ComponentName(this, it)
        ComplicationDataSourceUpdateRequester.create(this, component)
            .requestUpdateAll()
    }
}

fun Context.requestTileUpdate() {
    val updater = getUpdater(this)
    updater.requestUpdate(MainTileService::class.java)
    updater.requestUpdate(MonthTileService::class.java)
}

/** There is also [androidx.wear.protolayout.types.LayoutColor.prop] but it's library restricted for whatever reason*/
val LayoutColor.colorProp get() = ColorBuilders.ColorProp.Builder(staticArgb).build()
