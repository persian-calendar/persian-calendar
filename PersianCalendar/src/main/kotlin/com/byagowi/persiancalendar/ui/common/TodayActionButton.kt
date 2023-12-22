package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.byagowi.persiancalendar.R

@Composable
fun TodayActionButton(visible: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(visible, enter = scaleIn(), exit = scaleOut()) {
        AppIconButton(
            icon = ImageVector.vectorResource(R.drawable.ic_restore_modified),
            title = stringResource(R.string.return_to_today),
            onClick = onClick,
        )
    }
}
