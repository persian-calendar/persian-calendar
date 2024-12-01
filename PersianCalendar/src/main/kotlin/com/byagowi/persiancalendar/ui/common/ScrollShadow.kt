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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.ui.theme.scrollShadowColor
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

// It doesn't consider the amount unlike the non lazy one
@Composable
fun BoxScope.ScrollShadow(listState: LazyListState, top: Boolean) {
    val needsShadow = if (top) listState.canScrollBackward else listState.canScrollForward
    val height = if (listState.canScrollBackward || listState.canScrollForward) animateDpAsState(
        if (needsShadow) 8.dp else 0.dp,
        label = "height",
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    ).value else 0.dp
    ScrollShadowBar(top, height)
}

@Composable
private fun BoxScope.ScrollShadowBar(top: Boolean, height: Dp) {
    val color = scrollShadowColor()
    val colors = if (top) listOf(color, Color.Transparent) else listOf(Color.Transparent, color)
    Box(
        Modifier
            .align(if (top) Alignment.TopCenter else Alignment.BottomCenter)
            .fillMaxWidth()
            .height(height)
            .background(Brush.verticalGradient(colors)),
    )
}
