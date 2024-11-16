package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BoxScope.ScrollShadow(scrollState: ScrollState, top: Boolean) {
    val alpha = if (scrollState.maxValue == Int.MAX_VALUE) {
        // If max value is infinity the page isn't even initialized
        0f
    } else {
        val visible = if (top) scrollState.value != 0 else scrollState.value != scrollState.maxValue
        animateFloatAsState(
            if (visible) 1 / 3f else 0f,
            label = "alpha",
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ).value
    }
    val color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha)
    val colors = if (top) listOf(color, Color.Transparent) else listOf(Color.Transparent, color)
    Box(
        Modifier
            .align(if (top) Alignment.TopCenter else Alignment.BottomCenter)
            .fillMaxWidth()
            .height(8.dp)
            .background(Brush.verticalGradient(colors)),
    )
}
