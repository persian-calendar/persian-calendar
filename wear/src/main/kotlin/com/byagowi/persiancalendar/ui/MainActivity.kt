package com.byagowi.persiancalendar.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.dynamicColorScheme
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.byagowi.persiancalendar.requestComplicationUpdate
import com.byagowi.persiancalendar.requestTileUpdate

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
                val navController = rememberSwipeDismissableNavController()
                val mainRoute = "app"
                val settingsRoute = "settings"
                val utilitiesRoute = "utilities"
                val converterRoute = "converter"
                SwipeDismissableNavHost(
                    navController = navController,
                    startDestination = mainRoute
                ) {
                    composable(mainRoute) {
                        MainScreen(navigateToUtilities = { navController.navigate(utilitiesRoute) })
                    }
                    composable(utilitiesRoute) {
                        UtilitiesScreen(
                            navigateToConverter = { navController.navigate(converterRoute) },
                            navigateToSettings = { navController.navigate(settingsRoute) },
                        )
                    }
                    composable(converterRoute) { ConverterScreen() }
                    composable(settingsRoute) { SettingsScreen() }
                }
            }
        }
    }
}
