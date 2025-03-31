package com.byagowi.persiancalendar

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceComposable
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.wear.tiles.GlanceTileService
import androidx.wear.tiles.EventBuilders
import com.byagowi.persiancalendar.ui.MainActivity
import io.github.persiancalendar.calendar.PersianDate

class MonthTileService : GlanceTileService() {

    override fun onDestroy() {
        runCatching { super.onDestroy() }.onFailure {
            Log.e("PC", "prevent a crash", it)
        }
    }

    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        super.onTileEnterEvent(requestParams)
        requestTileUpdate()
    }

    @Composable
    @GlanceComposable
    override fun Content() {
        Box(contentAlignment = Alignment.Center) {
            // LocalConfiguration doesn't work here
            val configuration = resources.configuration
            val screenHeightDp = configuration.screenHeightDp
            val localeUtils = LocaleUtils()
            val today = Jdn.today()
            val persianDate = today.toPersianDate()
            val monthStartJdn = Jdn(PersianDate(persianDate.year, persianDate.month, 1))
            val monthEndJdn = Jdn(persianDate.monthStartOfMonthsDistance(1))
            val monthLength = monthEndJdn - monthStartJdn
            val startingDay = ((monthStartJdn.value + 2) % 7).toInt()

            Text(
                localeUtils.persianMonth(persianDate) + " " + localeUtils.format(persianDate.year),
                modifier = GlanceModifier.padding(bottom = (screenHeightDp / 1.7).dp),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            )
            Column(GlanceModifier.clickable(actionStartActivity(MainActivity::class.java))) {
                Spacer(GlanceModifier.height(24.dp))
                repeat(7) { y ->
                    Row {
                        repeat(7) { x ->
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
                            Box(contentAlignment = Alignment.Center) {
                                if (jdn == today) Image(
                                    provider = ImageProvider(R.drawable.month_today_indicator),
                                    contentDescription = "امروز",
                                    modifier = GlanceModifier.size(18.dp),
                                )
                                Text(
                                    text,
                                    style = if (isHoliday || y == 0) TextStyle(
                                        textAlign = TextAlign.Center,
                                        color = ColorProvider(
                                            if (y == 0) R.color.month_tile_weekdays
                                            else R.color.month_tile_holidays
                                        ),
                                    ) else null,
                                    modifier = GlanceModifier.size(
                                        width = 24.dp,
                                        height = if (y == 0) 22.dp else 18.dp,
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
