package com.byagowi.persiancalendar.ui.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import com.byagowi.persiancalendar.R
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar

class SystemBarsTransparency(activity: Activity) {
    val isPrimaryColorLight = !MaterialColors.isColorLight(
        activity.resolveColor(R.attr.colorOnAppBar)
    )
    val isSurfaceColorLight = MaterialColors.isColorLight(
        activity.resolveColor(com.google.android.material.R.attr.colorSurface)
    )
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
 * Also have a look at [com.google.android.material.internal.EdgeToEdgeUtils.applyEdgeToEdge]
 */
fun Activity.transparentSystemBars() {
    // Android 4 is hard to debug and apparently ViewCompat.setOnApplyWindowInsetsListener isn't
    // reporting any value there so let's skip and simplify
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return

    WindowCompat.setDecorFitsSystemWindows(window, false)

    val transparencyState = SystemBarsTransparency(this)

    if (transparencyState.isPrimaryColorLight || transparencyState.isSurfaceColorLight) {
        val insetsController: WindowInsetsControllerCompat =
            WindowCompat.getInsetsController(window, window.decorView)
        if (transparencyState.isPrimaryColorLight)
            insetsController.isAppearanceLightStatusBars = true
        if (transparencyState.isSurfaceColorLight)
            insetsController.isAppearanceLightNavigationBars = true
    }

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

fun Snackbar.considerSystemBarsInsets() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return // no insets tweak here either
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return // not needed in 30 >=
    view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        // Not the best way but setOnApplyWindowInsetsListener refuses to give the value
        bottomMargin = ((48 + 8) * context.resources.dp).toInt()
    }
}
