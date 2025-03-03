package com.byagowi.persiancalendar

import androidx.compose.runtime.Composable
import androidx.glance.GlanceComposable
import androidx.glance.text.Text
import androidx.glance.wear.tiles.GlanceTileService

class MainTileService : GlanceTileService() {
    @Composable
    @GlanceComposable
    override fun Content() {
        Text("Hello Worlsd!")
    }
}
