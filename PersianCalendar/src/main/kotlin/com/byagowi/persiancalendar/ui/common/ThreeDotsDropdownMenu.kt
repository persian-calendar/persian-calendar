package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_THREE_DOTS_MENU
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ThreeDotsDropdownMenu(
    animatedContentScope: AnimatedContentScope,
    content: @Composable ColumnScope.(onDismissRequest: () -> Unit) -> Unit,
) {
    Box(
        Modifier.sharedElement(
            rememberSharedContentState(key = SHARED_CONTENT_KEY_THREE_DOTS_MENU),
            animatedVisibilityScope = animatedContentScope,
            boundsTransform = appBoundsTransform,
        )
    ) {
        var expanded by rememberSaveable { mutableStateOf(false) }
        run {
            val rotation by animateFloatAsState(
                targetValue = if (!expanded) 0f
                else if (LocalLayoutDirection.current == LayoutDirection.Rtl) -90f else 90f,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                ),
                label = "rotation",
            )
            AppIconButton(
                icon = Icons.Default.MoreVert,
                title = stringResource(R.string.more_options),
                modifier = Modifier.rotate(rotation),
            ) { expanded = !expanded }
        }
        AppDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            content = content,
        )
    }
}
