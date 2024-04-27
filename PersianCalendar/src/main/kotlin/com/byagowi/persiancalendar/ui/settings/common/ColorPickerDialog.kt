package com.byagowi.persiancalendar.ui.settings.common

import android.content.res.Configuration
import androidx.compose.animation.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.theme.appColorAnimationSpec
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.preferences
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ColorPickerDialog(isBackgroundPick: Boolean, key: String, onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val color = rememberSaveable(
        saver = Saver(save = { it.value.toArgb() }, restore = { Animatable(Color(it)) })
    ) {
        val initialColor = context.preferences.getString(key, null)?.toColorInt()
            ?: if (isBackgroundPick) DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
            else DEFAULT_SELECTED_WIDGET_TEXT_COLOR
        Animatable(Color(initialColor))
    }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    AppDialog(
        title = {
            if (isLandscape) return@AppDialog
            val title =
                if (isBackgroundPick) R.string.widget_background_color else R.string.widget_text_color
            Text(stringResource(title))
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                val colorResult = if (isBackgroundPick) "#%08X".format(
                    Locale.ENGLISH, 0xFFFFFFFF and color.value.toArgb().toLong()
                ) else "#%06X".format(Locale.ENGLISH, 0xFFFFFF and color.value.toArgb())
                context.preferences.edit { putString(key, colorResult) }
            }) { Text(stringResource(R.string.accept)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        onDismissRequest = onDismissRequest,
    ) {
        val checkerBoard = with(LocalDensity.current) { createCheckerBoard(4.dp.toPx()) }
        var showEditor by rememberSaveable { mutableStateOf(false) }
        Box(
            Modifier
                .padding(vertical = if (isLandscape) 0.dp else 16.dp)
                .align(Alignment.CenterHorizontally)
                .shadow(if (isLandscape) 0.dp else 16.dp)
                .clip(MaterialTheme.shapes.large)
                .clickable { showEditor = !showEditor }
                .border(BorderStroke(1.dp, Color(0x80808080)), MaterialTheme.shapes.large)
                .background(Color.White)
                .background(checkerBoard)
                .background(color.value)
                .size(if (isLandscape) 64.dp else 120.dp),
        ) {
            if (showEditor) CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                SelectionContainer(Modifier.align(Alignment.BottomCenter)) {
                    Text(
                        "#%08X".format(Locale.ENGLISH, color.value.toArgb()),
                        color = animateColorAsState(
                            if (color.value.compositeOver(Color.White).isLight) Color.Black
                            else Color.White,
                            animationSpec = appColorAnimationSpec,
                            label = "text color",
                        ).value,
                        fontSize = if (isLandscape) MaterialTheme.typography.labelSmall.fontSize
                        else TextUnit.Unspecified,
                    )
                }
            }
        }
        val coroutineScope = rememberCoroutineScope()
        (0..if (isBackgroundPick) 3 else 2).forEach {
            Slider(
                value = when (it) {
                    0 -> color.value.red
                    1 -> color.value.green
                    2 -> color.value.blue
                    else -> color.value.alpha
                },
                onValueChange = { value ->
                    val newColor = when (it) {
                        0 -> color.value.copy(red = value)
                        1 -> color.value.copy(green = value)
                        2 -> color.value.copy(blue = value)
                        else -> color.value.copy(alpha = value)
                    }
                    coroutineScope.launch { color.snapTo(newColor) }
                },
                colors = when (it) {
                    0 -> Color(0xFFFF1744)
                    1 -> Color(0xFF00C853)
                    2 -> Color(0xFF448AFF)
                    else -> Color(0xFFA0A0A0)
                }.let { tint ->
                    SliderDefaults.colors().copy(
                        activeTrackColor = tint, thumbColor = tint,
                        inactiveTrackColor = tint.copy(alpha = .2f),
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .then(if (isLandscape) Modifier.height(36.dp) else Modifier),
            )
        }
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            (if (isBackgroundPick) backgroundColors else foregroundColors).forEach { entry ->
                Box(
                    Modifier
                        .padding(vertical = if (isLandscape) 0.dp else 16.dp, horizontal = 4.dp)
                        .shadow(if (isLandscape) 0.dp else 4.dp)
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            coroutineScope.launch {
                                color.animateTo(entry, appColorAnimationSpec)
                            }
                        }
                        .border(BorderStroke(1.dp, Color(0x80808080)), MaterialTheme.shapes.small)
                        .background(Color.White)
                        .background(checkerBoard)
                        .background(entry)
                        .size(if (isLandscape) 32.dp else 40.dp),
                )
            }
        }
    }
}

private val backgroundColors = listOf(
    Color.Transparent,
    Color(0x20000000),
    Color(0x50000000),
    Color.Black,
)

private val foregroundColors = listOf(
    Color.White,
    Color(0xFFE65100),
    Color(0xFF00796B),
    Color(0xFFFEF200),
    Color(0xFF202020),
)

// https://stackoverflow.com/a/58471997
private fun createCheckerBoard(tileSize: Float): ShaderBrush {
    val image = ImageBitmap(tileSize.roundToInt() * 2, tileSize.roundToInt() * 2)
    val canvas = Canvas(image)
    val fill = Paint().also { it.style = PaintingStyle.Fill; it.color = Color(0x22000000) }
    canvas.drawRect(0f, 0f, tileSize, tileSize, fill)
    canvas.drawRect(tileSize, tileSize, tileSize * 2, tileSize * 2, fill)
    return ShaderBrush(ImageShader(image, TileMode.Repeated, TileMode.Repeated))
}
