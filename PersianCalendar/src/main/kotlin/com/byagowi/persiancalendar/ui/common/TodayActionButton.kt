package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.runtime.Composable
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.return_to_today
import org.jetbrains.compose.resources.stringResource

@Composable
fun TodayActionButton(visible: Boolean = true, onClick: () -> Unit) {
    AnimatedVisibility(visible, enter = scaleIn(), exit = scaleOut()) {
        AppIconButton(
            icon = Icons.Default.Restore,
            title = stringResource(Res.string.return_to_today),
            onClick = onClick,
        )
    }
}
