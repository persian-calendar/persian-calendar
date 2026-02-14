package com.byagowi.persiancalendar.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalView
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@Composable
fun <T> ChangesHapticFeedback(key: State<T>) {
    val view = LocalView.current
    @OptIn(FlowPreview::class) LaunchedEffect(key1 = Unit) {
        snapshotFlow { key.value }.debounce(500).collect {
            view.performHapticFeedbackVirtualKey()
        }
    }
}
