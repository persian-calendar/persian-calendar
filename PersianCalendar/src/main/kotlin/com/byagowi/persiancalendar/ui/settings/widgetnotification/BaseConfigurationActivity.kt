package com.byagowi.persiancalendar.ui.settings.widgetnotification

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.CallSuper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
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
import com.byagowi.persiancalendar.ui.BaseActivity
import com.byagowi.persiancalendar.ui.theme.SystemTheme
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha

// This is used in various widgets screens, screen saver and wallpaper settings screens
// Please test the different usages when modifying
abstract class BaseConfigurationActivity(
    private val contentNeedsMaxHeight: Boolean = false
) : BaseActivity() {
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            BackHandler(onBack = ::onBack)
            SystemTheme {
                Column(
                    Modifier
                        .safeDrawingPadding()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                ) {
                    Header()
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .then(if (contentNeedsMaxHeight) Modifier.fillMaxSize() else Modifier)
                            .alpha(AppBlendAlpha)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.extraLarge,
                            )
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(onClick = ::onAcceptClick) { Text(stringResource(R.string.accept)) }
                        Content()
                    }
                }
            }
        }
    }

    protected open fun onAcceptClick() = finish()
    protected open fun onBack() = onAcceptClick()

    @Composable
    protected abstract fun ColumnScope.Content()

    @Composable
    protected open fun Header() {
        Spacer(Modifier.padding(16.dp))
    }
}
