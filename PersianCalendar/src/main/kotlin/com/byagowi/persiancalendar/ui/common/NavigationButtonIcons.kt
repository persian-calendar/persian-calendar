package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_OPEN_NAVIGATION_RAIL
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.NavigationOpenNavigationRailIcon(
    animatedContentScope: AnimatedContentScope,
    openNavigationRail: () -> Unit,
) {
    AppIconButton(
        icon = Icons.Default.Menu,
        title = stringResource(R.string.open_navigation_rail),
        modifier = Modifier.sharedElement(
            rememberSharedContentState(SHARED_CONTENT_KEY_OPEN_NAVIGATION_RAIL),
            animatedVisibilityScope = animatedContentScope,
            boundsTransform = appBoundsTransform,
        ),
        onClick = openNavigationRail,
    )
}

@Composable
fun NavigationNavigateUpIcon(navigateUp: () -> Unit) {
    AppIconButton(
        icon = Icons.AutoMirrored.Default.ArrowBack,
        title = stringResource(R.string.navigate_up),
        onClick = navigateUp,
    )
}
