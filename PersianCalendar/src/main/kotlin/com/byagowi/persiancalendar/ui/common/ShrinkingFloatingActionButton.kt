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
import com.byagowi.persiancalendar.ui.theme.appFabElevation
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

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
        FloatingActionButton(onClick = action, elevation = appFabElevation()) {
            Row(
                Modifier.padding(horizontal = if (showLabel) 16.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = title)
                LaunchedEffect(Unit) {
                    // Matches https://github.com/aosp-mirror/platform_frameworks_base/blob/1dcde70/services/core/java/com/android/server/notification/NotificationManagerService.java#L382
                    delay(3.5.seconds)
                    showLabel = false
                }
                AnimatedVisibility(showLabel) { Text(title) }
            }
        }
    }
}
