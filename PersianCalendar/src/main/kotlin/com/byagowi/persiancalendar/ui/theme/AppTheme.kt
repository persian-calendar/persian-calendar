package com.byagowi.persiancalendar.ui.theme

import android.content.Context
import android.graphics.Typeface
import android.os.PowerManager
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.core.text.layoutDirection
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.STORED_FONT_NAME
import com.byagowi.persiancalendar.global.customFontName
import com.byagowi.persiancalendar.global.isBoldFont
import com.byagowi.persiancalendar.global.isCyberpunk
import com.byagowi.persiancalendar.global.isGradient
import com.byagowi.persiancalendar.global.isHighTextContrastEnabled
import com.byagowi.persiancalendar.global.isRedHolidays
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.systemDarkTheme
import com.byagowi.persiancalendar.global.systemLightTheme
import com.byagowi.persiancalendar.global.userSetTheme
import com.byagowi.persiancalendar.ui.calendar.calendarpager.MonthColors
import com.byagowi.persiancalendar.ui.calendar.times.SunViewColors
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.isDynamicGrayscale
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.debugAssertNotNull
import com.byagowi.persiancalendar.utils.logException
import java.io.File

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = appColorScheme(),
        typography = resolveTypography(),
        shapes = appShapes(),
    ) {
        val contentColor by animateColor(MaterialTheme.colorScheme.onBackground)

        val language by language.collectAsState()
        val isRtl =
            language.isLessKnownRtl || language.asSystemLocale().layoutDirection == View.LAYOUT_DIRECTION_RTL
        val context = LocalContext.current
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
            LocalUriHandler provides object : UriHandler {
                override fun openUri(uri: String) = runCatching {
                    CustomTabsIntent.Builder().build().launchUrl(context, uri.toUri())
                }.getOrNull().debugAssertNotNull ?: Unit
            },
        ) {
            Box(
                Modifier
                    // Don't draw behind sides insets in landscape, we don't have any plan to use it
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                    .clipToBounds()
                    // Don't move this upper to top of the chain so .clipToBounds can be applied to it
                    .background(appBackground()),
            ) { content() }
        }
    }
}

fun resolveCustomFontPath(context: Context): File? {
    return File(context.filesDir, STORED_FONT_NAME).takeIf { it.exists() }
}

@Composable
fun resolveFontFile(): File? {
    val customFontName by customFontName.collectAsState()
    return if (customFontName != null) runCatching {
        resolveCustomFontPath(LocalContext.current)
    }.onFailure(logException).getOrNull() else null
}

@Composable
fun resolveAndroidCustomTypeface(): Typeface? {
    val fontFile = resolveFontFile()
    val isBoldFont by isBoldFont.collectAsState()
    return remember(fontFile, isBoldFont) {
        fontFile?.let(Typeface::createFromFile).let {
            if (isBoldFont) Typeface.create(it, Typeface.BOLD) else it
        }
    }
}

@Composable
fun resolveTypography(): Typography {
    val result = resolveFontFile()?.let { fontFile ->
        val typography = MaterialTheme.typography
        val font = FontFamily(Font(fontFile))
        typography.copy(
            displayLarge = typography.displayLarge.copy(fontFamily = font),
            displayMedium = typography.displayMedium.copy(fontFamily = font),
            displaySmall = typography.displaySmall.copy(fontFamily = font),

            headlineLarge = typography.headlineLarge.copy(fontFamily = font),
            headlineMedium = typography.headlineMedium.copy(fontFamily = font),
            headlineSmall = typography.headlineSmall.copy(fontFamily = font),

            titleLarge = typography.titleLarge.copy(fontFamily = font),
            titleMedium = typography.titleMedium.copy(fontFamily = font),
            titleSmall = typography.titleSmall.copy(fontFamily = font),

            bodyLarge = typography.bodyLarge.copy(fontFamily = font),
            bodyMedium = typography.bodyMedium.copy(fontFamily = font),
            bodySmall = typography.bodySmall.copy(fontFamily = font),

            labelLarge = typography.labelLarge.copy(fontFamily = font),
            labelMedium = typography.labelMedium.copy(fontFamily = font),
            labelSmall = typography.labelSmall.copy(fontFamily = font)
        )
    } ?: MaterialTheme.typography
    val isBoldFont by isBoldFont.collectAsState()
    return if (isBoldFont) result.copy(
        displayLarge = result.displayLarge.copy(fontWeight = FontWeight.Bold),
        displayMedium = result.displayMedium.copy(fontWeight = FontWeight.Bold),
        displaySmall = result.displaySmall.copy(fontWeight = FontWeight.Bold),

        headlineLarge = result.headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineMedium = result.headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = result.headlineSmall.copy(fontWeight = FontWeight.Bold),

        titleLarge = result.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = result.titleMedium.copy(fontWeight = FontWeight.Bold),
        titleSmall = result.titleSmall.copy(fontWeight = FontWeight.Bold),

        bodyLarge = result.bodyLarge.copy(fontWeight = FontWeight.Bold),
        bodyMedium = result.bodyMedium.copy(fontWeight = FontWeight.Bold),
        bodySmall = result.bodySmall.copy(fontWeight = FontWeight.Bold),

        labelLarge = result.labelLarge.copy(fontWeight = FontWeight.Bold),
        labelMedium = result.labelMedium.copy(fontWeight = FontWeight.Bold),
        labelSmall = result.labelSmall.copy(fontWeight = FontWeight.Bold)
    ) else result
}

