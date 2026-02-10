package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.byagowi.persiancalendar.R

@Composable
fun StopButton(isStopped: MutableState<Boolean>) {
    AppFloatingActionButton(onClick = { isStopped.value = !isStopped.value }) {
        Crossfade(targetState = isStopped.value) { isStopped ->
            Icon(
                imageVector = if (isStopped) Icons.Default.PlayArrow else Icons.Default.Stop,
                contentDescription = stringResource(if (isStopped) R.string.resume else R.string.stop),
            )
        }
    }
}
