package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.lerp
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.ui.utils.appContentSizeAnimationSpec
import com.byagowi.persiancalendar.ui.utils.performLongPress

@Composable
fun <T> AppScreenModesDropDown(
    value: T,
    onValueChange: (T) -> Unit,
    label: @Composable (T) -> String,
    values: Iterable<T>,
    small: Boolean = false,
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    Box(
        Modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(LocalContentColor.current.copy(alpha = .175f))
            .clickable {
                showMenu = !showMenu
                if (showMenu) hapticFeedback.performLongPress()
            },
    ) {
        var dropDownWidth by remember { mutableIntStateOf(0) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = if (small) 0.dp else 4.dp)
                .padding(start = if (small) 8.dp else 16.dp, end = if (small) 2.dp else 8.dp)
                .defaultMinSize(minHeight = if (small) 0.dp else 38.dp)
                .onSizeChanged { dropDownWidth = it.width },
        ) {
            Text(
                label(value),
                style = lerp(
                    MaterialTheme.typography.titleMedium,
                    MaterialTheme.typography.titleLarge,
                    animateFloatAsState(
                        targetValue = if (small) 0f else 1f,
                        label = "fraction"
                    ).value,
                ),
                maxLines = 1,
                modifier = Modifier
                    .semantics { this.role = Role.DropdownList }
                    .animateContentSize(appContentSizeAnimationSpec),
            )
            ExpandArrow(
                isExpanded = showMenu,
                modifier = Modifier.requiredSize(if (small) 16.dp else 24.dp),
            )
        }
        AppDropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            minWidth = with(LocalDensity.current) { dropDownWidth.toDp() },
        ) {
            values.forEach { entry ->
                AppDropdownMenuRadioItem(
                    label(entry),
                    value == entry,
                    withRadio = false,
                ) {
                    showMenu = false
                    hapticFeedback.performLongPress()
                    onValueChange(entry)
                }
            }
        }
    }
}
