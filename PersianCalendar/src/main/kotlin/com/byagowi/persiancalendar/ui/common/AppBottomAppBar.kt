package com.byagowi.persiancalendar.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.ui.theme.animateColor

@Composable
fun AppBottomAppBar(
    overlay: @Composable () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    BottomAppBar(
        windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Bottom)
            .add(WindowInsets(bottom = 4.dp)),
        contentPadding = PaddingValues.Zero,
        containerColor = animateColor(MaterialTheme.colorScheme.surfaceContainer).value,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                Modifier.padding(PaddingValues(start = 16.dp, end = 24.dp)),
                verticalAlignment = Alignment.CenterVertically,
            ) { content() }
            overlay()
        }
    }
}
