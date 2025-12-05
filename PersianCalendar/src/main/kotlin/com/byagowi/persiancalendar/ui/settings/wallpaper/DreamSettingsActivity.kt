package com.byagowi.persiancalendar.ui.settings.wallpaper

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.byagowi.persiancalendar.PREF_DREAM_NOISE
import com.byagowi.persiancalendar.global.dreamNoise
import com.byagowi.persiancalendar.ui.BaseActivity
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.ui.settings.widgetnotification.BaseSettingsLayout

class DreamSettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            BaseSettingsLayout(finish = ::finish, needsMaxHeight = false) {
                val dreamNoise by dreamNoise.collectAsState()
                SettingsSwitch(key = PREF_DREAM_NOISE, value = dreamNoise, title = "🔊🔊🔊")
            }
        }
    }
}
