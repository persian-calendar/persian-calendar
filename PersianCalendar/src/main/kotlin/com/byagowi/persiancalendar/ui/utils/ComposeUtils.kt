package com.byagowi.persiancalendar.ui.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.minusAssign
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.databinding.ComposeScreenBinding
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.google.accompanist.themeadapter.material3.Mdc3Theme

fun showComposeDialog(
    activity: FragmentActivity,
    dialog: @Composable ((closeDialog: () -> Unit) -> Unit)
) {
    val decorView = (activity.window.decorView as? ViewGroup).debugAssertNotNull ?: return
    decorView.addView(ComposeView(activity).also { composeView ->
        composeView.setContent {
            var isDialogOpen by remember { mutableStateOf(true) }
            if (isDialogOpen) Mdc3Theme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = SolidColor(MaterialTheme.colorScheme.surface.copy(alpha = .4f)))
                ) { Box(Modifier.safeDrawingPadding()) { dialog { isDialogOpen = false } } }
            } else decorView.post { decorView -= composeView }
        }
    })
}

private fun createComposeView(
    layoutInflater: LayoutInflater, isUpNavigation: Boolean,
    content: @Composable (setTitle: (String) -> Unit, setSubtitle: (String) -> Unit) -> Unit
): View {
    val binding = ComposeScreenBinding.inflate(layoutInflater)
    val toolbar = binding.appBar.toolbar
    val setTitle = { value: String -> toolbar.title = value }
    val setSubtitle = { value: String -> toolbar.subtitle = value }
    if (isUpNavigation) toolbar.setupUpNavigation() else toolbar.setupMenuNavigation()
    binding.compose.setContent {
        Mdc3Theme {
            Surface(shape = RoundedCornerShape(24.dp)) {
                Box(modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp)) {
                    content(setTitle, setSubtitle)
                }
            }
        }
    }
    ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        binding.appBar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = insets.top
        }
        WindowInsetsCompat.CONSUMED
    }
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
