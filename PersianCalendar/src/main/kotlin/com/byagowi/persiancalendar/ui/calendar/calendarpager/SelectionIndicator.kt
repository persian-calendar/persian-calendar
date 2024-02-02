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
fun SelectionIndicator(color: Color, radius: Float, center: Offset?) {
    val animatedCenter = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val animatedRadiusFraction = remember { Animatable(0f) }

    LaunchedEffect(key1 = center != null) {
        if (center != null) animatedCenter.snapTo(center)
        val target = if (center != null) 1f else 0f
        if (animatedRadiusFraction.value != target) animatedRadiusFraction.animateTo(
            targetValue = target,
            animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow),
        )
    }

    LaunchedEffect(key1 = center) {
        if (center != null && animatedCenter.value != center) animatedCenter.animateTo(
            targetValue = center,
            animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow),
        )
    }

    Canvas(Modifier.fillMaxSize()) {
        val radiusFraction = animatedRadiusFraction.value
        if (radiusFraction > 0f) drawCircle(
            color = color,
            center = animatedCenter.value,
            radius = radius * radiusFraction,
        )
    }
}
