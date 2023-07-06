package com.byagowi.persiancalendar.entities

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.google.android.material.color.DynamicColors

enum class Theme(
    val key: String,
    @StringRes val title: Int,
    @StyleRes private val styleRes: Int,
    private val hasGradient: Boolean = true,
    private val hasDynamicColors: Boolean = false,
) {
    SYSTEM_DEFAULT(
        "SystemDefault", R.string.theme_default, R.style.LightTheme,
        hasDynamicColors = true
    ),
    LIGHT("LightTheme", R.string.theme_light, R.style.LightTheme),
    DARK("DarkTheme", R.string.theme_dark, R.style.DarkTheme),
    MODERN(
        "ClassicTheme"/*legacy*/, R.string.theme_modern, R.style.ModernTheme,
        hasDynamicColors = true
    ),
    AQUA("BlueTheme"/*legacy*/, R.string.theme_aqua, R.style.AquaTheme),
    BLACK(
        "BlackTheme", R.string.theme_black, R.style.BlackTheme,
        hasGradient = false, hasDynamicColors = true
    );

    companion object {
        private fun SharedPreferences?.getTheme() =
            this?.getString(PREF_THEME, null) ?: SYSTEM_DEFAULT.key

        fun supportsGradient(context: Context) = getCurrent(context).hasGradient &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

        fun apply(activity: AppCompatActivity) {
            val theme = getCurrent(activity)
            val isDynamicColorAvailable = DynamicColors.isDynamicColorAvailable()
            if (theme == SYSTEM_DEFAULT || (isDynamicColorAvailable && theme.hasDynamicColors)) {
                val isNightModeEnabled = isNightMode(activity)

                if (isDynamicColorAvailable) {
                    activity.setTheme(
                        when {
                            theme == BLACK -> R.style.DynamicBlackTheme
                            theme == MODERN -> R.style.DynamicModernTheme
                            isNightModeEnabled -> R.style.DynamicDarkTheme
                            else -> R.style.DynamicLightTheme
                        }
                    )
                    DynamicColors.applyToActivityIfAvailable(activity)
                    activity.setTheme(
                        when {
                            theme == BLACK -> R.style.DynamicBlackSurfaceOverride
                            theme == MODERN -> R.style.DynamicModernSurfaceOverride
                            isNightModeEnabled -> R.style.DynamicDarkSurfaceOverride
                            else -> R.style.DynamicLightSurfaceOverride
                        }
                    )
                } else activity.setTheme(if (isNightModeEnabled) DARK.styleRes else LIGHT.styleRes)
            } else activity.setTheme(theme.styleRes)

            activity.setTheme(R.style.SharedStyle)

            // Apply blur considerations only if is supported by the device
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                activity.windowManager.isCrossWindowBlurEnabled
            ) activity.setTheme(R.style.AlertDialogThemeBlurSupportedOverlay)
        }

        private fun getCurrent(context: Context): Theme {
            val key = context.appPrefs.getTheme()
            return entries.find { it.key == key } ?: SYSTEM_DEFAULT
        }

        @StyleRes
        fun getWidgetSuitableStyle(context: Context, prefersWidgetsDynamicColors: Boolean): Int {
            val isNightMode = isNightMode(context)
            return if (prefersWidgetsDynamicColors) {
                if (isNightMode) R.style.DynamicDarkTheme else R.style.DynamicModernTheme
            } else MODERN.styleRes
        }

        fun isDefault(prefs: SharedPreferences?) = prefs.getTheme() == SYSTEM_DEFAULT.key

        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
        fun isDynamicColor(prefs: SharedPreferences?): Boolean {
            val themeKey = prefs.getTheme()
            return DynamicColors.isDynamicColorAvailable() &&
                    entries.firstOrNull { it.key == themeKey }?.hasDynamicColors ?: false
        }

        fun isNightMode(context: Context): Boolean =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}
