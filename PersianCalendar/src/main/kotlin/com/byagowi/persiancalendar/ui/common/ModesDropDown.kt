package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.ui.utils.appContentSizeAnimationSpec
import com.byagowi.persiancalendar.ui.utils.performLongPress

@Composable
fun <T> ModesDropDown(
    value: T,
    onValueChange: (T) -> Unit,
    title: @Composable (T) -> String,
    values: Iterable<T>,
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    Box(
        Modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(LocalContentColor.current.copy(alpha = .175f))
            .semantics { this.role = Role.DropdownList }
            .clickable {
                showMenu = !showMenu
                if (showMenu) hapticFeedback.performLongPress()
            },
    ) {
        var dropDownWidth by remember { mutableIntStateOf(0) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .defaultMinSize(minHeight = 38.dp)
                .onSizeChanged { dropDownWidth = it.width },
        ) {
            Spacer(Modifier.width(16.dp))
            Text(title(value), Modifier.animateContentSize(appContentSizeAnimationSpec))
            ExpandArrow(isExpanded = showMenu)
            Spacer(Modifier.width(8.dp))
        }
        AppDropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            minWidth = with(LocalDensity.current) { dropDownWidth.toDp() },
        ) {
            values.forEach { entry ->
                AppDropdownMenuRadioItem(
                    title(entry),
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
