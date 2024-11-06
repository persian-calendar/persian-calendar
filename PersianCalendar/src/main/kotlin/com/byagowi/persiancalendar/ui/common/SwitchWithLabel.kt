package com.byagowi.persiancalendar.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.ui.utils.performLongPress

@Composable
fun SwitchWithLabel(
    label: String,
    checked: Boolean,
    labelBeforeSwitch: Boolean = false,
    toggle: () -> Unit,
) {
    Row(
        Modifier.clickable(
            indication = ripple(bounded = false),
            interactionSource = null,
            onClick = toggle,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (labelBeforeSwitch) {
            Text(label)
            Spacer(modifier = Modifier.width(8.dp))
        }
        val hapticFeedback = LocalHapticFeedback.current
        Switch(checked, onCheckedChange = { hapticFeedback.performLongPress(); toggle() })
        if (!labelBeforeSwitch) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(label)
        }
    }
}
