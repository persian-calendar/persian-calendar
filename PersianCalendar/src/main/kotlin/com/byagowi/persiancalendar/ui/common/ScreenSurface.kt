package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Constraints
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_CARD
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_CARD_CONTENT
import com.byagowi.persiancalendar.ui.theme.animatedSurfaceColor
import com.byagowi.persiancalendar.ui.utils.isOnCI
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ScreenSurface(
    animatedContentScope: AnimatedContentScope,
    shape: Shape = materialCornerExtraLargeTop(),
    content: @Composable () -> Unit,
) {
    Layout(content = {
        // Workaround CI not liking shared elements
        val isOnCI = LocalContext.current.isOnCI()
        // Parent
        Surface(
            shape = shape,
            color = animatedSurfaceColor(),
            modifier = if (isOnCI) Modifier else Modifier.sharedElement(
                rememberSharedContentState(SHARED_CONTENT_KEY_CARD),
                animatedVisibilityScope = animatedContentScope,
            ),
        ) {}
        // Content
        Box(
            (if (isOnCI) Modifier else Modifier.sharedBounds(
                rememberSharedContentState(SHARED_CONTENT_KEY_CARD_CONTENT),
                animatedVisibilityScope = animatedContentScope,
            )).clip(shape)
        ) {
            val contentColor by animateColorAsState(
                targetValue = contentColorFor(MaterialTheme.colorScheme.surface),
                label = "content color",
            )
            CompositionLocalProvider(LocalContentColor provides contentColor, content)
        }
    }) { (parent, content), constraints ->
        val placeableContent = content.measure(constraints)
        val childConstraints = Constraints.fixed(placeableContent.width, placeableContent.height)
        val placeableParent = parent.measure(childConstraints)
        layout(placeableContent.width, placeableContent.height) {
            placeableParent.placeRelative(0, 0)
            placeableContent.placeRelative(0, 0)
        }
    }
}
