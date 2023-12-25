package com.byagowi.persiancalendar.ui.icons

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

val In24HoursIcon by lazy(LazyThreadSafetyMode.NONE) {
    makeIconFromPath(
        // Made from:
        // * https://fonts.google.com/icons?selected=Material%20Icons%20Outlined%3Alocal_convenience_store%3A
        // * https://fonts.google.com/icons?selected=Material%20Icons%20Outlined%3Areplay_30%3A
        "m12,1 l5,5 -5,5v-4c-3.3,0 -6,2.7 -6,6s2.7,6 6,6 6,-2.7 6,-6h2c0,4.4 -3.6,8 -8,8s-8,-3.6 -8,-8 3.6,-8 8,-8zM8.6,12v0.87h1.7v0.87h-1.7v2.6h2.6v-0.87h-1.7v-0.87h1.7v-2.6zM12.6,12v2.6h1.7v1.7h0.87v-4.4h-0.87v1.7h-0.87v-1.7z"
    )
}

@Preview
@Composable
fun In24HoursPreview() = Icon(In24HoursIcon, null, tint = Color.Gray)
