package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.return_to_today
import com.byagowi.persiancalendar.ui.icons.material.filled.restoreIcon
import org.jetbrains.compose.resources.stringResource

@Composable
fun TodayActionButton(visible: Boolean = true, onClick: () -> Unit) {
    AnimatedVisibility(visible, enter = scaleIn(), exit = scaleOut()) {
        AppIconButton(
            icon = restoreIcon,
            title = stringResource(Res.string.return_to_today),
            onClick = onClick,
        )
    }
}
