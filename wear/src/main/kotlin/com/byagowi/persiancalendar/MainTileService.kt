package com.byagowi.persiancalendar

import android.content.ComponentName
import androidx.wear.protolayout.ActionBuilders.launchAction
import androidx.wear.protolayout.DimensionBuilders.degrees
import androidx.wear.protolayout.DimensionBuilders.dp
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
import com.google.common.util.concurrent.ListenableFuture
import io.github.persiancalendar.calendar.PersianDate
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
        val today = Jdn.today()
        val root = LayoutElementBuilders.Box.Builder()
        root.addContent(
            LayoutElementBuilders.Arc.Builder()
                .addContent(
                    LayoutElementBuilders.ArcLine.Builder()
                        .setLength(degrees(360f))
                        .setThickness(dp(2f))
                        .setColor(colorScheme.primaryContainer.colorProp)
                        .build()
                )
                .build()
        )
        val sweepAngle = run {
            val date = today.toPersianDate()
            val monthStartJdn = Jdn(PersianDate(date.year, date.month, 1))
            val monthEndJdn = Jdn(date.monthStartOfMonthsDistance(1))
            val monthLength = monthEndJdn - monthStartJdn
            360f * date.dayOfMonth / monthLength
        }
        root.addContent(
            LayoutElementBuilders.Arc.Builder()
                .setAnchorAngle(degrees(sweepAngle / 2))
                .addContent(
                    LayoutElementBuilders.ArcLine.Builder()
                        .setLength(degrees(sweepAngle))
                        .setThickness(dp(2f))
                        .setColor(colorScheme.primaryDim.colorProp)
                        .build()
                )
                .build()
        )

        val todayEntries = run {
            // Doesn't worth to use coroutine and stuff just to retrieve this I think
            val preferences = runBlocking { dataStore.data.firstOrNull() }
            val enabledEvents = preferences?.get(enabledEventsKey) ?: emptySet()
            generateEntries(localeUtils, today, enabledEvents, 1, false)
        }
        root.addContent(
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
                    todayEntries.drop(1).take(if (todayEntries.size > 4) 2 else 3).map {
                        if (it.type is EntryType.Holiday) text(
                            it.title.layoutString, color = colorScheme.primary
                        ) else text(it.title.layoutString)
                    }.forEach(column::addContent)
                    if (todayEntries.size > 4) column.addContent(text("…".layoutString))
                    column.build()
                },
                bottomSlot = {
                    val activityComponent =
                        ComponentName(applicationContext, MainActivity::class.java)
                    textEdgeButton(
                        onClick = clickable(launchAction(activityComponent)),
                    ) { text("تقویم".layoutString, typography = Typography.BODY_SMALL) }
                },
            )
        )
        root.build()
    }

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        return ImmediateFuture(
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
        return ImmediateFuture(
            ResourceBuilders.Resources.Builder()
                .setVersion(RESOURCES_VERSION)
                .build()
        )
    }

    companion object {
        private const val RESOURCES_VERSION = "1"
    }
}
