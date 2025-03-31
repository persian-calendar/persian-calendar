package com.byagowi.persiancalendar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.byagowi.persiancalendar.complicationMonthNumber
import com.byagowi.persiancalendar.complicationWeekdayInitial
import com.byagowi.persiancalendar.editPreferences
import com.byagowi.persiancalendar.enabledEventsKey
import com.byagowi.persiancalendar.internationalKey
import com.byagowi.persiancalendar.iranNonHolidaysKey
import com.byagowi.persiancalendar.requestComplicationUpdate
import com.byagowi.persiancalendar.requestTileUpdate
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(preferences: Preferences?) {
    val scrollState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = scrollState) {
        val context = LocalContext.current
        val enabledEvents = preferences?.get(enabledEventsKey) ?: emptySet()
        val coroutineScope = rememberCoroutineScope()

        @Composable
        fun EventsSwitch(key: String, title: String) {
            SwitchButton(
                key in enabledEvents,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                label = { Text(title) },
                onCheckedChange = { value ->
                    coroutineScope.launch {
                        context.editPreferences {
                            context.requestTileUpdate()
                            it[enabledEventsKey] =
                                if (value) enabledEvents + key else enabledEvents - key
                        }
                    }
                },
            )
        }

        @Composable
        fun ComplicationSwitch(
            key: Preferences.Key<Boolean>,
            title: @Composable (Boolean) -> Unit,
        ) {
            val value = preferences?.get(key) ?: false
            SwitchButton(
                value,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                label = { title(value) },
                onCheckedChange = { newValue ->
                    context.requestComplicationUpdate()
                    coroutineScope.launch { context.editPreferences { it[key] = newValue } }
                },
            )
        }

        ScalingLazyColumn(state = scrollState) {
            item {
                ListSubHeader {
                    Text("نمایش رویدادها", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
            item { EventsSwitch(iranNonHolidaysKey, "غیرتعطیل رسمی\nدانشگاه تهران") }
            item { EventsSwitch(internationalKey, "بین‌المللی") }
            item {
                ListSubHeader {
                    Text("صفحهٔ ساعت", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
            item {
                ComplicationSwitch(complicationWeekdayInitial) { checked ->
                    Column {
                        Text("روز هفتهٔ کوتاه")
                        AnimatedContent(
                            if (checked) "چ" else "چهارشنبه",
                            transitionSpec = appCrossfadeSpec
                        ) { Text(it) }
                    }
                }
            }
            item {
                ComplicationSwitch(complicationMonthNumber) { checked ->
                    Column {
                        Text("نمایش عددی ماه")
                        AnimatedContent(
                            if (checked) "۱/۳۱" else "۳۱ فروردین",
                            transitionSpec = appCrossfadeSpec
                        ) { Text(it) }
                    }
                }
            }
        }
    }
}

/** This is similar to what [androidx.compose.animation.Crossfade] uses */
private val crossfadeSpec = fadeIn(tween()) togetherWith fadeOut(tween())

// Our own cross fade spec where AnimatedContent() has nicer effect
// than Crossfade() (usually on non binary changes) but we need a crossfade effect also
val appCrossfadeSpec: AnimatedContentTransitionScope<*>.() -> ContentTransform =
    { crossfadeSpec }

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun SettingsPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AppScaffold { SettingsScreen(null) }
    }
}
