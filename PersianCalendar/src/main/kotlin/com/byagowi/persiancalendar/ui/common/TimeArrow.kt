package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.byagowi.persiancalendar.SHARED_CONTENT_NEXT_ARROW
import com.byagowi.persiancalendar.SHARED_CONTENT_PREVIOUS_ARROW
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.day
import com.byagowi.persiancalendar.shared.generated.resources.next_x
import com.byagowi.persiancalendar.shared.generated.resources.previous_x
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform
import com.byagowi.persiancalendar.ui.utils.performLongPress
import org.jetbrains.compose.resources.stringResource

@Composable
fun SharedTransitionScope.TimeArrow(
    onClick: () -> Unit,
    onClickLabel: String,
    onLongClick: () -> Unit,
    onLongClickLabel: String,
    isPrevious: Boolean,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current
    Icon(
        imageVector = if (isPrevious) Icons.AutoMirrored.Default.KeyboardArrowLeft
        else Icons.AutoMirrored.Default.KeyboardArrowRight,
        contentDescription = stringResource(
            if (isPrevious) Res.string.previous_x else Res.string.next_x,
            stringResource(Res.string.day),
        ),
        modifier = modifier
            .sharedElement(
                sharedContentState = rememberSharedContentState(
                    key = if (isPrevious) SHARED_CONTENT_PREVIOUS_ARROW
                    else SHARED_CONTENT_NEXT_ARROW,
                ),
                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                boundsTransform = appBoundsTransform,
            )
            .combinedClickable(
                indication = ripple(bounded = false),
                interactionSource = null,
                onClick = {
                    hapticFeedback.performLongPress()
                    onClick()
                },
                onClickLabel = onClickLabel,
                onLongClick = {
                    hapticFeedback.performLongPress()
                    onLongClick()
                },
                onLongClickLabel = onLongClickLabel,
            ),
        tint = MaterialTheme.colorScheme.primary,
    )
}