// The app's theme after custom dark/light theme is applied
@Composable
private fun effectiveTheme(): Theme {
    val explicitlySetTheme = userSetTheme.collectAsState().value
    if (explicitlySetTheme != Theme.SYSTEM_DEFAULT) return explicitlySetTheme
    return if (isSystemInDarkTheme()) {
        if (isPowerSaveMode(LocalContext.current)) Theme.BLACK
        else systemDarkTheme.collectAsState().value
    } else systemLightTheme.collectAsState().value
}

private fun isPowerSaveMode(context: Context): Boolean =
    context.getSystemService<PowerManager>()?.isPowerSaveMode == true

@Composable
private fun appColorScheme(): ColorScheme {
    val theme = effectiveTheme()
    val context = LocalContext.current
    val isDark = theme.isDark == true
    var colorScheme = if (theme.isDynamicColors) {
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (isDark) DefaultDarkColorScheme else DefaultLightColorScheme
    // Handle black theme which is useful for OLED screens
    if (theme == Theme.BLACK) colorScheme = colorScheme.copy(
        surface = Color.Black,
        surfaceContainerLow = colorScheme.surfaceContainerLowest,
        surfaceContainer = colorScheme.surfaceContainerLowest,
        surfaceContainerHigh = colorScheme.surfaceContainerLow,
        surfaceContainerHighest = colorScheme.surfaceContainer,
    )

    val backgroundColor = if (theme.isDynamicColors) when (theme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_600))
        Theme.DARK -> Color(context.getColor(android.R.color.system_neutral1_800))
        Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_1000))
        Theme.MODERN -> colorScheme.surface
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (theme) {
        Theme.LIGHT -> Color(0xFF00695c)
        Theme.DARK -> Color(0xFF2F3133)
        Theme.BLACK -> Color.Black
        Theme.AQUA -> Color(0xFF1A237E)
        Theme.MODERN -> Color(0xFFFAFAFA)
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    return colorScheme.copy(
        background = backgroundColor,
        onBackground = if (backgroundColor.isLight) DefaultLightColorScheme.onBackground
        else DefaultDarkColorScheme.onBackground,
    )
}

@Composable
private fun appShapes(): Shapes {
    if (!BuildConfig.DEVELOPMENT) return MaterialTheme.shapes
    val isCyberpunk by isCyberpunk.collectAsState()
    return if (isCyberpunk) Shapes(
        extraSmall = CutCornerShape(MaterialTheme.shapes.extraSmall.topStart),
        small = CutCornerShape(MaterialTheme.shapes.small.topStart),
        medium = CutCornerShape(MaterialTheme.shapes.medium.topStart),
        large = CutCornerShape(MaterialTheme.shapes.large.topStart),
        extraLarge = CutCornerShape(MaterialTheme.shapes.extraLarge.topStart),
    ) else MaterialTheme.shapes
}

@Composable
fun needsScreenSurfaceDragHandle(): Boolean = when (effectiveTheme()) {
    Theme.BLACK -> true
    Theme.MODERN -> !isGradient.collectAsState().value
    else -> false
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun appTopAppBarColors(): TopAppBarColors {
    return TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
        navigationIconContentColor = LocalContentColor.current,
        actionIconContentColor = LocalContentColor.current,
        titleContentColor = LocalContentColor.current,
    )
}

@Composable
fun appSwitchColors(): SwitchColors {
    val defaultColors = SwitchDefaults.colors()
    return defaultColors.copy(
        checkedThumbColor = animateColor(defaultColors.checkedThumbColor).value,
        checkedTrackColor = animateColor(defaultColors.checkedTrackColor).value,
        checkedBorderColor = animateColor(defaultColors.checkedBorderColor).value,
        checkedIconColor = animateColor(defaultColors.checkedIconColor).value,
        uncheckedThumbColor = animateColor(defaultColors.uncheckedThumbColor).value,
        uncheckedTrackColor = animateColor(defaultColors.uncheckedTrackColor).value,
        uncheckedBorderColor = animateColor(defaultColors.uncheckedBorderColor).value,
        uncheckedIconColor = animateColor(defaultColors.uncheckedIconColor).value,
    )
}

