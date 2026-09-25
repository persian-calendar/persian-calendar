package com.byagowi.persiancalendar.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.byagowi.persiancalendar.shared.AppContainer
import com.byagowi.persiancalendar.shared.HelloWorld

fun main() {
    // Make the native window title bar follow the system appearance on macOS
    // (Compose themes the content itself, but the title bar needs this hint).
    System.setProperty("apple.awt.application.appearance", "system")

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Persian Calendar",
            state = WindowState(width = 480.dp, height = 720.dp),
        ) {
            AppContainer {
                HelloWorld("Hello, Desktop!")
            }
        }
    }
}
