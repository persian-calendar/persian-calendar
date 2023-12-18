package com.byagowi.persiancalendar.ui.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import com.byagowi.persiancalendar.R

class SystemBarsTransparency(context: Context) {
    val isPrimaryColorLight = !isColorLight(context.resolveColor(R.attr.colorOnAppBar))
    val isSurfaceColorLight = isColorLight(context.resolveColor(R.attr.colorSurface))
    val needsVisibleStatusBarPlaceHolder = !isPrimaryColorLight && isSurfaceColorLight

    private val isLightStatusBarAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    private val isLightNavigationBarAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    // Either primary color, what we use behind above status icons, isn't light so we don't need to worry
    // about not being able to set isAppearanceLightStatusBars or let's check the sdk version so
    // we at least use isAppearanceLightStatusBars.
    val shouldStatusBarBeTransparent = !isPrimaryColorLight || isLightStatusBarAvailable

    // Either surface color, what we use behind below navigation icons, isn't light so we don't need to worry
    // about not being able to set isAppearanceLightNavigationBars or let's check the sdk version so
    // we at least use isAppearanceLightStatusBars.
    val shouldNavigationBarBeTransparent = !isSurfaceColorLight || isLightNavigationBarAvailable
}

/**
 * Make system bars (status and navigation bars) transparent as far as possible, also disables
 * decor view insets so we should consider the insets ourselves.
 *
 * From https://stackoverflow.com/a/76018821 with some modifications
 * Also have a look at [androidx.activity.enableEdgeToEdge] which provides the same functionality
 * but in non-gesture navigation is less immersive.
 */
fun Activity.transparentSystemBars() {
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val transparencyState = SystemBarsTransparency(this)

    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    insetsController.isAppearanceLightStatusBars = transparencyState.isPrimaryColorLight
    insetsController.isAppearanceLightNavigationBars = transparencyState.isSurfaceColorLight

    val systemUiScrim = ColorUtils.setAlphaComponent(Color.BLACK, 0x40) // 25% black
    window.statusBarColor =
        if (transparencyState.shouldStatusBarBeTransparent) Color.TRANSPARENT else systemUiScrim
    window.navigationBarColor =
        if (transparencyState.shouldNavigationBarBeTransparent) Color.TRANSPARENT else systemUiScrim

    // There is a window:enforceNavigationBarContrast set to false in styles.xml as the following
    // isn't as effective in dark themes.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
    }
}
