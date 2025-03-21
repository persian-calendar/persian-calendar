package com.byagowi.persiancalendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import kotlinx.coroutines.launch

/** This is similar to what [androidx.compose.animation.Crossfade] uses */
private val crossfadeSpec = fadeIn(tween()) togetherWith fadeOut(tween())

@Composable
fun SettingsScreen() {
    ScreenScaffold {
        val preferences by preferences.collectAsState()
        val enabledEvents = preferences?.get(enabledEventsKey) ?: emptySet()
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current

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
                            it[enabledEventsKey] =
                                if (value) enabledEvents + key else enabledEvents - key
                        }
                    }
                },
            )
        }

        @Composable
        fun PreferenceSwitch(key: Preferences.Key<Boolean>, title: @Composable (Boolean) -> Unit) {
            val value = preferences?.get(key) ?: false
            SwitchButton(
                value,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                label = { title(value) },
                onCheckedChange = { newValue ->
                    coroutineScope.launch { context.editPreferences { it[key] = newValue } }
                },
            )
        }

        ScalingLazyColumn {
            item { Text("نمایش رویدادها") }
            item { EventsSwitch(iranNonHolidaysKey, "غیرتعطیل رسمی\nدانشگاه تهران") }
            item { EventsSwitch(internationalKey, "بین‌المللی") }
            item { Text("صفحهٔ ساعت", modifier = Modifier.padding(top = 12.dp)) }
            item {
                PreferenceSwitch(complicationWeekdayInitial) { checked ->
                    Column {
                        Text("روز هفتهٔ کوتاه")
                        AnimatedContent(
                            if (checked) "چ" else "چهارشنبه",
                            transitionSpec = { crossfadeSpec }
                        ) { Text(it) }
                    }
                }
            }
            item {
                PreferenceSwitch(complicationMonthNumber) { checked ->
                    Column {
                        Text("نمایش عددی ماه")
                        AnimatedContent(
                            if (checked) "۱/۳۱" else "۳۱ فروردین",
                            transitionSpec = { crossfadeSpec }
                        ) { Text(it) }
                    }
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun SettingsPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AppScaffold { SettingsScreen() }
    }
}
