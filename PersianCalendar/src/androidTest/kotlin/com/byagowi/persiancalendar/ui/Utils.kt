package com.byagowi.persiancalendar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.ComposeContentTestRule

@OptIn(ExperimentalSharedTransitionApi::class)
fun ComposeContentTestRule.setContentWithParent(
    content: @Composable SharedTransitionScope.(AnimatedContentScope) -> Unit
) {
    setContent {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
            SharedTransitionLayout {
                AnimatedContent(targetState = Unit, label = "") { state ->
                    state.let {}
                    content(this)
                }
            }
        }
    }
}
