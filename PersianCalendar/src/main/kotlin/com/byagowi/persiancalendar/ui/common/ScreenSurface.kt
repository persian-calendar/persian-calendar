package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_CARD
import com.byagowi.persiancalendar.ui.theme.animatedSurfaceColor
import com.byagowi.persiancalendar.ui.utils.animationsEnabled
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
        modifier = if (LocalContext.current.animationsEnabled()) Modifier.sharedElement(
            rememberSharedContentState(SHARED_CONTENT_KEY_CARD),
            animatedVisibilityScope = animatedContentScope,
        ) else Modifier,
        content = content,
    )
}
