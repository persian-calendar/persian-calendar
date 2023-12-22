package com.byagowi.persiancalendar.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R

@Composable
fun ThreeDotsDropdownMenu(content: @Composable ColumnScope.(closeMenu: () -> Unit) -> Unit) {
    Box {
        var showMenu by rememberSaveable { mutableStateOf(false) }
        AppIconButton(
            icon = Icons.Default.MoreVert,
            title = stringResource(R.string.more_options),
        ) { showMenu = !showMenu }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.defaultMinSize(minWidth = 200.dp),
            content = { content { showMenu = false } },
        )
    }
}
