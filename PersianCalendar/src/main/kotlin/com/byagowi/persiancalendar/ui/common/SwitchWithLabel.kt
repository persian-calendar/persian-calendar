package com.byagowi.persiancalendar.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SwitchWithLabel(
    label: String,
    checked: Boolean,
    labelBeforeSwitch: Boolean = false,
    toggle: () -> Unit
) {
    Row(
        Modifier.clickable(
            indication = rememberRipple(bounded = false),
            interactionSource = remember { MutableInteractionSource() },
            onClick = toggle,
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (labelBeforeSwitch) {
            Text(label)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Switch(checked, onCheckedChange = { toggle() })
        if (!labelBeforeSwitch) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(label)
        }
    }
}
