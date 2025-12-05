package com.byagowi.persiancalendar.ui.settings.wallpaper

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_WALLPAPER_ALTERNATIVE
import com.byagowi.persiancalendar.PREF_WALLPAPER_AUTOMATIC
import com.byagowi.persiancalendar.PREF_WALLPAPER_DARK
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.wallpaperAlternative
import com.byagowi.persiancalendar.global.wallpaperAutomatic
import com.byagowi.persiancalendar.global.wallpaperDark
import com.byagowi.persiancalendar.ui.BaseActivity
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.ui.settings.widgetnotification.BaseSettingsLayout

class WallpaperSettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            BaseSettingsLayout(finish = ::finish, needsMaxHeight = false) {
                val wallpaperAutomatic by wallpaperAutomatic.collectAsState()
                SettingsSwitch(
                    key = PREF_WALLPAPER_AUTOMATIC,
                    value = wallpaperAutomatic,
                    title = stringResource(R.string.theme_default)
                )
                this.AnimatedVisibility(!wallpaperAutomatic) {
                    val wallpaperDark by wallpaperDark.collectAsState()
                    SettingsSwitch(
                        key = PREF_WALLPAPER_DARK,
                        value = wallpaperDark,
                        title = stringResource(R.string.theme_dark)
                    )
                }
                if (BuildConfig.DEVELOPMENT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) SettingsSwitch(
                    key = PREF_WALLPAPER_ALTERNATIVE,
                    value = wallpaperAlternative.collectAsState().value,
                    title = "Alternative"
                )
            }
        }
    }
}
