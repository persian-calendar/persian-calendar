package com.byagowi.persiancalendar.utils

import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.R

enum class Theme(val key: String, @StringRes val title: Int, @StyleRes private val styleRes: Int) {
    SYSTEM_DEFAULT("SystemDefault", R.string.theme_default, R.style.LightTheme),
    LIGHT("LightTheme", R.string.theme_light, R.style.LightTheme),
    DARK("DarkTheme", R.string.theme_dark, R.style.DarkTheme),
    MODERN("ClassicTheme"/*legacy*/, R.string.theme_modern, R.style.ModernTheme),
    BLUE("BlueTheme", R.string.theme_blue, R.style.BlueTheme),
    BLACK("BlackTheme", R.string.theme_black, R.style.BlackTheme);

    companion object {
        private val SharedPreferences?.theme
            get() = this?.getString(PREF_THEME, null) ?: SYSTEM_DEFAULT.key

        fun apply(activity: AppCompatActivity) {
            val key = activity.appPrefs.theme
            activity.setTheme((values().find { it.key == key }?.takeIf { it != SYSTEM_DEFAULT }
                ?: if (isNightModeEnabled(activity)) DARK else LIGHT).styleRes)
        }

        fun isNonDefault(appPrefs: SharedPreferences?) = appPrefs.theme != SYSTEM_DEFAULT.key
    }
}
