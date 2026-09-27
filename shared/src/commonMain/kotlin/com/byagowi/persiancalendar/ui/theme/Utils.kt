package com.byagowi.persiancalendar.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


val appColorAnimationSpec = spring<Color>(stiffness = Spring.StiffnessMediumLow)

@Composable
fun animateColor(color: Color) = animateColorAsState(color, appColorAnimationSpec, "color")
