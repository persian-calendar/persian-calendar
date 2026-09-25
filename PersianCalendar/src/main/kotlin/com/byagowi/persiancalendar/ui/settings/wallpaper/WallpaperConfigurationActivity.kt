package com.byagowi.persiancalendar.ui.settings.wallpaper

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_WALLPAPER_ALTERNATIVE
import com.byagowi.persiancalendar.PREF_WALLPAPER_AUTOMATIC
import com.byagowi.persiancalendar.PREF_WALLPAPER_DARK
import com.byagowi.persiancalendar.global.wallpaperAlternative
import com.byagowi.persiancalendar.global.wallpaperAutomatic
import com.byagowi.persiancalendar.global.wallpaperDark
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.theme_dark
import com.byagowi.persiancalendar.shared.generated.resources.theme_default
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.ui.settings.widgetnotification.BaseConfigurationActivity
import org.jetbrains.compose.resources.stringResource

class WallpaperConfigurationActivity : BaseConfigurationActivity() {
    @Composable
    override fun ColumnScope.Settings() {
        SettingsSwitch(
            key = PREF_WALLPAPER_AUTOMATIC,
            value = wallpaperAutomatic,
            title = stringResource(Res.string.theme_default),
        )
        AnimatedVisibility(!wallpaperAutomatic) {
            SettingsSwitch(
                key = PREF_WALLPAPER_DARK,
                value = wallpaperDark,
                title = stringResource(Res.string.theme_dark),
            )
        }
        if (BuildConfig.DEVELOPMENT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) SettingsSwitch(
            key = PREF_WALLPAPER_ALTERNATIVE,
            value = wallpaperAlternative,
            title = "Alternative",
        )
    }
}
