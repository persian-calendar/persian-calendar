package com.byagowi.persiancalendar.ui.common

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_CARD
import com.byagowi.persiancalendar.ui.theme.animatedSurfaceColor
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ScreenSurface(
    animatedContentScope: AnimatedContentScope,
    shape: Shape = materialCornerExtraLargeTop(),
    content: @Composable () -> Unit,
) {
    Surface(
        shape = shape,
        color = animatedSurfaceColor(),
        // Workaround CI not liking shared elements
        modifier = if (LocalContext.current.animationsEnabled()) Modifier.sharedBounds(
            rememberSharedContentState(SHARED_CONTENT_KEY_CARD),
            animatedVisibilityScope = animatedContentScope,
            enter = EnterTransition.None,
        ) else Modifier,
        content = content,
    )
}

private fun Context.animationsEnabled(): Boolean =
    Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f) != 0f
