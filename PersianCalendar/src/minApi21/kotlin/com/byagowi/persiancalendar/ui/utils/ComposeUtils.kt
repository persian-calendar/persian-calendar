package com.byagowi.persiancalendar.ui.utils

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import com.byagowi.persiancalendar.Variants.debugAssertNotNull
import com.byagowi.persiancalendar.databinding.FragmentComposeBinding
import com.google.android.material.composethemeadapter.MdcTheme

fun showComposeDialog(activity: Activity, dialog: @Composable ((closeDialog: () -> Unit) -> Unit)) {
    val decorView = (activity.window.decorView as? ViewGroup).debugAssertNotNull ?: return
    decorView.addView(ComposeView(activity).also { composeView ->
        composeView.setContent {
            val isDialogOpen = remember { mutableStateOf(true) }
            if (isDialogOpen.value) MdcTheme { dialog { isDialogOpen.value = false } }
            else decorView.post { decorView.removeView(composeView) }
        }
    })
}

private fun createComposeView(
    layoutInflater: LayoutInflater, isUpNavigation: Boolean,
    content: @Composable (setTitle: (String) -> Unit, setSubtitle: (String) -> Unit) -> Unit
): View {
    val binding = FragmentComposeBinding.inflate(layoutInflater)
    val toolbar = binding.appBar.toolbar
    val setTitle = { value: String -> toolbar.title = value }
    val setSubtitle = { value: String -> toolbar.subtitle = value }
    if (isUpNavigation) toolbar.setupUpNavigation() else toolbar.setupMenuNavigation()
    val composeView = ComposeView(layoutInflater.context)
    composeView.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
    )
    composeView.setContent { MdcTheme { Surface { content(setTitle, setSubtitle) } } }
    binding.root.addView(composeView)
    return binding.root
}

fun createUpNavigationComposeView(
    layoutInflater: LayoutInflater,
    content: @Composable (setTitle: (String) -> Unit, setSubtitle: (String) -> Unit) -> Unit
): View = createComposeView(layoutInflater, isUpNavigation = true, content)

fun createMenuNavigationComposeView(
    layoutInflater: LayoutInflater,
    content: @Composable (setTitle: (String) -> Unit, setSubtitle: (String) -> Unit) -> Unit
): View = createComposeView(layoutInflater, isUpNavigation = false, content)
