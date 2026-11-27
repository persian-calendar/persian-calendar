package com.byagowi.persiancalendar.ui.utils

import androidx.compose.animation.BoundsTransform
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize

val appBoundsTransform: BoundsTransform
    @Composable get() {
        val spec = MaterialTheme.motionScheme.defaultSpatialSpec<Rect>()
        return BoundsTransform { _, _ -> spec }
    }

val appContentSizeAnimationSpec
    @Composable get() = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()
