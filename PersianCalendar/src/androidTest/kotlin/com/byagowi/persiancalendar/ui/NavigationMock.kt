package com.byagowi.persiancalendar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.ui.LocalNavAnimatedContentScope

@Composable
fun NavigationMock(content: @Composable SharedTransitionScope.() -> Unit) {
    SharedTransitionLayout {
        AnimatedContent(targetState = Unit) { state ->
            state.let {}
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                LocalNavAnimatedContentScope provides this@AnimatedContent,
            ) { this@SharedTransitionLayout.content() }
        }
    }
}
