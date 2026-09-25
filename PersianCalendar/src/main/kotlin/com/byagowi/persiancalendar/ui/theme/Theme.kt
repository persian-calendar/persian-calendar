package com.byagowi.persiancalendar.ui.theme

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.theme_aqua
import com.byagowi.persiancalendar.shared.generated.resources.theme_black
import com.byagowi.persiancalendar.shared.generated.resources.theme_dark
import com.byagowi.persiancalendar.shared.generated.resources.theme_default
import com.byagowi.persiancalendar.shared.generated.resources.theme_light
import com.byagowi.persiancalendar.shared.generated.resources.theme_modern
import org.jetbrains.compose.resources.StringResource

enum class Theme(
    val key: String,
    val title: StringResource,
    val hasGradient: Boolean = true,
    private val lackDynamicColors: Boolean = false,
    // This is null in system default, if that's needed, use effectiveTheme()
    val isDark: Boolean? = false,
) {
    SYSTEM_DEFAULT("SystemDefault", Res.string.theme_default, isDark = null),
    LIGHT("LightTheme", Res.string.theme_light),
    MODERN("ClassicTheme"/*legacy*/, Res.string.theme_modern),
    AQUA("BlueTheme"/*legacy*/, Res.string.theme_aqua, lackDynamicColors = true),
    DARK("DarkTheme", Res.string.theme_dark, isDark = true),
    BLACK("BlackTheme", Res.string.theme_black, hasGradient = false, isDark = true);

    val isDynamicColors
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S) get() =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !lackDynamicColors
}
