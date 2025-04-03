package com.byagowi.persiancalendar

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.wear.tiles.GlanceTileService
import androidx.wear.tiles.EventBuilders
import com.byagowi.persiancalendar.ui.MainActivity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class MainTileService : GlanceTileService() {

    override fun onDestroy() {
        runCatching { super.onDestroy() }.onFailure { Log.e("", "", it) }
    }

    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        super.onTileEnterEvent(requestParams)
        getUpdater(this).requestUpdate(MainTileService::class.java)
    }

    @Composable
    @GlanceComposable
    override fun Content() {
        Box(contentAlignment = Alignment.Center) {
            // LocalConfiguration doesn't work here
            val screenHeightDp = resources.configuration.screenHeightDp
            val localeUtils = LocaleUtils()
            val todayEntries = run {
                val preferences = runBlocking { dataStore.data.firstOrNull() }
                val enabledEvents = preferences?.get(enabledEventsKey) ?: emptySet()
                generateEntries(localeUtils, Jdn.today(), enabledEvents, 1, false)
            }
            Text(
                todayEntries[0].title,
                GlanceModifier.padding(bottom = (screenHeightDp / 1.7).dp),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                todayEntries.drop(1).take(if (todayEntries.size > 4) 2 else 3).forEach {
                    Text(
                        it.title,
                        modifier = GlanceModifier.padding(vertical = 2.dp, horizontal = 8.dp),
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = if (it.type == EntryType.Holiday) {
                                ColorProvider(Color(0xffafcbfa))
                            } else null
                        ),
                        maxLines = 1,
                    )
                }
                if (todayEntries.size > 4) Text("…", style = TextStyle(fontSize = 16.sp))
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = GlanceModifier.padding(top = (screenHeightDp / 1.35).dp),
            ) {
                Image(
                    provider = ImageProvider(R.drawable.button),
                    contentDescription = "تقویم",
                    modifier = GlanceModifier
                        .size(56.dp, 32.dp)
                        .clickable(actionStartActivity<MainActivity>())
                )
                Text(
                    "تقویم",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = ColorProvider(R.color.tile_on_button_color)
                    )
                )
            }
        }
    }
}