val appColorAnimationSpec = spring<Color>(stiffness = Spring.StiffnessMediumLow)

@Composable
fun animateColor(color: Color) = animateColorAsState(color, appColorAnimationSpec, "color")

/** This is similar to what [androidx.compose.animation.Crossfade] uses */
private val crossfadeSpec = fadeIn(tween()) togetherWith fadeOut(tween())

// Our own cross fade spec where AnimatedContent() has nicer effect
// than Crossfade() (usually on non binary changes) but we need a crossfade effect also
val appCrossfadeSpec: AnimatedContentTransitionScope<*>.() -> ContentTransform = { crossfadeSpec }

// This works better than (Enter|Exit)Transition.None in days screen switches for some reason
private val noTransition = fadeIn(snap()) togetherWith fadeOut(snap())
val noTransitionSpec: AnimatedContentTransitionScope<*>.() -> ContentTransform = { noTransition }

@Composable
fun isDynamicGrayscale(): Boolean =
    effectiveTheme().isDynamicColors && LocalResources.current.isDynamicGrayscale

@Composable
private fun appBackground(): Brush {
    val backgroundColor = MaterialTheme.colorScheme.background
    val theme = effectiveTheme()
    val context = LocalContext.current
    val isGradient by isGradient.collectAsState()
    val backgroundGradientStart by animateColor(
        if (!isGradient) backgroundColor
        else if (theme.isDynamicColors) when (theme) {
            Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_500))
            Theme.DARK -> Color(context.getColor(android.R.color.system_neutral1_700))
            Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_1000))
            Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_0))
            else -> null.debugAssertNotNull ?: Color.Transparent
        } else when (theme) {
            Theme.LIGHT -> Color(0xFF00796B)
            Theme.DARK -> Color(0xFF3E4042)
            Theme.BLACK -> Color.Black
            Theme.AQUA -> Color(0xFF00838F)
            Theme.MODERN -> Color.White
            else -> null.debugAssertNotNull ?: Color.Transparent
        }
    )
    val backgroundGradientEnd by animateColor(
        if (!isGradient) backgroundColor
        else if (theme.isDynamicColors) when (theme) {
            Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_900))
            Theme.DARK -> Color(context.getColor(android.R.color.system_neutral1_900))
            Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_1000))
            Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_100))
            else -> null.debugAssertNotNull ?: Color.Transparent
        } else when (theme) {
            Theme.LIGHT -> Color(0xFF004D40)
            Theme.DARK -> Color(0xFF191C1E)
            Theme.BLACK -> Color.Black
            Theme.AQUA -> Color(0xFF1A237E)
            Theme.MODERN -> Color(0xFFE1E3E5)
            else -> null.debugAssertNotNull ?: Color.Transparent
        }
    )
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    return Brush.linearGradient(
        0f to backgroundGradientStart,
        1f to backgroundGradientEnd,
        start = Offset(if (isRtl) Float.POSITIVE_INFINITY else 0f, 0f),
        end = Offset(if (isRtl) 0f else Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )
}

