package com.byagowi.persiancalendar.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.dynamicColorScheme
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.byagowi.persiancalendar.Jdn
import com.byagowi.persiancalendar.LocaleUtils
import com.byagowi.persiancalendar.dataStore
import com.byagowi.persiancalendar.requestComplicationUpdate
import com.byagowi.persiancalendar.requestTileUpdate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        run {
            // Request update of both on activity just in case
            requestComplicationUpdate()
            requestTileUpdate()
        }
        setContent { WearApp() }
    }
}

@Composable
private fun WearApp() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = dynamicColorScheme(LocalContext.current) ?: MaterialTheme.colorScheme
        ) {
            AppScaffold {
                val context = LocalContext.current
                val dataStore = context.dataStore.data
                val preferences by dataStore.collectAsState(
                    remember { runBlocking { dataStore.firstOrNull() } }
                )
                val navController = rememberSwipeDismissableNavController()
                val mainRoute = "app"
                val settingsRoute = "settings"
                val utilitiesRoute = "utilities"
                val converterRoute = "converter"
                val calendarRoute = "calendar"
                val dayRoute = "day"
                val dayJdnKey = "dayJdnKey"
                val localeUtils = LocaleUtils()
                val today = updatedToday()
                SwipeDismissableNavHost(
                    navController = navController,
                    startDestination = mainRoute,
                ) {
                    composable(mainRoute) {
                        MainScreen(
                            localeUtils = localeUtils,
                            today = today,
                            preferences = preferences,
                            navigateToUtilities = { navController.navigate(utilitiesRoute) },
                            navigateToDay = { jdn ->
                                navController.graph.findNode(dayRoute)?.let { destination ->
                                    navController.navigate(
                                        destination.id, bundleOf(dayJdnKey to jdn.value)
                                    )
                                }
                            }
                        )
                    }
                    composable(utilitiesRoute) {
                        UtilitiesScreen(
                            navigateToConverter = { navController.navigate(converterRoute) },
                            navigateToCalendar = { navController.navigate(calendarRoute) },
                            navigateToSettings = { navController.navigate(settingsRoute) },
                        )
                    }
                    composable(converterRoute) { ConverterScreen(today) }
                    composable(calendarRoute) {
                        CalendarScreen(
                            today = today,
                            localeUtils = localeUtils,
                            preferences = preferences
                        ) { jdn ->
                            navController.graph.findNode(dayRoute)?.let { destination ->
                                navController.navigate(
                                    destination.id, bundleOf(dayJdnKey to jdn.value)
                                )
                            }
                        }
                    }
                    composable(dayRoute) { backStackEntry ->
                        DayScreen(
                            preferences = preferences,
                            localeUtils = localeUtils,
                            day = Jdn(
                                backStackEntry.arguments?.getLong(dayJdnKey, today.value)
                                    ?: today.value
                            )
                        )
                    }
                    composable(settingsRoute) { SettingsScreen(preferences) }
                }
            }
        }
    }
}

@Composable
private fun updatedToday(): Jdn {
    var today by remember { mutableStateOf(Jdn.today()) }

    val currentState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    if (currentState == Lifecycle.State.RESUMED) LaunchedEffect(Unit) {
        while (isActive) {
            today = Jdn.today()
            delay(30.seconds)
        }
    }

    return today
}
