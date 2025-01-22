package com.byagowi.persiancalendar.ui.utils

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.entities.Jdn

/**
 * Determines if a color should be considered light or dark.
 *
 * See also:
 * * https://github.com/material-components/material-components-android/blob/dfa474fd/lib/java/com/google/android/material/color/MaterialColors.java#L252
 * * https://github.com/androidx/androidx/blob/95394634/core/core/src/main/java/androidx/core/graphics/ColorUtils.java#L159
 */
@Stable
val Color.isLight: Boolean get() = this.luminance() > .5

// https://github.com/auchenberg/volkswagen like idea…
// Please don't use it outside shared elements context
// For more context https://github.com/ReactiveCircus/android-emulator-runner/issues/417
fun Context.isOnCI(): Boolean = BuildConfig.DEVELOPMENT && Settings.Global.getFloat(
    contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f
) == 0f // Our current CI config disables animation

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

val JdnSaver = Saver<MutableState<Jdn>, Long>(
    save = { it.value.value },
    restore = { mutableStateOf(Jdn(it)) }
)

val AnimatableFloatSaver = Saver<Animatable<Float, AnimationVector1D>, Float>(
    save = { it.value },
    restore = { Animatable(it) }
)

// When something needs to match with Material default theme corner sizes
const val ExtraLargeShapeCornerSize = 28f
const val LargeShapeCornerSize = 16f
const val SmallShapeCornerSize = 8f

// Plain items in settings should have this horizontal padding
const val SettingsHorizontalPaddingItem = 24

// Clickable items in settings should have this height
const val SettingsItemHeight = SettingsHorizontalPaddingItem * 2

// Common alpha value to blend a component with it's background
const val AppBlendAlpha = .75f

// analogous to compat's listPreferredItemPaddingLeft/Right, it's in .dp
const val ItemWidth = 100f
