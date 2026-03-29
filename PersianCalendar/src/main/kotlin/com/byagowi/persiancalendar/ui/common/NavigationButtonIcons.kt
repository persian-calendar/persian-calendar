package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.dropUnlessStarted
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_ARROW_ICON
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_MENU_ICON
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform

@Composable
fun SharedTransitionScope.NavigationOpenNavigationRailIcon(openNavigationRail: () -> Unit) =
    NavigationMenuArrow(fraction = 0f, action = openNavigationRail)

@Composable
fun SharedTransitionScope.NavigationNavigateUpIcon(navigateUp: () -> Unit) =
    NavigationMenuArrow(fraction = 1f, action = navigateUp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.NavigationMenuArrow(
    fraction: Float,
    modifier: Modifier = Modifier,
    action: () -> Unit,
) {
    val title =
        stringResource(if (fraction == 0f) R.string.open_navigation_rail else R.string.navigate_up)
    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(title) } },
        state = rememberTooltipState(),
    ) {
        IconButton(onClick = dropUnlessStarted(block = action)) {
            val iconModifier = Modifier
                .semantics { this.contentDescription = title }
                .sharedElement(
                    sharedContentState = rememberSharedContentState(
                        if (fraction == 0f) SHARED_CONTENT_KEY_MENU_ICON
                        else SHARED_CONTENT_KEY_ARROW_ICON,
                    ),
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                    boundsTransform = appBoundsTransform,
                )
            when (fraction) {
                0f -> Icon(imageVector = Icons.Default.Menu, null, iconModifier)
                1f -> Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, null, iconModifier)
                else -> DrawerArrowDrawable(fraction = fraction, modifier = iconModifier)
            }
        }
    }
}
