package com.byagowi.persiancalendar.ui.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
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
    modifier: Modifier = Modifier,
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
            modifier = modifier,
        )
    }
}

@Composable
fun AppDropdownMenuRadioItem(
    text: String,
    isSelected: Boolean,
    withRadio: Boolean = true,
    setSelected: () -> Unit,
) {
    AppDropdownMenuItem(
        modifier = Modifier.selectable(isSelected, onClick = setSelected, role = Role.RadioButton),
        text = { Text(text) },
        trailingIcon = { if (withRadio) RadioButton(selected = isSelected, onClick = null) },
    ) { setSelected() }
}

@Composable
fun AppDropdownMenuCheckableItem(
    text: String,
    isChecked: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    AppDropdownMenuItem(
        modifier = Modifier.toggleable(
            value = isChecked,
            onValueChange = onValueChange,
            role = Role.Checkbox
        ),
        text = { Text(text) },
        trailingIcon = { Checkbox(checked = isChecked, onCheckedChange = null) },
    ) { onValueChange(!isChecked) }
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
