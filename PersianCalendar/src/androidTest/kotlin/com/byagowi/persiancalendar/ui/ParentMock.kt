package com.byagowi.persiancalendar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ParentMock(content: @Composable SharedTransitionScope.(AnimatedContentScope) -> Unit) {
    SharedTransitionLayout {
        AnimatedContent(targetState = Unit, label = "") { state ->
            state.let {}
            content(this)
        }
    }
}
