package com.byagowi.persiancalendar.shared

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    AppContainer {
        HelloWorld("Hello, iOS!")
    }
}
