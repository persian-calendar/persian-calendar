package com.byagowi.persiancalendar.ui.theme

import android.os.Build
import android.view.View
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.text.layoutDirection
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.global.isCyberpunk
import com.byagowi.persiancalendar.global.isGradient
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.theme
import com.byagowi.persiancalendar.ui.utils.getActivity
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.ui.utils.transparentSystemBars
import com.byagowi.persiancalendar.variants.debugAssertNotNull

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColorScheme(), shapes = AppShapes()) {
        val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
        val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
        val context = LocalContext.current
        LaunchedEffect(isBackgroundColorLight, isSurfaceColorLight) {
            context.getActivity()?.window?.let {
                transparentSystemBars(it, isBackgroundColorLight, isSurfaceColorLight)
            }
        }

        val contentColor by animateColorAsState(
            MaterialTheme.colorScheme.onBackground,
            animationSpec = colorAnimationSpec,
            label = "content color"
        )

        val language by language.collectAsState()
        val isRtl =
            language.isLessKnownRtl || language.asSystemLocale().layoutDirection == View.LAYOUT_DIRECTION_RTL
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
        ) {
            // Don't draw behind sides insets in landscape, we don't have any plan for using that space
            val sidesInsets =
                WindowInsets.systemBars.only(WindowInsetsSides.Start + WindowInsetsSides.End)
            Box(
                Modifier
                    .windowInsetsPadding(sidesInsets)
                    .clipToBounds()
                    // Don't move this upper to top of the chain so clip to bound can be applied to it
                    .background(AppBackground()),
            ) { content() }
        }
    }
}

@Composable
private fun AppColorScheme(): ColorScheme {
    val theme by theme.collectAsState()
    val context = LocalContext.current
    val systemInDarkTheme = isSystemInDarkTheme()
    val darkTheme = theme.isDark || (theme == Theme.SYSTEM_DEFAULT && systemInDarkTheme)
    val hasDynamicColors = theme.hasDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    var colorScheme = if (hasDynamicColors) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (darkTheme) DarkColorScheme else LightColorScheme
    // Handle black theme which is useful for OLED screens
    if (theme == Theme.BLACK) colorScheme = colorScheme.copy(surface = Color.Black)

    val resolvedTheme =
        if (theme != Theme.SYSTEM_DEFAULT) theme else if (isSystemInDarkTheme()) Theme.DARK else Theme.LIGHT
    val backgroundColor = if (hasDynamicColors) when (resolvedTheme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_600))
        Theme.DARK -> Color(context.getColor(android.R.color.system_neutral1_800))
        Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_1000))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_10))
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (resolvedTheme) {
        Theme.LIGHT -> Color(0xFF00695c)
        Theme.DARK -> Color(0xFF2F3133)
        Theme.BLACK -> Color.Black
        Theme.AQUA -> Color(0xFF1A237E)
        Theme.MODERN -> Color(0xFFFAFAFA)
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    return colorScheme.copy(
        background = backgroundColor,
        onBackground = if (backgroundColor.isLight) Color.Black else Color.White,
    )
}

@Composable
private fun AppShapes(): Shapes {
    if (!BuildConfig.DEVELOPMENT) return MaterialTheme.shapes
    val isCyberpunk by isCyberpunk.collectAsState()
    return if (isCyberpunk) Shapes(
        extraSmall = CutCornerShape(4.dp),
        small = CutCornerShape(8.dp),
        medium = CutCornerShape(12.dp),
        large = CutCornerShape(16.dp),
        extraLarge = CutCornerShape(28.dp),
    ) else MaterialTheme.shapes
}

private val colorAnimationSpec = spring<Color>(stiffness = Spring.StiffnessLow)

