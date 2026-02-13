package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.ui.theme.animateColor

@Composable
fun AppBottomAppBar(
    overlay: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    hideContainer: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val surfaceContainer by animateColor(MaterialTheme.colorScheme.surfaceContainer)
    val offsetFraction by animateFloatAsState(
        if (hideContainer) 100f else 0f,
        spring(stiffness = Spring.StiffnessMediumLow),
    )
    BottomAppBar(
        modifier = modifier.drawBehind {
            translate(top = offsetFraction / 100 * this.size.height) {
                drawRect(surfaceContainer)
            }
        },
        windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Bottom)
            .add(WindowInsets(bottom = 4.dp)),
        contentPadding = PaddingValues.Zero,
        containerColor = Color.Transparent,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                Modifier.padding(PaddingValues(start = 16.dp, end = 24.dp)),
                verticalAlignment = Alignment.CenterVertically,
            ) { content() }
            overlay()
        }
    }
}
