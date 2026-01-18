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
import androidx.compose.ui.res.stringResource
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_NEXT_ARROW
import com.byagowi.persiancalendar.SHARED_CONTENT_PREVIOUS_ARROW
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform
import com.byagowi.persiancalendar.ui.utils.performLongPress

@Composable
fun SharedTransitionScope.TimeArrow(
    onClick: () -> Unit,
    onClickLabel: String,
    onLongClick: () -> Unit,
    onLongClickLabel: String,
    isPrevious: Boolean,
) {
    val hapticFeedback = LocalHapticFeedback.current
    Icon(
        imageVector = if (isPrevious) Icons.AutoMirrored.Default.KeyboardArrowLeft
        else Icons.AutoMirrored.Default.KeyboardArrowRight,
        contentDescription = stringResource(
            if (isPrevious) R.string.previous_x else R.string.next_x,
            stringResource(R.string.day),
        ),
        modifier = Modifier
            .sharedElement(
                sharedContentState = rememberSharedContentState(
                    if (isPrevious) SHARED_CONTENT_PREVIOUS_ARROW else SHARED_CONTENT_NEXT_ARROW,
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
