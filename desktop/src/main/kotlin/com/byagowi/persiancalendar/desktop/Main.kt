package com.byagowi.persiancalendar.desktop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.byagowi.persiancalendar.desktop.windows.DwmApi
import com.byagowi.persiancalendar.shared.AppContainer
import com.byagowi.persiancalendar.shared.HelloWorld
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

fun main() {
    // macOS needs this hint to get title bar dark mode
    System.setProperty("apple.awt.application.appearance", "system")

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = stringResource(Res.string.app_name),
            state = WindowState(width = 800.dp, height = 600.dp),
        ) {
            val isSystemInDarkTheme = isSystemInDarkTheme()
            LaunchedEffect(key1 = isSystemInDarkTheme) {
                DwmApi.applyWindowsDarkMode(window, isDark = isSystemInDarkTheme)
            }

            AppContainer {
                HelloWorld(stringResource(Res.string.app_name))
            }
        }
    }
}