@Composable
private fun AppBackground(): Brush {
    val backgroundColor = MaterialTheme.colorScheme.background
    val theme by theme.collectAsState()
    val hasDynamicColors = theme.hasDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current
    val resolvedTheme =
        if (theme != Theme.SYSTEM_DEFAULT) theme else if (isSystemInDarkTheme()) Theme.DARK else Theme.LIGHT
    val isGradient by isGradient.collectAsState()
    val backgroundGradientStart by animateColorAsState(
        if (!isGradient) backgroundColor
        else if (hasDynamicColors) when (resolvedTheme) {
            Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_500))
            Theme.DARK -> Color(context.getColor(android.R.color.system_neutral1_700))
            Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_1000))
            Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_0))
            else -> null.debugAssertNotNull ?: Color.Transparent
        } else when (resolvedTheme) {
            Theme.LIGHT -> Color(0xFF00796B)
            Theme.DARK -> Color(0xFF3E4042)
            Theme.BLACK -> Color.Black
            Theme.AQUA -> Color(0xFF00838F)
            Theme.MODERN -> Color.White
            else -> null.debugAssertNotNull ?: Color.Transparent
        },
        label = "gradient start color",
        animationSpec = colorAnimationSpec,
    )
    val backgroundGradientEnd by animateColorAsState(
        if (!isGradient) backgroundColor
        else if (hasDynamicColors) when (resolvedTheme) {
            Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_900))
            Theme.DARK -> Color(context.getColor(android.R.color.system_neutral1_900))
            Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_1000))
            Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_100))
            else -> null.debugAssertNotNull ?: Color.Transparent
        } else when (resolvedTheme) {
            Theme.LIGHT -> Color(0xFF004D40)
            Theme.DARK -> Color(0xFF191C1E)
            Theme.BLACK -> Color.Black
            Theme.AQUA -> Color(0xFF1A237E)
            Theme.MODERN -> Color(0xFFE1E3E5)
            else -> null.debugAssertNotNull ?: Color.Transparent
        },
        label = "gradient end color",
        animationSpec = colorAnimationSpec,
    )
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    return Brush.linearGradient(
        0f to backgroundGradientStart,
        1f to backgroundGradientEnd,
        start = Offset(if (isRtl) Float.POSITIVE_INFINITY else 0f, 0f),
        end = Offset(if (isRtl) 0f else Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )
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
        if (isInDarkMode) DarkColorScheme else LightColorScheme
    }
    MaterialTheme(colorScheme = colorScheme) {
        // Brought from: https://github.com/google/accompanist/blob/03a0a0a0/themeadapter-material3/src/main/java/com/google/accompanist/themeadapter/material3/Mdc3Theme.kt#L113-L118
        //  We update the LocalContentColor to match our onBackground. This allows the default
        //  content color to be more appropriate to the theme background
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground, content = content
        )
    }
}

