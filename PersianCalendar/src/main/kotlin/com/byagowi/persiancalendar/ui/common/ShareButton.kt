package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_SHARE_BUTTON
import com.byagowi.persiancalendar.ui.utils.isOnCI

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ShareActionButton(
    animatedContentScope: AnimatedContentScope,
    action: () -> Unit,
) {
    AppIconButton(
        icon = Icons.Default.Share,
        title = stringResource(R.string.share),
        // Workaround CI not liking shared elements
        iconModifier = if (LocalContext.current.isOnCI()) Modifier else Modifier.sharedElement(
            rememberSharedContentState(SHARED_CONTENT_KEY_SHARE_BUTTON),
            animatedVisibilityScope = animatedContentScope,
        ),
        onClick = action,
    )
}
