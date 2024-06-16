package com.byagowi.persiancalendar.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * All Material icons (currently) are 24dp by 24dp, with a viewport size of 24 by 24.
 *
 * Redefined [androidx.compose.material.icons.MaterialIconDimension] as it isn't accessible
 */
const val MaterialIconDimension = 24f

// From https://stackoverflow.com/a/71723593 with some tweaks
fun makeIconFromPath(
    path: String,
    viewportWidth: Float = MaterialIconDimension,
    viewportHeight: Float = MaterialIconDimension,
    defaultWidth: Dp = MaterialIconDimension.dp,
    defaultHeight: Dp = MaterialIconDimension.dp,
    fillColor: Color = Color.White,
): ImageVector {
    val fillBrush = SolidColor(fillColor)
    val strokeBrush = SolidColor(fillColor)

    return ImageVector.Builder(
        defaultWidth = defaultWidth,
        defaultHeight = defaultHeight,
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
    ).run {
        addPath(
            pathData = addPathNodes(path),
            name = "",
            fill = fillBrush,
            stroke = strokeBrush,
        )
        build()
    }
}
