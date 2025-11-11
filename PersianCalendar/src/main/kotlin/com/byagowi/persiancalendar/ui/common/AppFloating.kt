package com.byagowi.persiancalendar.ui.common

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.global.isGradient
import com.byagowi.persiancalendar.ui.theme.animateColor

@Composable
fun AppFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val containerColor = FloatingActionButtonDefaults.containerColor
    val contentColor = contentColorFor(containerColor)
    val isGradient by isGradient.collectAsState()
    FloatingActionButton(
        onClick = onClick,
        containerColor = animateColor(containerColor).value,
        contentColor = animateColor(contentColor).value,
        modifier = modifier,
        content = content,
        elevation = if (!isGradient) FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
        ) else FloatingActionButtonDefaults.elevation(),
    )
}
