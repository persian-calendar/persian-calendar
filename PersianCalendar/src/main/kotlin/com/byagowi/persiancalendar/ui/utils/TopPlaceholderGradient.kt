package com.byagowi.persiancalendar.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.ui.theme.animateColor

@Composable
private fun rememberTopPlaceholderColor(): Color {
    val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
    val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
    val needsVisibleStatusBarPlaceHolder = !isBackgroundColorLight && isSurfaceColorLight
    return animateColor(
        if (needsVisibleStatusBarPlaceHolder) Color(0x70000000) else Color.Transparent,
    ).value
}

@Composable
private fun Modifier.topPlaceholderGradient(
    gradientHeight: Int? = null,
    overflow: Dp = 0.dp,
): Modifier {
    val topColor = rememberTopPlaceholderColor()
    var height by remember { mutableFloatStateOf(0f) }
    val direction = LocalLayoutDirection.current
    return this
        .onGloballyPositioned { height = it.positionInRoot().y }
        .drawBehind {
            val drawHeight = gradientHeight?.toFloat() ?: height
            val overflowPx = overflow.toPx()
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, Color.Transparent),
                    startY = -height,
                    // Equivalent to the original `endY = 0f` when drawHeight == height.
                    endY = -height + drawHeight,
                ),
                topLeft = Offset(
                    x = if (direction == LayoutDirection.Rtl) -overflowPx else 0f,
                    y = -height,
                ),
                size = Size(
                    width = size.width + overflowPx,
                    height = drawHeight,
                ),
            )
        }
}

@Composable
fun Modifier.navigationRailTopGradient(): Modifier =
    topPlaceholderGradient(overflow = 20.dp)

@Composable
fun Modifier.expandedSearchTopGradient(gradientHeight: Int): Modifier =
    topPlaceholderGradient(gradientHeight = gradientHeight)
