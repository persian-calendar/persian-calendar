package com.byagowi.persiancalendar.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val appColorAnimationSpec @Composable get() = MaterialTheme.motionScheme.slowSpatialSpec<Color>()

@Composable
fun animateColor(color: Color) = animateColorAsState(color, appColorAnimationSpec, "color")
