package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.BaseActivity
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.theme.SystemTheme
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha

// This is used in various widgets screens, screensaver and wallpaper settings screens
// Please test the different usages when modifying
abstract class BaseConfigurationActivity(
    private val contentNeedsMaxHeight: Boolean = false,
) : BaseActivity() {

    final override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        onAfterCreate()
        setContent {
            BackHandler(onBack = ::onBack)
            SystemTheme {
                @Composable
                fun Linear(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
                    val isLandscape =
                        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                    if (isLandscape) Row(modifier) { content() } else Column(modifier) { content() }
                }
                Linear(
                    Modifier
                        .safeDrawingPadding()
                        .padding(16.dp)
                ) {
                    Header()
                    val scrollState = rememberScrollState()
                    val shape = MaterialTheme.shapes.extraLarge
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .then(if (contentNeedsMaxHeight) Modifier.fillMaxSize() else Modifier)
                            .alpha(AppBlendAlpha)
                            .clip(shape)
                            .background(MaterialTheme.colorScheme.surfaceBright),
                    ) {
                        Column(
                            Modifier.verticalScroll(scrollState),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = ::onAcceptClick) { Text(stringResource(R.string.accept)) }
                            Spacer(Modifier.height(4.dp))
                            Settings()
                            Spacer(Modifier.height(16.dp))
                        }
                        ScrollShadow(scrollState)
                    }
                }
            }
        }
    }

    protected open fun onAcceptClick() = finish()
    protected open fun onBack() = onAcceptClick()
    protected open fun onAfterCreate() = Unit

    @Composable
    protected abstract fun ColumnScope.Settings()

    @Composable
    protected open fun Header() = Unit
}
