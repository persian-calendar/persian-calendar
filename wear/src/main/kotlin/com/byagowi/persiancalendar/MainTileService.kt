package com.byagowi.persiancalendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonColors
import androidx.glance.GlanceComposable
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.wear.tiles.GlanceTileService

class MainTileService : GlanceTileService() {
    @Composable
    @GlanceComposable
    override fun Content() {
        Box(contentAlignment = Alignment.Center) {
            val todayEntries = generateEntries(1)
            Text(
                todayEntries[0].title,
                GlanceModifier.padding(bottom = 132.dp),
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
                modifier = GlanceModifier.padding(top = 172.dp),
            ) {
                Button(
                    "تقویم",
                    actionStartActivity<MainActivity>(),
                    style = TextStyle(fontSize = 14.sp),
                    modifier = GlanceModifier.padding(
                        start = 10.dp,
                        end = 10.dp,
                        top = 4.dp,
                        bottom = 6.dp,
                    ),
                    colors = ButtonColors(
                        ColorProvider(Color(0xffafcbfa)),
                        ColorProvider(Color(0xff303133)),
                    )
                )
            }
        }
    }
}
