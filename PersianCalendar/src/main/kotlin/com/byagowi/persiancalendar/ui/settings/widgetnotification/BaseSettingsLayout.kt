package com.byagowi.persiancalendar.ui.settings.widgetnotification

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.SystemTheme
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha

// This is used in various widgets screens, screen saver and wallpaper settings screens
// Please test the different usages when modifying
@Composable
fun BaseSettingsLayout(
    finish: () -> Unit,
    header: (@Composable () -> Unit)? = null,
    needsMaxHeight: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    BackHandler { finish() }
    SystemTheme {
        Column(
            Modifier
                .safeDrawingPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .padding(top = if (header == null) 16.dp else 0.dp),
        ) {
            header?.invoke()
            Column(
                Modifier
                    .fillMaxWidth()
                    .then(if (needsMaxHeight) Modifier.fillMaxSize() else Modifier.Companion)
                    .alpha(AppBlendAlpha)
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.extraLarge),
            ) {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp)
                ) {
                    Button(
                        onClick = finish,
                        modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
                    ) {
                        Text(
                            stringResource(R.string.accept),
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                    content()
                }
            }
        }
    }
}
