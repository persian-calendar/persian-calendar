package com.byagowi.persiancalendar.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.dynamicColorScheme
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.byagowi.persiancalendar.dataStore
import com.byagowi.persiancalendar.requestComplicationUpdate
import com.byagowi.persiancalendar.requestTileUpdate
import io.github.persiancalendar.calendar.CivilDate
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.GregorianCalendar
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
                val preferences by context.dataStore.data.collectAsState(null)
                val navController = rememberSwipeDismissableNavController()
                val mainRoute = "app"
                val settingsRoute = "settings"
                val utilitiesRoute = "utilities"
                val converterRoute = "converter"
                val calendarRoute = "calendar"
                val dayRoute = "day"
                val dayJdnKey = "dayJdnKey"
                val today by rememberUpdatedToday()
                SwipeDismissableNavHost(
                    navController = navController,
                    startDestination = mainRoute,
                ) {
                    composable(mainRoute) {
                        MainScreen(
                            today = today,
                            preferences = preferences,
                            navigateToUtilities = { navController.navigate(utilitiesRoute) },
                            navigateToDay = { jdn ->
                                navController.graph.findNode(dayRoute)?.let { destination ->
                                    navController.navigate(
                                        destination.id, bundleOf(dayJdnKey to jdn)
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
                    composable(converterRoute) { ConverterScreen() }
                    composable(calendarRoute) {
                        CalendarScreen(preferences) { jdn ->
                            navController.graph.findNode(dayRoute)?.let { destination ->
                                navController.navigate(
                                    destination.id, bundleOf(dayJdnKey to jdn)
                                )
                            }
                        }
                    }
                    composable(dayRoute) { backStackEntry ->
                        DayScreen(
                            preferences = preferences,
                            jdn = backStackEntry.arguments?.getLong(dayJdnKey, today) ?: today
                        )
                    }
                    composable(settingsRoute) { SettingsScreen(preferences) }
                }
            }
        }
    }
}

fun todayJdn(): Long {
    val calendar = GregorianCalendar.getInstance()
    return CivilDate(
        calendar[GregorianCalendar.YEAR],
        calendar[GregorianCalendar.MONTH] + 1,
        calendar[GregorianCalendar.DAY_OF_MONTH],
    ).toJdn()
}

@Composable
private fun rememberUpdatedToday(): State<Long> {
    val lifecycleOwner = LocalLifecycleOwner.current

    return produceState(initialValue = todayJdn(), lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    todayJdn().let { if (value != it) value = it }
                    launch {
                        while (isActive) {
                            delay(30.seconds)
                            todayJdn().let { if (value != it) value = it }
                        }
                    }
                }

                Lifecycle.Event.ON_PAUSE -> coroutineContext.cancelChildren()

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        awaitDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            coroutineContext.cancelChildren()
        }
    }
}
