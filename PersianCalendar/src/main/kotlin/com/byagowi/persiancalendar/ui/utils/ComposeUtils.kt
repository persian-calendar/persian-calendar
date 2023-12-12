package com.byagowi.persiancalendar.ui.utils

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.minusAssign
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.variants.debugAssertNotNull

fun showComposeDialog(
    activity: ComponentActivity,
    dialog: @Composable ((onDismissRequest: () -> Unit) -> Unit)
) {
    val decorView = (activity.window.decorView as? ViewGroup).debugAssertNotNull ?: return
    decorView.addView(ComposeView(activity).also { composeView ->
        composeView.setContent {
            var showDialog by remember { mutableStateOf(true) }
            if (showDialog) AppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
@Stable
fun MaterialCornerExtraLargeTop(): CornerBasedShape {
    return MaterialTheme.shapes.extraLarge.copy(
        bottomStart = ZeroCornerSize,
        bottomEnd = ZeroCornerSize,
    )
}

@Composable
@Stable
fun MaterialCornerExtraLargeNoBottomEnd(): CornerBasedShape {
    return MaterialTheme.shapes.extraLarge.copy(bottomEnd = ZeroCornerSize)
}

// When something needs to match with corner size
const val ExtraLargeShapeCornerSize = 28f

// Clickable items in settings should have this height
const val SettingsItemHeight = 48f

// Items in settings that have a radio button or checkbox should have this horizontal padding
const val SettingsHorizontalPaddingItemWithButton = 22f

// Radio button and checkbox should have this space with following text
const val SettingsHorizontalButtonItemSpacer = 12f

// Plain items in settings should have this horizontal padding
const val SettingsHorizontalPaddingItem = 24f

// Common alpha value to blend a component with it's background
const val AppBlendAlpha = .75f
