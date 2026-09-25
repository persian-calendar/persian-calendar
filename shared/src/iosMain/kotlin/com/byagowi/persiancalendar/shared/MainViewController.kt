package com.byagowi.persiancalendar.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

fun MainViewController() = ComposeUIViewController {
    AppContainer {
        HelloWorld(stringResource(Res.string.app_name))
    }
}
