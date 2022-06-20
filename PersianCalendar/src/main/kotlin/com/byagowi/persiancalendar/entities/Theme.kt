package com.byagowi.persiancalendar.entities

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.PowerManager
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.google.android.material.color.DynamicColors

enum class Theme(val key: String, @StringRes val title: Int, @StyleRes private val styleRes: Int) {
    SYSTEM_DEFAULT("SystemDefault", R.string.theme_default, R.style.LightTheme),
    LIGHT("LightTheme", R.string.theme_light, R.style.LightTheme),
    DARK("DarkTheme", R.string.theme_dark, R.style.DarkTheme),
    MODERN("ClassicTheme"/*legacy*/, R.string.theme_modern, R.style.ModernTheme),
    BLUE("BlueTheme", R.string.theme_aqua, R.style.AquaTheme),
    BLACK("BlackTheme", R.string.theme_black, R.style.BlackTheme);

    companion object {
        private val SharedPreferences?.theme
            get() = this?.getString(PREF_THEME, null) ?: SYSTEM_DEFAULT.key

        fun apply(activity: AppCompatActivity) {
            val theme = getCurrent(activity)
            if (theme != SYSTEM_DEFAULT) return activity.setTheme(theme.styleRes)
            val isNightModeEnabled = isNightMode(activity)

            if (isDynamicColorAvailable()) {
                activity.setTheme(
                    if (isNightModeEnabled) R.style.DynamicDarkTheme else R.style.DynamicLightTheme
                )
                DynamicColors.applyToActivityIfAvailable(activity)
            } else activity.setTheme(if (isNightModeEnabled) DARK.styleRes else LIGHT.styleRes)

            // Apply blur considerations only if is supported by the device
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                activity.windowManager.isCrossWindowBlurEnabled
            ) activity.setTheme(R.style.AlertDialogThemeBlurSupportedOverlay)
        }

        private fun getCurrent(context: Context): Theme {
            val key = context.appPrefs.theme
            val userTheme = values().find { it.key == key } ?: SYSTEM_DEFAULT
            if (userTheme != SYSTEM_DEFAULT) return userTheme
            if (isPowerSaveMode(context)) return BLACK
            return SYSTEM_DEFAULT
        }

        @StyleRes
        fun getWidgetSuitableStyle(context: Context): Int {
            val isNightMode = isNightMode(context)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isNightMode) R.style.DynamicDarkTheme else R.style.DynamicLightTheme
            } else MODERN.styleRes
        }

        fun isDefault(prefs: SharedPreferences?) = prefs.theme == SYSTEM_DEFAULT.key

        // DynamicColors.isDynamicColorAvailable() checks for particular brands but sooner or later
        // all the brands will have dynamic colors, let's skip its check.
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
        fun isDynamicColorAvailable() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
        fun isDynamicColor(prefs: SharedPreferences?) =
            isDynamicColorAvailable() && isDefault(prefs)

        fun isNightMode(context: Context): Boolean =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        private fun isPowerSaveMode(context: Context): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                    context.getSystemService<PowerManager>()?.isPowerSaveMode == true
        }
    }
}
