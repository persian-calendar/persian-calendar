package com.byagowi.persiancalendar.ui.utils

import android.app.Activity
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import com.byagowi.persiancalendar.ui.ComposeTheme

fun showComposeDialog(activity: Activity, dialog: @Composable ((MutableState<Boolean>) -> Unit)) {
    (activity.window.decorView as? ViewGroup)?.addView(ComposeView(activity).also { composeView ->
        composeView.setContent {
            val isDialogOpen = remember { mutableStateOf(true) }
            if (!isDialogOpen.value) return@setContent
            ComposeTheme { dialog(isDialogOpen) }
        }
    })
}
