package com.byagowi.persiancalendar.ui.theme

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
    AQUA("BlueTheme"/*legacy*/, R.string.theme_aqua),
    DARK("DarkTheme", R.string.theme_dark, isDark = true),
    BLACK(
        "BlackTheme", R.string.theme_black,
        hasGradient = false, hasDynamicColors = true, isDark = true
    ),
    MODERN("ClassicTheme",/*legacy*/R.string.theme_modern, hasDynamicColors = true);

    val isDynamicColors
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S) get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && hasDynamicColors
}
