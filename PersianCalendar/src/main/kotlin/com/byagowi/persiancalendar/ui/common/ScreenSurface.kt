package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_CARD
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_CARD_CONTENT
import com.byagowi.persiancalendar.global.customImageName
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.needsScreenSurfaceDragHandle
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ScreenSurface(
    animatedContentScope: AnimatedContentScope,
    shape: CornerBasedShape = materialCornerExtraLargeTop(),
    // Remove when https://issuetracker.google.com/issues/376709945 is resolved
    // Actually this can be simplified into a simple Box inside a Surface when that resolved
    workaroundClipBug: Boolean = false,
    disableSharedContent: Boolean = false,
    mayNeedDragHandleToDivide: Boolean = false,
    drawBehindSurface: Boolean = true,
    content: @Composable () -> Unit,
) {
    Layout(content = {
        // Parent
        run {
            val outlineColor = MaterialTheme.colorScheme.outline
            val needsScreenSurfaceDragHandle =
                mayNeedDragHandleToDivide && needsScreenSurfaceDragHandle()
            val customImageName by customImageName.collectAsState()
            val surfaceColor by animateColor(
                MaterialTheme.colorScheme.surface.let {
                    if (customImageName == null) it else it.copy(AppBlendAlpha)
                }
            )
            val density = LocalDensity.current
            Canvas(
                modifier = if (disableSharedContent) Modifier else Modifier.sharedElement(
                    rememberSharedContentState(SHARED_CONTENT_KEY_CARD),
                    animatedVisibilityScope = animatedContentScope,
                    boundsTransform = appBoundsTransform,
                )
            ) {
                val size = this.size
                if (needsScreenSurfaceDragHandle) drawRoundRect(
                    outlineColor,
                    Offset(size.width / 2f - 24.dp.toPx(), -6.dp.toPx()),
                    Size(48.dp.toPx(), 4.dp.toPx()),
                    CornerRadius(4.dp.toPx()),
                    alpha = .375f,
                )
                run {
                    val outline = shape.createOutline(size, this.layoutDirection, density)
                    drawOutline(outline, surfaceColor)
                }
                // Ugly but in order to support overshoot animations let's draw surface color
                // under the content
                if (drawBehindSurface) drawRect(surfaceColor, Offset(0f, size.height), size)
            }
        }
        // Content
        Box(
            (if (disableSharedContent) Modifier else Modifier.sharedBounds(
                rememberSharedContentState(SHARED_CONTENT_KEY_CARD_CONTENT),
                animatedVisibilityScope = animatedContentScope,
                boundsTransform = appBoundsTransform,
            )).clip(if (workaroundClipBug) MaterialTheme.shapes.extraLarge else shape)
        ) {
            val onSurface by animateColor(MaterialTheme.colorScheme.onSurface)
            CompositionLocalProvider(LocalContentColor provides onSurface, content)
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
