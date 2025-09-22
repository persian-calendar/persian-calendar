package com.byagowi.persiancalendar

import android.content.ComponentName
import android.os.Build
import android.util.TypedValue
import androidx.wear.protolayout.ActionBuilders.launchAction
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.wrap
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.layout.basicText
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.modifiers.padding
import androidx.wear.protolayout.types.LayoutColor
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.byagowi.persiancalendar.ui.MainActivity
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import io.github.persiancalendar.calendar.PersianDate
import kotlin.math.ceil
import kotlin.math.min

class MonthTileService : TileService() {
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
        root.addContent(curvedText(localeUtils.format(today.toCivilDate()), 45f))
        root.addContent(curvedText(localeUtils.format(today.toIslamicDate()), -45f))

        val persianDate = today.toPersianDate()
        val month = localeUtils.persianMonth(persianDate)
        root.addContent(
            primaryLayout(
                titleSlot = {
                    text(
                        (month + " " + localeUtils.format(persianDate.year)).layoutString,
                        typography = Typography.TITLE_LARGE,
                    )
                },
                mainSlot = { LayoutElementBuilders.Box.Builder().build() },
            )
        )
        val activityComponent = ComponentName(applicationContext, MainActivity::class.java)
        root.addContent(
            LayoutElementBuilders.Box.Builder()
                .setWidth(expand())
                .setHeight(expand())
                .setModifiers(
                    ModifiersBuilders.Modifiers.Builder()
                        .setClickable(clickable(launchAction(activityComponent)))
                        .setPadding(padding(start = 32f, end = 32f, top = 36f))
                        .build()
                )
                .addContent(calendarTable(today, persianDate, localeUtils))
                .build()
        )
        root.build()
    }

    private fun LayoutColor.colorProp() = ColorBuilders.ColorProp.Builder(staticArgb).build()

    private fun MaterialScope.curvedText(text: String, angle: Float): LayoutElementBuilders.Arc {
        val arcText = LayoutElementBuilders.ArcText.Builder()
            .setText(text)
            .setFontStyle(
                LayoutElementBuilders.FontStyle.Builder()
                    .setColor(colorScheme.onSurface.colorProp())
                    .setSize(DimensionBuilders.SpProp.Builder().setValue(12f).build())
                    .build()
            )
            .build()

        return LayoutElementBuilders.Arc.Builder()
            .setAnchorAngle(DimensionBuilders.DegreesProp.Builder(angle).build())
            .addContent(arcText)
            .build()
    }

    private fun MaterialScope.calendarTable(
        today: Jdn,
        persianDate: PersianDate,
        localeUtils: LocaleUtils,
    ): LayoutElementBuilders.Column {
        val column = LayoutElementBuilders.Column.Builder()
            .setWidth(expand())
            .setHeight(wrap())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
        val screenMinDp = resources.configuration.run { min(screenHeightDp, screenWidthDp) }
        val monthStartJdn = Jdn(PersianDate(persianDate.year, persianDate.month, 1))
        val monthEndJdn = Jdn(persianDate.monthStartOfMonthsDistance(1))
        val monthLength = monthEndJdn - monthStartJdn
        val startingDay = ((monthStartJdn.value + 2) % 7).toInt()
        repeat(1 + ceil((monthLength + startingDay) / 7f).toInt()) { y ->
            val row = LayoutElementBuilders.Row.Builder()
                .setWidth(expand())
                .setHeight(wrap())
                .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            row.setModifiers(
                ModifiersBuilders.Modifiers.Builder().also {
                    if (y == 0) it.setBackground(
                        ModifiersBuilders.Background.Builder()
                            .setColor(colorScheme.secondaryContainer.colorProp())
                            .setCorner(shapes.full).build()
                    )
                    it.setPadding(padding(horizontal = 4f, vertical = 0f))
                }.build()
            )
            val cellFontSize = dpToSp(screenMinDp / (if (y == 0) 17f else 19.5f))
            repeat(7) { x ->
                row.addContent(
                    tableCell(
                        cellFontSize,
                        x,
                        y,
                        localeUtils,
                        today,
                        startingDay,
                        monthLength,
                        monthStartJdn,
                        screenMinDp,
                    )
                )
            }
            column.addContent(row.build())
        }
        return column.build()
    }

    private fun MaterialScope.tableCell(
        cellFontSize: Float,
        x: Int,
        y: Int,
        localeUtils: LocaleUtils,
        today: Jdn,
        startingDay: Int,
        monthLength: Int,
        monthStartJdn: Jdn,
        screenMinDp: Int,
    ): LayoutElementBuilders.Box {
        val day = 7 - x + (y - 1) * 7 - 1 - startingDay
        val text = when {
            y == 0 -> localeUtils.narrowWeekdays[((7 - x) + 5) % 7 + 1]
            day < 0 -> ""
            day < monthLength -> localeUtils.format(day + 1)
            else -> ""
        }
        val jdn = monthStartJdn + day
        val isHoliday = x == 0 || getEventsOfDay(
            enabledEvents = emptySet(),
            civilDate = jdn.toCivilDate(),
        ).any { it.type == EntryType.Holiday }
        val isToday = jdn == today

        return LayoutElementBuilders.Box.Builder()
            .setWidth(expand())
            .setHeight(wrap())
            .addContent(run {
                val element = basicText(
                    text.layoutString,
                    LayoutElementBuilders.FontStyle.Builder()
                        .setSize(
                            DimensionBuilders.SpProp.Builder()
                                .setValue(cellFontSize)
                                .build()
                        )
                        .setColor(
                            when {
                                y == 0 -> colorScheme.onSecondaryContainer
                                isToday -> colorScheme.onPrimary
                                isHoliday -> colorScheme.primary
                                else -> colorScheme.onBackground
                            }.colorProp()
                        )
                        .build(),
                )
                if (isToday) LayoutElementBuilders.Box.Builder()
                    .setModifiers(
                        ModifiersBuilders.Modifiers.Builder()
                            .setBackground(
                                ModifiersBuilders.Background.Builder()
                                    .setColor(colorScheme.primary.colorProp())
                                    .setCorner(shapes.full)
                                    .build()
                            )
                            .build()
                    )
                    .setWidth(DimensionBuilders.dp(screenMinDp / 12f))
                    .setHeight(DimensionBuilders.dp(screenMinDp / 12f))
                    .addContent(element)
                    .build()
                else element
            })
            .build()
    }

    private fun dpToSp(dp: Float): Float {
        val displayMetrics = resources.displayMetrics
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            TypedValue.convertPixelsToDimension(TypedValue.COMPLEX_UNIT_SP, px, displayMetrics)
        } else @Suppress("DEPRECATION") {
            px / displayMetrics.scaledDensity
        }
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
