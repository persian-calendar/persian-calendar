package com.byagowi.persiancalendar.ui.athan

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

@Composable
fun PatternCanvas(patternDrawable: PatternDrawable) {
    val direction = remember { listOf(1, -1).random() }
    val infiniteTransition = rememberInfiniteTransition()
    val animationSpec = infiniteRepeatable<Float>(
        animation = tween(durationMillis = 360_000, easing = LinearEasing),
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f, animationSpec = animationSpec,
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawIntoCanvas { patternDrawable.draw(it, rotation * direction) }
    }
}
