package com.byagowi.persiancalendar.ui.utils

import android.graphics.Color
import android.os.Build
import android.view.Window
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat

/**
 * Make system bars (status and navigation bars) transparent as far as possible, also disables
 * decor view insets so we should consider the insets ourselves.
 *
 * From https://stackoverflow.com/a/76018821 with some modifications
 * Also have a look at [androidx.activity.enableEdgeToEdge] which provides the same functionality
 * but in non-gesture navigation is less immersive.
 */
fun transparentSystemBars(
    window: Window,
    isBackgroundColorLight: Boolean,
    isSurfaceColorLight: Boolean,
) {
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    insetsController.isAppearanceLightStatusBars = isBackgroundColorLight
    insetsController.isAppearanceLightNavigationBars = isSurfaceColorLight

    val isLightStatusBarAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    val isLightNavigationBarAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    // Either primary color, what we use behind above status icons, isn't light so we don't need to worry
    // about not being able to set isAppearanceLightStatusBars or let's check the sdk version so
    // we at least use isAppearanceLightStatusBars.
    val shouldStatusBarBeTransparent = !isBackgroundColorLight || isLightStatusBarAvailable

    // Either surface color, what we use behind below navigation icons, isn't light so we don't need to worry
    // about not being able to set isAppearanceLightNavigationBars or let's check the sdk version so
    // we at least use isAppearanceLightStatusBars.
    val shouldNavigationBarBeTransparent = !isSurfaceColorLight || isLightNavigationBarAvailable

    val systemUiScrim = ColorUtils.setAlphaComponent(Color.BLACK, 0x40) // 25% black
    window.statusBarColor =
        if (shouldStatusBarBeTransparent) Color.TRANSPARENT else systemUiScrim
    window.navigationBarColor =
        if (shouldNavigationBarBeTransparent) Color.TRANSPARENT else systemUiScrim
}
