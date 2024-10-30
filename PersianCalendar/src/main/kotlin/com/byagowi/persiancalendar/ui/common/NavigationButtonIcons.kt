package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_OPEN_DRAWER
import com.byagowi.persiancalendar.ui.utils.isOnCI

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.NavigationOpenDrawerIcon(
    animatedContentScope: AnimatedContentScope,
    openDrawer: () -> Unit,
) {
    AppIconButton(
        icon = Icons.Default.Menu,
        title = stringResource(R.string.open_drawer),
        // Workaround CI not liking shared elements
        iconModifier = if (LocalContext.current.isOnCI()) Modifier else Modifier.sharedElement(
            rememberSharedContentState(SHARED_CONTENT_KEY_OPEN_DRAWER),
            animatedVisibilityScope = animatedContentScope,
        ),
        onClick = openDrawer,
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
