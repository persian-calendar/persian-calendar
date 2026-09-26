package com.byagowi.persiancalendar.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.byagowi.persiancalendar.shared.AppContainer
import com.byagowi.persiancalendar.shared.HelloWorld
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.app_name
import kotlinx.browser.document
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val app = document.getElementById("app")
    requireNotNull(app) { "Missing element with id 'app'" }
    ComposeViewport(app) {
        AppContainer {
            HelloWorld(stringResource(Res.string.app_name))
        }
    }
}