// These two color schemes are taken from Android 14, they are in sRGB color space
private val DarkColorScheme = darkColorScheme(
    primary = Color(0.4627451f, 0.81960785f, 1.0f, 1.0f),
    onPrimary = Color(0.0f, 0.20784314f, 0.28627452f, 1.0f),
    primaryContainer = Color(0.0f, 0.29803923f, 0.4117647f, 1.0f),
    onPrimaryContainer = Color(0.75686276f, 0.9098039f, 1.0f, 1.0f),
    inversePrimary = Color(0.0f, 0.4f, 0.54509807f, 1.0f),
    secondary = Color(0.70980394f, 0.7921569f, 0.84313726f, 1.0f),
    onSecondary = Color(0.1254902f, 0.2f, 0.23921569f, 1.0f),
    secondaryContainer = Color(0.21568628f, 0.28627452f, 0.33333334f, 1.0f),
    onSecondaryContainer = Color(0.81960785f, 0.8980392f, 0.95686275f, 1.0f),
    tertiary = Color(0.7921569f, 0.75686276f, 0.91764706f, 1.0f),
    onTertiary = Color(0.19607843f, 0.17254902f, 0.29803923f, 1.0f),
    tertiaryContainer = Color(0.28235295f, 0.25882354f, 0.39215687f, 1.0f),
    onTertiaryContainer = Color(0.9019608f, 0.87058824f, 1.0f, 1.0f),
    background = Color(0.09803922f, 0.10980392f, 0.11764706f, 1.0f),
    onBackground = Color(0.88235295f, 0.8901961f, 0.8980392f, 1.0f),
    surface = Color(0.09803922f, 0.10980392f, 0.11764706f, 1.0f),
    onSurface = Color(0.88235295f, 0.8901961f, 0.8980392f, 1.0f),
    surfaceVariant = Color(0.2509804f, 0.28235295f, 0.3019608f, 1.0f),
    onSurfaceVariant = Color(0.7529412f, 0.78039217f, 0.8039216f, 1.0f),
    surfaceTint = Color(0.4627451f, 0.81960785f, 1.0f, 1.0f),
    inverseSurface = Color(0.88235295f, 0.8901961f, 0.8980392f, 1.0f),
    inverseOnSurface = Color(0.18039216f, 0.19215687f, 0.2f, 1.0f),
    error = Color(0.9490196f, 0.72156864f, 0.70980394f, 1.0f),
    onError = Color(0.3764706f, 0.078431375f, 0.0627451f, 1.0f),
    errorContainer = Color(0.54901963f, 0.11372549f, 0.09411765f, 1.0f),
    onErrorContainer = Color(0.9764706f, 0.87058824f, 0.8627451f, 1.0f),
    outline = Color(0.5411765f, 0.57254905f, 0.5921569f, 1.0f),
    outlineVariant = Color(0.28627452f, 0.27058825f, 0.30980393f, 1.0f),
    scrim = Color(0.0f, 0.0f, 0.0f, 1.0f),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0.0f, 0.4f, 0.54509807f, 1.0f),
    onPrimary = Color(1.0f, 1.0f, 1.0f, 1.0f),
    primaryContainer = Color(0.75686276f, 0.9098039f, 1.0f, 1.0f),
    onPrimaryContainer = Color(0.0f, 0.11764706f, 0.17254902f, 1.0f),
    inversePrimary = Color(0.4627451f, 0.81960785f, 1.0f, 1.0f),
    secondary = Color(0.30588236f, 0.38039216f, 0.42352942f, 1.0f),
    onSecondary = Color(1.0f, 1.0f, 1.0f, 1.0f),
    secondaryContainer = Color(0.81960785f, 0.8980392f, 0.95686275f, 1.0f),
    onSecondaryContainer = Color(0.03529412f, 0.11764706f, 0.15686275f, 1.0f),
    tertiary = Color(0.3764706f, 0.3529412f, 0.4862745f, 1.0f),
    onTertiary = Color(1.0f, 1.0f, 1.0f, 1.0f),
    tertiaryContainer = Color(0.9019608f, 0.87058824f, 1.0f, 1.0f),
    onTertiaryContainer = Color(0.11372549f, 0.09019608f, 0.21176471f, 1.0f),
    background = Color(0.9882353f, 0.9882353f, 1.0f, 1.0f),
    onBackground = Color(0.09803922f, 0.10980392f, 0.11764706f, 1.0f),
    surface = Color(0.9882353f, 0.9882353f, 1.0f, 1.0f),
    onSurface = Color(0.09803922f, 0.10980392f, 0.11764706f, 1.0f),
    surfaceVariant = Color(0.8627451f, 0.8901961f, 0.9137255f, 1.0f),
    onSurfaceVariant = Color(0.2509804f, 0.28235295f, 0.3019608f, 1.0f),
    surfaceTint = Color(0.0f, 0.4f, 0.54509807f, 1.0f),
    inverseSurface = Color(0.18039216f, 0.19215687f, 0.2f, 1.0f),
    inverseOnSurface = Color(0.9411765f, 0.9411765f, 0.9529412f, 1.0f),
    error = Color(0.7019608f, 0.14901961f, 0.11764706f, 1.0f),
    onError = Color(1.0f, 1.0f, 1.0f, 1.0f),
    errorContainer = Color(0.9764706f, 0.87058824f, 0.8627451f, 1.0f),
    onErrorContainer = Color(0.25490198f, 0.05490196f, 0.043137256f, 1.0f),
    outline = Color(0.4392157f, 0.46666667f, 0.4862745f, 1.0f),
    outlineVariant = Color(0.7921569f, 0.76862746f, 0.8156863f, 1.0f),
    scrim = Color(0.0f, 0.0f, 0.0f, 1.0f),
)
