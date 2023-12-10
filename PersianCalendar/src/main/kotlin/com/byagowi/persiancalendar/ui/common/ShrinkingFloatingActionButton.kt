package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.utils.THREE_SECONDS_AND_HALF_IN_MILLIS
import kotlinx.coroutines.delay

@Composable
fun ShrinkingFloatingActionButton(
    modifier: Modifier,
    isVisible: Boolean,
    action: () -> Unit,
    icon: ImageVector,
    title: String,
) {
    var showLabel by rememberSaveable { mutableStateOf(true) }
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = scaleIn(),
        exit = scaleOut(),
    ) {
        FloatingActionButton(onClick = action) {
            Row(
                Modifier.padding(horizontal = if (showLabel) 16.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = title)
                LaunchedEffect(null) {
                    delay(THREE_SECONDS_AND_HALF_IN_MILLIS)
                    showLabel = false
                }
                AnimatedVisibility(showLabel) { Text(title) }
            }
        }
    }
}
