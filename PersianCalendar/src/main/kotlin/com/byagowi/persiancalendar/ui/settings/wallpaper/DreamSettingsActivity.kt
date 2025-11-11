package com.byagowi.persiancalendar.ui.settings.wallpaper

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.PREF_DREAM_NOISE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.dreamNoise
import com.byagowi.persiancalendar.ui.BaseActivity
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.ui.theme.SystemTheme
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha

class DreamSettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            BackHandler { finish() }
            SystemTheme {
                Column(modifier = Modifier.safeDrawingPadding()) {
                    Column(
                        Modifier
                            .alpha(AppBlendAlpha)
                            .verticalScroll(rememberScrollState())
                            .padding(all = 16.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.shapes.extraLarge
                            )
                            .padding(vertical = 16.dp),
                    ) {
                        Button(
                            onClick = ::finish,
                            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
                        ) {
                            Text(
                                stringResource(R.string.accept),
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                        }
                        val dreamNoise by dreamNoise.collectAsState()
                        SettingsSwitch(
                            key = PREF_DREAM_NOISE,
                            value = dreamNoise,
                            title = "🔊🔊🔊",
                        )
                    }
                }
            }
        }
    }
}
