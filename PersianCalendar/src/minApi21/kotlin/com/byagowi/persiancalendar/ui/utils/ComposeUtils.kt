package com.byagowi.persiancalendar.ui.utils

import android.app.Activity
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import com.byagowi.persiancalendar.Variants.debugAssertNotNull
import com.byagowi.persiancalendar.ui.ComposeTheme

fun showComposeDialog(activity: Activity, dialog: @Composable ((closeDialog: () -> Unit) -> Unit)) {
    val decorView = (activity.window.decorView as? ViewGroup).debugAssertNotNull ?: return
    decorView.addView(ComposeView(activity).also { composeView ->
        composeView.setContent {
            val isDialogOpen = remember { mutableStateOf(true) }
            if (isDialogOpen.value) ComposeTheme { dialog { isDialogOpen.value = false } }
            else decorView.post { decorView.removeView(composeView) }
        }
    })
}
