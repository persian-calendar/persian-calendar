package com.byagowi.persiancalendar.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.byagowi.persiancalendar.shared.AppContainer
import com.byagowi.persiancalendar.shared.HelloWorld
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val app = document.getElementById("app")
    requireNotNull(app) { "Missing element with id 'app'" }
    ComposeViewport(app) {
        AppContainer {
            HelloWorld("Hello, Web!")
        }
    }
}
