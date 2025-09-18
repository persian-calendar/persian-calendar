package com.byagowi.persiancalendar

import android.content.Intent
import androidx.wear.protolayout.ActionBuilders.launchAction
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.wrap
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.byagowi.persiancalendar.ui.MainActivity
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class MainTileService : TileService() {
    override fun onRecentInteractionEventsAsync(
        events: MutableList<EventBuilders.TileInteractionEvent?>
    ): ListenableFuture<Void?> {
        if (events.any { it?.eventType == EventBuilders.TileInteractionEvent.ENTER }) {
            getUpdater(this).requestUpdate(MainTileService::class.java)
        }
        return super.onRecentInteractionEventsAsync(events)
    }

    private fun tileLayout(requestParams: RequestBuilders.TileRequest) = materialScope(
        applicationContext,
        deviceConfiguration = requestParams.deviceConfiguration,
    ) {
        val localeUtils = LocaleUtils()
        val todayEntries = run {
            val preferences = runBlocking { dataStore.data.firstOrNull() }
            val enabledEvents = preferences?.get(enabledEventsKey) ?: emptySet()
            generateEntries(localeUtils, Jdn.today(), enabledEvents, 1, false)
        }
        primaryLayout(
            titleSlot = {
                text(
                    text = todayEntries[0].title.layoutString,
                    typography = Typography.TITLE_SMALL,
                )
            },
            mainSlot = {
                val column = LayoutElementBuilders.Column.Builder()
                    .setWidth(expand())
                    .setHeight(wrap())
                    .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                todayEntries.drop(1).take(if (todayEntries.size > 4) 2 else 3).forEach {
                    column.addContent(
                        if (it.type == EntryType.Holiday) {
                            text(it.title.layoutString, color = colorScheme.primary)
                        } else text(it.title.layoutString)
                    )
                }
                if (todayEntries.size > 4) column.addContent(text("…".layoutString))
                column.build()
            },
            bottomSlot = {
                Intent(
                    applicationContext,
                    MainActivity::class.java
                ).component?.let {
                    textEdgeButton(
                        onClick = clickable(launchAction(it))
                    ) { text("تقویم".layoutString, typography = Typography.BODY_SMALL) }
                } ?: text("".layoutString)
            }
        )
    }

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        return Futures.immediateFuture(
            TileBuilders.Tile.Builder()
                .setResourcesVersion(RESOURCES_VERSION)
                .setTileTimeline(
                    TimelineBuilders.Timeline.fromLayoutElement(tileLayout(requestParams))
                )
                .build()
        )
    }

    override fun onTileResourcesRequest(requestParams: RequestBuilders.ResourcesRequest)
            : ListenableFuture<ResourceBuilders.Resources> {
        return Futures.immediateFuture(
            ResourceBuilders.Resources.Builder()
                .setVersion(RESOURCES_VERSION)
                .build()
        )
    }

    companion object {
        private const val RESOURCES_VERSION = "1"
    }
}
