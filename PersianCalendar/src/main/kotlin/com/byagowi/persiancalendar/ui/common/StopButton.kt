package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.Crossfade
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.byagowi.persiancalendar.icons.material.filled.playArrowIcon
import com.byagowi.persiancalendar.icons.material.filled.stopIcon
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.resume
import com.byagowi.persiancalendar.shared.generated.resources.stop
import org.jetbrains.compose.resources.stringResource

@Composable
fun StopButton(
    isStopped: Boolean,
    modifier: Modifier = Modifier,
    onIsStoppedChange: (Boolean) -> Unit,
) {
    AppFloatingActionButton(
        modifier = modifier,
        onClick = { onIsStoppedChange(!isStopped) },
    ) {
        Crossfade(targetState = isStopped) { isStopped ->
            Icon(
                imageVector = if (isStopped) playArrowIcon else stopIcon,
                contentDescription = stringResource(
                    if (isStopped) Res.string.resume else Res.string.stop,
                ),
            )
        }
    }
}
