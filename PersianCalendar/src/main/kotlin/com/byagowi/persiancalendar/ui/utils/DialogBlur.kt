package com.byagowi.persiancalendar.ui.utils

import android.os.Build
import android.view.View
import android.view.ViewParent
import android.view.Window
import android.view.WindowManager
import androidx.annotation.Px
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

// This is copied from https://issuetracker.google.com/issues/296272625#comment3 (public domain)
// with modification and simplification till Compose provides a native support.
// It also follows parts of https://source.android.com/docs/core/display/window-blurs
@Composable
fun SetupDialogBlur(@Px radius: Int = 20, window: Window? = findWindow()) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || window == null ||
        window.windowManager?.isCrossWindowBlurEnabled != true
    ) return

    LaunchedEffect(window, radius) {
        window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        window.setDimAmount(.1f)
        window.attributes.blurBehindRadius = radius
        window.attributes = window.attributes
    }
}

@Composable
private fun findWindow(): Window? {
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current
    var window by remember { mutableStateOf(view.findWindow()) }
    DisposableEffect(view, lifecycleOwner) {
        val listener = object : View.OnAttachStateChangeListener, DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                window = view.findWindow()
            }

            override fun onViewAttachedToWindow(v: View) {
                window = view.findWindow()
            }

            override fun onViewDetachedFromWindow(v: View) {
                window = view.findWindow()
            }
        }
        lifecycleOwner.lifecycle.addObserver(listener)
        view.addOnAttachStateChangeListener(listener)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(listener)
            view.removeOnAttachStateChangeListener(listener)
        }
    }

    return window
}

private fun View.findWindow(): Window? =
    (this as? DialogWindowProvider ?: parent?.findDialogWindowProvider())?.window

private tailrec fun ViewParent.findDialogWindowProvider(): DialogWindowProvider? =
    this as? DialogWindowProvider ?: parent?.findDialogWindowProvider()
