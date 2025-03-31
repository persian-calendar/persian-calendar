package com.byagowi.persiancalendar

import android.os.Build
import android.util.Log
import android.util.TypedValue
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
import kotlin.math.min

class MonthTileService : GlanceTileService() {

    override fun onDestroy() {
        runCatching { super.onDestroy() }.onFailure {
            Log.e("PC", "prevent a crash", it)
        }
    }

    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        super.onTileEnterEvent(requestParams)
        getUpdater(this).requestUpdate(MonthTileService::class.java)
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

    @Composable
    @GlanceComposable
    override fun Content() {
        Box(contentAlignment = Alignment.Center) {
            // LocalConfiguration doesn't work here
            val configuration = resources.configuration
            val screenHeightDp = configuration.screenHeightDp
            val screenMinDp = min(
                configuration.screenHeightDp,
                configuration.screenWidthDp,
            )
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
                                    modifier = GlanceModifier.size((screenMinDp / 12.5).dp),
                                )
                                Text(
                                    text,
                                    style = TextStyle(
                                        textAlign = TextAlign.Center,
                                        color = if (isHoliday || y == 0) ColorProvider(
                                            resId = when {
                                                y == 0 -> R.color.month_tile_weekdays
                                                jdn == today -> R.color.tile_on_button_color
                                                else -> R.color.month_tile_holidays
                                            }
                                        ) else null,
                                        fontSize = dpToSp(
                                            screenMinDp / (if (y == 0) 13f else 12f)
                                        ).sp,
                                    ),
                                    modifier = GlanceModifier.size(
                                        width = (screenMinDp / 9.5).dp,
                                        height = if (y == 0) (screenMinDp / 10.3).dp
                                        else (screenMinDp / 12.5).dp,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
