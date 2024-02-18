package com.byagowi.persiancalendar.ui.utils

import androidx.annotation.StringRes
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import com.byagowi.persiancalendar.entities.LocalLanguage
import androidx.compose.ui.res.stringResource as cStringResource

/**
 * Determines if a color should be considered light or dark.
 *
 * See: https://github.com/material-components/material-components-android/blob/dfa474fd/lib/java/com/google/android/material/color/MaterialColors.java#L252
 *
 * Per [androidx.core.graphics.ColorUtils.calculateLuminance], second component of xyz is luminance
 */
@Stable
val Color.isLight: Boolean get() = this.convert(ColorSpaces.CieXyz).component2() > .5

/**
 * As Material's [androidx.compose.material3.tokens.ShapeTokens.CornerExtraLargeTop] isn't exposed and we need it frequently
 * let's build our own based on Material's
 */
@Composable
@Stable
fun materialCornerExtraLargeTop(): CornerBasedShape {
    return MaterialTheme.shapes.extraLarge.copy(
        bottomStart = ZeroCornerSize,
        bottomEnd = ZeroCornerSize,
    )
}

@Composable
@Stable
fun materialCornerExtraLargeNoBottomEnd(): CornerBasedShape {
    return MaterialTheme.shapes.extraLarge.copy(bottomEnd = ZeroCornerSize)
}


/**
 * Load a string resource.
 *
 * @param id the resource identifier
 * @return the string data associated with the resource
 */
@Composable
@ReadOnlyComposable
fun stringResource(@StringRes id: Int): String {
    LocalLanguage.current
    return cStringResource(id)
}

/**
 * Load a string resource with formatting.
 *
 * @param id the resource identifier
 * @param formatArgs the format arguments
 * @return the string data associated with the resource
 */
@Composable
@ReadOnlyComposable
fun stringResource(@StringRes id: Int, vararg formatArgs: Any): String {
    LocalLanguage.current
    return cStringResource(id, formatArgs)
}


// When something needs to match with Material default theme corner sizes
const val ExtraLargeShapeCornerSize = 28f
const val LargeShapeCornerSize = 16f

// Plain items in settings should have this horizontal padding
const val SettingsHorizontalPaddingItem = 24

// Clickable items in settings should have this height
const val SettingsItemHeight = SettingsHorizontalPaddingItem * 2

// Common alpha value to blend a component with it's background
const val AppBlendAlpha = .75f
