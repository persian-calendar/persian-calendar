package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun ExpandArrow(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    tint: Color = LocalContentColor.current,
    contentDescription: String? = null,
) {
    val unexpandedAngle = if (LocalLayoutDirection.current == LayoutDirection.Ltr) -180f else 180f
    val angle by animateFloatAsState(if (isExpanded) unexpandedAngle else 0f, label = "angle")
    Icon(
        imageVector = Icons.Default.ExpandMore,
        contentDescription = contentDescription,
        modifier = Modifier.rotate(angle) then modifier,
        tint = tint,
    )
}
