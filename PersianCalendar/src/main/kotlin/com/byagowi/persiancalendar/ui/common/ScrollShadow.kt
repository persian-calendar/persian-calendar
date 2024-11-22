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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun BoxScope.ScrollShadow(scrollState: ScrollState, top: Boolean) {
    // If max value is infinity the page isn't even initialized
    val height = if (scrollState.maxValue == Int.MAX_VALUE) 0.dp else animateDpAsState(
        with(LocalDensity.current) {
            (abs((scrollState.value - (if (top) 0 else scrollState.maxValue))) / 8).toDp()
        }.coerceAtMost(8.dp),
        label = "scroll shadow height",
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    ).value
    ScrollShadowBar(top, height)
}

// Only top is supported with lazy list
@Composable
fun BoxScope.ScrollShadow(listState: LazyListState) {
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }
    val height by animateDpAsState(if (isAtTop) 0.dp else 8.dp, label = "height")
    ScrollShadowBar(true, height)
}

@Composable
private fun BoxScope.ScrollShadowBar(top: Boolean, height: Dp) {
    val color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f)
    val colors = if (top) listOf(color, Color.Transparent) else listOf(Color.Transparent, color)
    Box(
        Modifier
            .align(if (top) Alignment.TopCenter else Alignment.BottomCenter)
            .fillMaxWidth()
            .height(height)
            .background(Brush.verticalGradient(colors)),
    )
}
