package com.byagowi.persiancalendar

import android.content.ComponentName
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
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

data class FixedColorProvider(val color: Color) : ColorProvider {
    override fun getColor(context: Context): Color = color
}

data class ResourceColorProvider(val resId: Int) : ColorProvider {
    override fun getColor(context: Context): Color {
        val androidColor = context.getColor(resId)
        return Color(androidColor)
    }
}
