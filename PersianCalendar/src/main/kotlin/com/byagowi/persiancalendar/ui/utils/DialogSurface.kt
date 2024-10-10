package com.byagowi.persiancalendar.ui.utils

import android.os.Build
import android.view.View
import android.view.ViewParent
import android.view.Window
import android.view.WindowManager
import androidx.annotation.CheckResult
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

@Composable
fun DialogSurface(content: @Composable () -> Unit) {
    val containerColor = AlertDialogDefaults.containerColor
    Surface(
        shape = AlertDialogDefaults.shape,
        color = if (setupDialogBlur()) containerColor.copy(alpha = .99f) else containerColor,
        contentColor = contentColorFor(containerColor),
        tonalElevation = AlertDialogDefaults.TonalElevation,
        content = content,
    )
}

// This initially was taken from https://issuetracker.google.com/issues/296272625#comment3 (public domain)
// with modification and simplification till Compose provides a native support.
// It also follows parts of https://source.android.com/docs/core/display/window-blurs
@CheckResult
@Composable
private fun setupDialogBlur(): Boolean {
    val window = LocalView.current.findWindow()
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        window?.windowManager?.isCrossWindowBlurEnabled != true
    ) return false

    LaunchedEffect(window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        window.setDimAmount(.4f)
        window.attributes.blurBehindRadius = 30
        window.attributes = window.attributes
    }
    return true
}

private fun View.findWindow(): Window? =
    (this as? DialogWindowProvider ?: parent?.findDialogWindowProvider())?.window

private tailrec fun ViewParent.findDialogWindowProvider(): DialogWindowProvider? =
    this as? DialogWindowProvider ?: parent?.findDialogWindowProvider()
