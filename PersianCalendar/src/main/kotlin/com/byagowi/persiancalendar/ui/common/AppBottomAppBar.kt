package com.byagowi.persiancalendar.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun AppBottomAppBar(content: @Composable RowScope.() -> Unit) {
    BottomAppBar(
        contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 24.dp),
        content = content,
    )
}
