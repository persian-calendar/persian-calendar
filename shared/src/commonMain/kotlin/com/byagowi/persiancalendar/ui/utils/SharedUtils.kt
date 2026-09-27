package com.byagowi.persiancalendar.ui.utils

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize

val appBoundsTransform = BoundsTransform { _, _ ->
    spring(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioLowBouncy,
        visibilityThreshold = Rect.VisibilityThreshold,
    )
}

val appContentSizeAnimationSpec = spring(
    stiffness = Spring.StiffnessMediumLow,
    dampingRatio = Spring.DampingRatioLowBouncy,
    visibilityThreshold = IntSize.VisibilityThreshold,
)
