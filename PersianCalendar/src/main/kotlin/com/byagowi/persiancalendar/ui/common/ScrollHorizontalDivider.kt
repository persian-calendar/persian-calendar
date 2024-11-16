package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

@Composable
fun ScrollHorizontalDivider(scrollState: ScrollState, top: Boolean) {
    val alpha = if (scrollState.maxValue == Int.MAX_VALUE) {
        // If max value is infinity the page isn't even initialized
        0f
    } else {
        val visible = if (top) scrollState.value != 0 else scrollState.value != scrollState.maxValue
        animateFloatAsState(
            if (visible) 1f else 0f,
            label = "alpha",
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ).value
    }
    HorizontalDivider(Modifier.alpha(alpha))
}
