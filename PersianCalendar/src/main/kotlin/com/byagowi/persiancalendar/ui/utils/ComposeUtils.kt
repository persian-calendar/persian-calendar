package com.byagowi.persiancalendar.ui.utils

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.minusAssign
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.google.accompanist.themeadapter.material3.Mdc3Theme

fun showComposeDialog(
    activity: ComponentActivity,
    dialog: @Composable ((onDismissRequest: () -> Unit) -> Unit)
) {
    val decorView = (activity.window.decorView as? ViewGroup).debugAssertNotNull ?: return
    decorView.addView(ComposeView(activity).also { composeView ->
        composeView.setContent {
            var showDialog by rememberSaveable { mutableStateOf(true) }
            if (showDialog) Mdc3Theme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = SolidColor(MaterialTheme.colorScheme.surface.copy(alpha = .4f)))
                        .safeDrawingPadding()
                ) { dialog { showDialog = false } }
            } else decorView.post { decorView -= composeView }
        }
    })
}

/**
 * As [androidx.compose.material.icons.MaterialIconDimension] isn't accessible despite saying otherwise
 */
const val MaterialIconDimension = 24f

/**
 * As Material's [androidx.compose.material3.tokens.ShapeTokens.CornerExtraLargeTop] isn't exposed and we need it frequently
 * let's build our own based on Material's
 */
@Composable
fun MaterialCornerExtraLargeTop(): CornerBasedShape {
    return MaterialTheme.shapes.extraLarge.copy(
        bottomStart = CornerSize(0),
        bottomEnd = CornerSize(0),
    )
}

// When something needs to match with corner size
const val ExtraLargeShapeCornerSize = 28f

// Common alpha value to blend a component with it's background
const val AppBlendAlpha = .75f
