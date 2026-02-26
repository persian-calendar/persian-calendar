package com.byagowi.persiancalendar.ui.utils

import android.view.View
import android.view.ViewParent
import android.view.Window
import androidx.annotation.ColorRes
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.DialogWindowProvider

/**
 * Determines if a color should be considered light or dark.
 *
 * See also:
 * * https://github.com/material-components/material-components-android/blob/dfa474fd/lib/java/com/google/android/material/color/MaterialColors.java#L252
 * * https://github.com/androidx/androidx/blob/95394634/core/core/src/main/java/androidx/core/graphics/ColorUtils.java#L159
 */
@Stable
val Color.isLight: Boolean get() = this.luminance() > .5

@Composable
fun getResourcesColor(@ColorRes id: Int) = Color(LocalResources.current.getColor(id, null))

/**
 * As Material's [androidx.compose.material3.tokens.ShapeTokens.CornerExtraLargeTop] isn't exposed
 * while we need it frequently let's build our own based on Material's
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

@Composable
fun Modifier.highlightItem(enabled: Boolean, isOnBackground: Boolean = false): Modifier {
    if (!enabled) return this
    val alpha = rememberSaveable { mutableFloatStateOf(.1f) }
    if (isOnBackground) return this
    LaunchedEffect(Unit) {
        animate(
            initialValue = alpha.floatValue,
            targetValue = 0f,
            animationSpec = tween(4000),
        ) { value, _ -> alpha.floatValue = value }
    }
    return this.background(LocalContentColor.current.copy(alpha = alpha.floatValue))
}

fun View.findDialogWindow(): Window? =
    (this as? DialogWindowProvider ?: parent?.findDialogWindowProvider())?.window

private tailrec fun ViewParent.findDialogWindowProvider(): DialogWindowProvider? =
    this as? DialogWindowProvider ?: parent?.findDialogWindowProvider()

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

// When something needs to match with Material default theme corner sizes
const val ExtraLargeShapeCornerSize = 28f
const val LargeShapeCornerSize = 16f
const val SmallShapeCornerSize = 8f

// Plain items in settings should have this horizontal padding
const val SettingsHorizontalPaddingItem = 24

// Clickable items in settings should have this height
const val SettingsItemHeight = SettingsHorizontalPaddingItem * 2

// Common alpha value to blend a component with its background
const val AppBlendAlpha = .75f

// analogous to compat's listPreferredItemPaddingLeft/Right, it's in .dp
const val ItemWidth = 100f
