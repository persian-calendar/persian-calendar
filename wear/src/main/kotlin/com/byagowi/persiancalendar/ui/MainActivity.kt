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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.MotionScheme
import androidx.wear.compose.material3.dynamicColorScheme
import androidx.wear.compose.navigation3.rememberSwipeDismissableSceneStrategy
import com.byagowi.persiancalendar.Jdn
import com.byagowi.persiancalendar.LocaleUtils
import com.byagowi.persiancalendar.dataStore
import com.byagowi.persiancalendar.requestComplicationsUpdate
import com.byagowi.persiancalendar.requestTileUpdate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        run {
            // Request update of both on activity just in case
            requestComplicationsUpdate()
            requestTileUpdate()
        }
        setContent { WearApp() }
    }
}

@JvmSynthetic
@Composable
private fun WearApp() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = dynamicColorScheme(LocalContext.current) ?: MaterialTheme.colorScheme,
            motionScheme = MotionScheme.expressive(),
        ) {
            AppScaffold {
                val context = LocalContext.current
                val dataStore = context.dataStore.data
                val preferences by dataStore.collectAsState(
                    remember { runBlocking { dataStore.firstOrNull() } },
                )
                val localeUtils = LocaleUtils()
                val today = updatedToday()
                val backStack = rememberNavBackStack(Screen.Main)
                fun Screen.navigate() {
                    backStack += this
                }
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    sceneStrategy = rememberSwipeDismissableSceneStrategy(),
                    entryProvider = entryProvider {
                        entry<Screen.Main> {
                            MainScreen(
                                localeUtils = localeUtils,
                                today = today,
                                preferences = preferences,
                                navigateToUtilities = Screen.Utilities::navigate,
                                navigateToDay = { Screen.Day(it).navigate() },
                            )
                        }
                        entry<Screen.Utilities> {
                            UtilitiesScreen(
                                navigateToConverter = Screen.Converter::navigate,
                                navigateToCalendar = Screen.Calendar::navigate,
                                navigateToSettings = Screen.Settings::navigate,
                            )
                        }
                        entry<Screen.Converter> { ConverterScreen(today) }
                        entry<Screen.Calendar> {
                            CalendarScreen(
                                today = today,
                                localeUtils = localeUtils,
                                preferences = preferences,
                            ) { Screen.Day(it).navigate() }
                        }
                        entry<Screen.Day> {
                            DayScreen(
                                preferences = preferences,
                                localeUtils = localeUtils,
                                day = it.jdn,
                            )
                        }
                        entry<Screen.Settings> { SettingsScreen(preferences) }
                    },
                )
            }
        }
    }
}

private sealed interface Screen : NavKey {
    @Serializable
    data object Main : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object Utilities : Screen

    @Serializable
    data object Calendar : Screen

    @Serializable
    data object Converter : Screen

    @Serializable
    data class Day(val jdn: Jdn) : Screen
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
