package com.byagowi.persiancalendar.ui.common

import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.byagowi.persiancalendar.ui.theme.SetupDialogBlur
import com.byagowi.persiancalendar.ui.theme.appDialogSurfaceColor

@Composable
fun DialogSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    SetupDialogBlur()
    Surface(
        shape = AlertDialogDefaults.shape,
        color = appDialogSurfaceColor(),
        contentColor = contentColorFor(AlertDialogDefaults.containerColor),
        tonalElevation = AlertDialogDefaults.TonalElevation,
        content = content,
        modifier = modifier,
    )
}
