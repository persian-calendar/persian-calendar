package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun SelectionIndicator(color: Color, radius: Float, center: Offset, visible: Boolean) {
    val animatedCenter = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    LaunchedEffect(center) {
        if (visible) animatedCenter.animateTo(
            targetValue = center,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }

    val animatedRadiusFraction = remember { Animatable(0f) }
    LaunchedEffect(visible) {
        if (visible) animatedCenter.snapTo(center)
        animatedRadiusFraction.animateTo(
            targetValue = if (visible) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }

    Canvas(Modifier.fillMaxSize()) {
        val animatedRadius = radius * animatedRadiusFraction.value
        drawCircle(color = color, center = animatedCenter.value, radius = animatedRadius)
    }
}
