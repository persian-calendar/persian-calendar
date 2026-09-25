package com.byagowi.persiancalendar.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.byagowi.persiancalendar.shared.AppContainer
import com.byagowi.persiancalendar.shared.HelloWorld
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

fun main() {
    // Fortunately only macOS needs this hint to get title bar dark mode
    System.setProperty("apple.awt.application.appearance", "system")

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = stringResource(Res.string.app_name),
            state = WindowState(width = 480.dp, height = 720.dp),
        ) {
            AppContainer {
                HelloWorld(stringResource(Res.string.app_name))
            }
        }
    }
}
