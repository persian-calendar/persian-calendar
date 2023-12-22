package com.byagowi.persiancalendar.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byagowi.persiancalendar.R

@Composable
fun NavigationOpenDrawerIcon(openDrawer: () -> Unit) {
    AppIconButton(
        icon = Icons.Default.Menu,
        title = stringResource(R.string.open_drawer),
        onClick = openDrawer,
    )
}

@Composable
fun NavigationNavigateUpIcon(navigateUp: () -> Unit) {
    AppIconButton(
        icon = Icons.AutoMirrored.Default.ArrowBack,
        title = stringResource(R.string.navigate_up),
        onClick = navigateUp,
    )
}
