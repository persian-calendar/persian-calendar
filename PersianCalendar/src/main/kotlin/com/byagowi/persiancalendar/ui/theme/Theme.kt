package com.byagowi.persiancalendar.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.themeadapter.material3.Mdc3Theme

//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)
//
//private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
//    secondary = PurpleGrey40,
//    tertiary = Pink40
//
//    /* Other default colors to override
//    background = Color(0xFFFFFBFE),
//    surface = Color(0xFFFFFBFE),
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onTertiary = Color.White,
//    onBackground = Color(0xFF1C1B1F),
//    onSurface = Color(0xFF1C1B1F),
//    */
//)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
// Need more work for modern and black theme who have dynamic variant
//    val context = LocalContext.current
//    val dynamicColor = Theme.isDynamicColor(context.appPrefs)
//    if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//        val darkTheme = isSystemInDarkTheme()
//        val colorScheme =
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        MaterialTheme(colorScheme = colorScheme) {
//            // Brought from: https://github.com/google/accompanist/blob/03a0a0a0/themeadapter-material3/src/main/java/com/google/accompanist/themeadapter/material3/Mdc3Theme.kt#L113-L118
//            //  We update the LocalContentColor to match our onBackground. This allows the default
//            //  content color to be more appropriate to the theme background
//            CompositionLocalProvider(
//                LocalContentColor provides MaterialTheme.colorScheme.onBackground,
//                content = content
//            )
//        }
//    } else
    Mdc3Theme { content() }
}

// Best effort theme matching system, used for widget and wallpaper configuration screen meant to
// not affected by app's internals
@Composable
fun SystemTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val isInDarkMode = isSystemInDarkTheme()
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isInDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        // Use better default scheme here
        if (isInDarkMode) darkColorScheme() else lightColorScheme()
    }
    MaterialTheme(colorScheme = colorScheme) {
        // Brought from: https://github.com/google/accompanist/blob/03a0a0a0/themeadapter-material3/src/main/java/com/google/accompanist/themeadapter/material3/Mdc3Theme.kt#L113-L118
        //  We update the LocalContentColor to match our onBackground. This allows the default
        //  content color to be more appropriate to the theme background
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            content = content
        )
    }
}
