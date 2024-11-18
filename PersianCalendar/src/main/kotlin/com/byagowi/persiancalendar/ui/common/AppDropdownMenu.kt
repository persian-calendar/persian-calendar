package com.byagowi.persiancalendar.ui.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
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
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.defaultMinSize(minWidth = minWidth),
        shape = MaterialTheme.shapes.extraLarge,
        content = { content(onDismissRequest) },
    )
}

@Composable
fun AppDropdownMenuItem(
    text: @Composable () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    MaterialTheme(
        typography = MaterialTheme.typography.copy(labelLarge = MaterialTheme.typography.bodyLarge),
    ) {
        DropdownMenuItem(
            text = text,
            onClick = onClick,
            trailingIcon = trailingIcon,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
            enabled = enabled,
        )
    }
}

@Composable
fun AppDropdownMenuRadioItem(
    text: String,
    isSelected: Boolean,
    setSelected: (Boolean) -> Unit,
) {
    AppDropdownMenuItem(
        text = { Text(text) },
        trailingIcon = { RadioButton(selected = isSelected, onClick = null) },
    ) { setSelected(!isSelected) }
}

@Composable
fun AppDropdownMenuCheckableItem(
    text: String,
    isChecked: Boolean,
    setChecked: (Boolean) -> Unit,
) {
    AppDropdownMenuItem(
        text = { Text(text) },
        trailingIcon = { Checkbox(checked = isChecked, onCheckedChange = null) },
    ) { setChecked(!isChecked) }
}

@Composable
fun AppDropdownMenuExpandableItem(
    text: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    AppDropdownMenuItem(
        text = { Text(text) },
        trailingIcon = { ExpandArrow(isExpanded = isExpanded) },
        onClick = onClick,
    )
}
