package com.byagowi.persiancalendar.ui.theme

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R

enum class Theme(
    val key: String,
    @StringRes val title: Int,
    val hasGradient: Boolean = true,
    private val hasDynamicColors: Boolean = false,
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

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
    fun isDynamicColors(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && hasDynamicColors
    }

    companion object {
        fun isNightMode(context: Context): Boolean =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}
