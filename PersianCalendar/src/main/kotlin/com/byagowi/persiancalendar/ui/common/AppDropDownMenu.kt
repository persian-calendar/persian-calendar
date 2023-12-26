package com.byagowi.persiancalendar.ui.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    minWidth: Dp = 200.dp,
    content: @Composable ColumnScope.(closeMenu: () -> Unit) -> Unit,
) {
    val defaultShapes = MaterialTheme.shapes
    MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = MaterialTheme.shapes.extraLarge)) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = Modifier
                .defaultMinSize(minWidth = minWidth)
                .padding(horizontal = 8.dp),
            content = { MaterialTheme(shapes = defaultShapes) { content(onDismissRequest) } },
        )
    }
}
