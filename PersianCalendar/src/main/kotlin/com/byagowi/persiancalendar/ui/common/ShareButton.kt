package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_SHARE_BUTTON
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform

@Composable
fun SharedTransitionScope.ShareActionButton(action: () -> Unit) {
    AppIconButton(
        icon = Icons.Default.Share,
        title = stringResource(R.string.share),
        modifier = Modifier.sharedElement(
            rememberSharedContentState(SHARED_CONTENT_KEY_SHARE_BUTTON),
            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
            boundsTransform = appBoundsTransform,
        ),
        onClick = action,
    )
}
