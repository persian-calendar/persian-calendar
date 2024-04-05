package com.byagowi.persiancalendar.ui.theme

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R

enum class Theme(
    val key: String,
    @StringRes val title: Int,
    val hasGradient: Boolean = true,
    private val lackDynamicColors: Boolean = false,
    val isDark: Boolean = false,
) {
    SYSTEM_DEFAULT("SystemDefault", R.string.theme_default),
    LIGHT("LightTheme", R.string.theme_light),
    MODERN("ClassicTheme"/*legacy*/, R.string.theme_modern),
    AQUA("BlueTheme"/*legacy*/, R.string.theme_aqua, lackDynamicColors = true),
    DARK("DarkTheme", R.string.theme_dark, isDark = true),
    BLACK("BlackTheme", R.string.theme_black, hasGradient = false, isDark = true);

    val isDynamicColors
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S) get() =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !lackDynamicColors
}
