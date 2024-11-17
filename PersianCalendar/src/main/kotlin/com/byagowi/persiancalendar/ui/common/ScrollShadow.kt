package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
    val height = if (scrollState.maxValue == Int.MAX_VALUE) {
        // If max value is infinity the page isn't even initialized
        0.dp
    } else {
        val visible = if (top) scrollState.value != 0 else scrollState.value != scrollState.maxValue
        animateDpAsState(
            if (visible) 8.dp else 0.dp,
            label = "alpha",
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ).value
    }
    val color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 1 / 3f)
    val colors = if (top) listOf(color, Color.Transparent) else listOf(Color.Transparent, color)
    Box(
        Modifier
            .align(if (top) Alignment.TopCenter else Alignment.BottomCenter)
            .fillMaxWidth()
            .height(height)
            .background(Brush.verticalGradient(colors)),
    )
}
