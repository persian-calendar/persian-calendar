package com.byagowi.persiancalendar.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.R

enum class Theme(
    val key: String,
    @StringRes val title: Int,
    val hasGradient: Boolean = true,
    val hasDynamicColors: Boolean = false,
    val isDark: Boolean = false,
) {
    SYSTEM_DEFAULT("SystemDefault", R.string.theme_default, hasDynamicColors = true),
    LIGHT("LightTheme", R.string.theme_light),
    DARK("DarkTheme", R.string.theme_dark, isDark = true),
    MODERN("ClassicTheme",/*legacy*/R.string.theme_modern, hasDynamicColors = true),
    AQUA("BlueTheme"/*legacy*/, R.string.theme_aqua),
    BLACK(
        "BlackTheme", R.string.theme_black,
        hasGradient = false, hasDynamicColors = true, isDark = true
    );

    companion object {
        private fun SharedPreferences?.getTheme() =
            this?.getString(PREF_THEME, null) ?: SYSTEM_DEFAULT.key

        fun getCurrent(prefs: SharedPreferences): Theme {
            val key = prefs.getTheme()
            return entries.find { it.key == key } ?: SYSTEM_DEFAULT
        }

        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
        fun isDynamicColor(prefs: SharedPreferences?): Boolean {
            val themeKey = prefs.getTheme()
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    entries.firstOrNull { it.key == themeKey }?.hasDynamicColors ?: false
        }

        fun isNightMode(context: Context): Boolean =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}
