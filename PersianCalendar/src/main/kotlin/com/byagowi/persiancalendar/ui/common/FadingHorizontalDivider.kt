package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

@Composable
fun FadingHorizontalDivider(visible: Boolean) {
    val alpha by animateFloatAsState(
        if (visible) 1f else 0f,
        label = "alpha",
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    )
    HorizontalDivider(Modifier.alpha(alpha))
}
