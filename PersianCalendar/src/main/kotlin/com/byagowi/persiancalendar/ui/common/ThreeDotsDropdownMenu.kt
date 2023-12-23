package com.byagowi.persiancalendar.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.byagowi.persiancalendar.R

@Composable
fun ThreeDotsDropdownMenu(
    content: @Composable ColumnScope.(onDismissRequest: () -> Unit) -> Unit,
) {
    Box {
        var showMenu by rememberSaveable { mutableStateOf(false) }
        AppIconButton(
            icon = Icons.Default.MoreVert,
            title = stringResource(R.string.more_options),
        ) { showMenu = !showMenu }
        AppDropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            content = content,
        )
    }
}
