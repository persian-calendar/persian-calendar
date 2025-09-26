package com.byagowi.persiancalendar.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.ui.utils.performLongPress

@Composable
fun SwitchWithLabel(
    label: String,
    checked: Boolean,
    labelBeforeSwitch: Boolean = false,
    onValueChange: (Boolean) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    Row(
        Modifier
            .toggleable(checked, onValueChange = onValueChange, role = Role.Switch)
            .clickable(
                indication = ripple(bounded = false),
                interactionSource = null,
                onClick = { hapticFeedback.performLongPress(); onValueChange(!checked) },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (labelBeforeSwitch) {
            Text(label)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Switch(checked, null, Modifier.minimumInteractiveComponentSize())
        if (!labelBeforeSwitch) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(label)
        }
    }
}
