package com.byagowi.persiancalendar.ui.settings.wallpaper

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.byagowi.persiancalendar.PREF_DREAM_NOISE
import com.byagowi.persiancalendar.global.dreamNoise
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.ui.settings.widgetnotification.BaseConfigurationActivity

class ScreensaverConfigurationActivity : BaseConfigurationActivity() {
    @Composable
    override fun ColumnScope.Settings() {
        val dreamNoise by dreamNoise.collectAsState()
        SettingsSwitch(key = PREF_DREAM_NOISE, value = dreamNoise, title = "🔊🔊🔊")
    }
}
