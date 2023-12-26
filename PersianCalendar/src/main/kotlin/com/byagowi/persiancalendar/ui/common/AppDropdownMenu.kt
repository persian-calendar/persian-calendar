package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
            modifier = Modifier.defaultMinSize(minWidth = minWidth),
            content = { MaterialTheme(shapes = defaultShapes) { content(onDismissRequest) } },
        )
    }
}

@Composable
fun AppDropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
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
        onClick = { setSelected(!isSelected) },
    )
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
        onClick = { setChecked(!isChecked) },
    )
}

@Composable
fun AppDropdownMenuExpandableItem(
    text: String,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    AppDropdownMenuItem(
        text = { Text(text) },
        trailingIcon = {
            val angle by animateFloatAsState(if (isExpanded) 180f else 0f, label = "angle")
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.rotate(angle),
            )
        },
        onClick = onClick,
    )
}
