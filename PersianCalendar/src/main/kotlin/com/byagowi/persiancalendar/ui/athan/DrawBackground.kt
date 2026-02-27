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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import com.byagowi.persiancalendar.entities.PrayTime

@Composable
fun DrawBackground(patternDrawable: PatternDrawable, durationMillis: Int) {
    val direction = remember { listOf(1, -1).random() }
    val infiniteTransition = rememberInfiniteTransition()
    val animationSpec = infiniteRepeatable<Float>(
        animation = tween(durationMillis = durationMillis, easing = LinearEasing),
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f, animationSpec = animationSpec,
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawIntoCanvas { patternDrawable.draw(it.nativeCanvas, rotation * direction) }
    }
}

@Preview
@Composable
internal fun AthanActivityContentPreview() = AthanActivityContent(PrayTime.FAJR) {}
