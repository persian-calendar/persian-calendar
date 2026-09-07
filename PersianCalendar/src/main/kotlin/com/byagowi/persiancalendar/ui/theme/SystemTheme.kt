package com.byagowi.persiancalendar.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

// Best effort theme matching system, used for widget and wallpaper configuration activities,
// meant to be not affected by app's internals and match the system
@Composable
fun SystemTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val isInDarkMode = isSystemInDarkTheme()
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isInDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (isInDarkMode) DefaultDarkColorScheme else DefaultLightColorScheme
    }
    MaterialTheme(colorScheme = colorScheme, typography = resolveTypography()) {
        // Brought from: https://github.com/google/accompanist/blob/03a0a0a0/themeadapter-material3/src/main/java/com/google/accompanist/themeadapter/material3/Mdc3Theme.kt#L113-L118
        //  We update the LocalContentColor to match our onBackground. This allows the default
        //  content color to be more appropriate to the theme background
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground, content = content,
        )
    }
}
