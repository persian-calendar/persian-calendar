package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byagowi.persiancalendar.R

@Composable
fun TodayActionButton(visible: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(visible, enter = scaleIn(), exit = scaleOut()) {
        AppIconButton(
            icon = Icons.Default.Restore,
            title = stringResource(R.string.return_to_today),
            onClick = onClick,
        )
    }
}
