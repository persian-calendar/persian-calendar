package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
                imageVector = if (isStopped) Icons.Default.PlayArrow else Icons.Default.Stop,
                contentDescription = stringResource(
                    if (isStopped) Res.string.resume else Res.string.stop,
                ),
            )
        }
    }
}
