package com.byagowi.persiancalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WearApp() }
    }
}

@Composable
private fun WearApp() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme {
            AppScaffold {
                val navController = rememberSwipeDismissableNavController()
                val mainRoute = "app"
                val settingsRoute = "settings"
                SwipeDismissableNavHost(
                    navController = navController,
                    startDestination = mainRoute
                ) {
                    composable(mainRoute) {
                        MainScreen(navigateToSettings = { navController.navigate(settingsRoute) })
                    }
                    composable(settingsRoute) { SettingsScreen() }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen() {
    ScreenScaffold {
        ScalingLazyColumn {
            item {
                Button(
                    onClick = {},
                    label = {
                        Row {
                            Text("Item ${1 + 1}")
                            Switch(true)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun MainScreen(navigateToSettings: () -> Unit) {
    val scrollState = rememberScalingLazyListState()
    ScreenScaffold(
        scrollState = scrollState,
//        edgeButton = {
//            EdgeButton(
//                onClick = navigateToSettings,
//                buttonSize = EdgeButtonSize.Medium,
//            ) {
//                Icon(
//                    Icons.Default.Settings,
//                    contentDescription = "تنظیمات",
//                    tint = MaterialTheme.colors.onPrimary
//                )
//            }
//        },
    ) {
        ScalingLazyColumn(Modifier.fillMaxWidth(), state = scrollState) {
            items(items = generateEntries(days = 31)) { EntryView(it) }
        }
    }
}

@Composable
private fun EntryView(it: Entry) {
    if (it.type == EntryType.Date) Text(
        text = it.title,
        style = MaterialTheme.typography.caption1,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    ) else {
        val isHoliday = it.type == EntryType.Holiday
        Text(
            it.title,
            color = if (isHoliday) MaterialTheme.colors.onPrimary
            else MaterialTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 4.dp, start = 8.dp, end = 8.dp)
                .background(
                    if (isHoliday) MaterialTheme.colors.primary
                    else MaterialTheme.colors.surface,
                    RoundedCornerShape(24.dp),
                )
                .fillMaxWidth()
                .padding(12.dp)
        )
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun MainPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AppScaffold { MainScreen {} }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun SettingsPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AppScaffold { SettingsScreen() }
    }
}