@Composable
fun appMonthColors(): MonthColors {
    val contentColor = LocalContentColor.current
    val theme = effectiveTheme()
    val isRedHolidays by isRedHolidays.collectAsState()
    val context = LocalContext.current
    val colorAppointments = if (theme.isDynamicColors) when (theme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_200))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_accent1_200))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_400))
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (theme) {
        Theme.LIGHT -> Color(0xFF74BBEF)
        Theme.DARK, Theme.BLACK -> Color(0xFF74BBEF)
        Theme.AQUA -> Color(0xFF74BBEF)
        Theme.MODERN -> Color(0xFF376E9F)
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    val colorHolidays = if (theme.isDynamicColors && !isRedHolidays) when (theme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_200))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_accent1_200))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_400))
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (theme) {
        Theme.LIGHT -> Color(0xFFFF8A65)
        Theme.DARK, Theme.BLACK -> Color(0xFFE65100)
        Theme.AQUA -> Color(0xFFFF8A65)
        Theme.MODERN -> Color(0xFFE51C23)
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    val isHighTextContrastEnabled by isHighTextContrastEnabled.collectAsState()
    val holidayCircleAlpha = if (isHighTextContrastEnabled) .2f
    else if (theme.isDynamicColors && !isRedHolidays) when (theme) {
        Theme.LIGHT -> .125f
        Theme.DARK -> .1f
        Theme.BLACK -> .2f
        Theme.MODERN -> .1f
        else -> null.debugAssertNotNull ?: 0f
    } else when (theme) {
        Theme.LIGHT -> .10f
        Theme.DARK -> .10f
        Theme.BLACK -> .2f
        Theme.AQUA -> .15f
        Theme.MODERN -> .075f
        else -> null.debugAssertNotNull ?: 0f
    }
    val colorHolidaysCircle = colorHolidays.copy(alpha = holidayCircleAlpha)
    val colorCurrentDay = if (theme.isDynamicColors) when (theme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_400))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_accent1_200))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_600))
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (theme) {
        Theme.LIGHT -> Color(0xFFFF7043)
        Theme.DARK, Theme.BLACK -> Color(0xFF82B1FF)
        Theme.AQUA -> Color(0xFFFF7043)
        Theme.MODERN -> Color(0xFF42AFBF)
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    val colorEventIndicator = if (theme.isDynamicColors) when (theme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_neutral1_0))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_100))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_neutral1_1000))
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (theme) {
        Theme.LIGHT -> Color(0xFFEFF2F1)
        Theme.DARK, Theme.BLACK -> Color(0xFFE0E0E0)
        Theme.AQUA -> Color(0xFFEFF2F1)
        Theme.MODERN -> Color.Black
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    val colorTextDaySelected = if (theme.isDynamicColors) when (theme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent2_0))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_accent2_0))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_accent2_900))
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (theme) {
        Theme.LIGHT -> Color(0xFF2F3133)
        Theme.DARK -> Color(0xFF2F3133)
        Theme.BLACK -> Color.Black
        Theme.AQUA -> Color(0xFF2F3133)
        Theme.MODERN -> Color.Black
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    val indicator = if (theme.isDynamicColors) when (theme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_neutral1_800))
        Theme.DARK -> Color(context.getColor(android.R.color.system_neutral1_500))
        Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_600))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_accent2_200))
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (theme) {
        Theme.LIGHT -> Color(0xFFEFF2F1)
        Theme.DARK, Theme.BLACK -> Color(0xFFE0E0E0)
        Theme.AQUA -> Color(0xFFF5F5F5)
        Theme.MODERN -> Color(0xFFDDDEE2)
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    return MonthColors(
        contentColor = contentColor,
        appointments = colorAppointments,
        holidays = colorHolidays,
        holidaysCircle = colorHolidaysCircle,
        currentDay = colorCurrentDay,
        eventIndicator = colorEventIndicator,
        textDaySelected = colorTextDaySelected,
        indicator = indicator,
    )
}

@Composable
fun nextTimeColor(): Color {
    val theme = effectiveTheme()
    val context = LocalContext.current
    return if (theme.isDynamicColors) when (theme) {
        Theme.LIGHT, Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_500))
        else -> MaterialTheme.colorScheme.primary
    } else MaterialTheme.colorScheme.primary
}

@Composable
fun scrollShadowColor(): Color =
    animateColor(Color(if (effectiveTheme().isDark == true) 0x38FFFFFF else 0x38000000)).value

@Composable
fun appSunViewColors(): SunViewColors {
    val theme = effectiveTheme()
    val context = LocalContext.current
    var nightColor = ContextCompat.getColor(
        context,
        if (theme.isDynamicColors) R.color.sun_view_dynamic_night_color else R.color.sun_view_night_color
    )
    var dayColor = ContextCompat.getColor(
        context,
        if (theme.isDynamicColors) R.color.sun_view_dynamic_day_color else R.color.sun_view_day_color
    )
    var midDayColor = ContextCompat.getColor(
        context,
        if (theme.isDynamicColors) R.color.sun_view_dynamic_midday_color else R.color.sun_view_midday_color
    )
    if (theme == Theme.BLACK && theme.isDynamicColors) {
        nightColor = ContextCompat.getColor(context, android.R.color.system_accent1_900)
        dayColor = ContextCompat.getColor(context, android.R.color.system_accent1_800)
        midDayColor = ContextCompat.getColor(context, android.R.color.system_accent1_600)
    }
    return SunViewColors(
        nightColor = nightColor,
        dayColor = dayColor,
        middayColor = midDayColor,
        sunriseTextColor = 0xFFFF9800.toInt(),
        middayTextColor = 0xFFFFC107.toInt(),
        sunsetTextColor = 0xFFF22424.toInt(),
        textColorSecondary = LocalContentColor.current.copy(alpha = AppBlendAlpha).toArgb(),
        linesColor = 0x60888888,
    )
}
