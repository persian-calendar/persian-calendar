package com.byagowi.persiancalendar.ui.settings.common

import androidx.compose.animation.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.graphics.applyCanvas
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.appPrefs
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ColorPickerDialog(isBackgroundPick: Boolean, key: String, onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val initialColor = remember {
        context.appPrefs.getString(key, null)?.let(android.graphics.Color::parseColor)
            ?: if (isBackgroundPick) DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
            else DEFAULT_SELECTED_WIDGET_TEXT_COLOR
    }
    val color = remember { Animatable(Color(initialColor)) }
    AppDialog(
        title = {
            Text(
                stringResource(
                    if (isBackgroundPick) R.string.widget_background_color
                    else R.string.widget_text_color
                )
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                val colorResult = if (isBackgroundPick) "#%08X".format(
                    Locale.ENGLISH, 0xFFFFFFFF and color.value.toArgb().toLong()
                ) else "#%06X".format(Locale.ENGLISH, 0xFFFFFF and color.value.toArgb())
                context.appPrefs.edit { putString(key, colorResult) }
            }) { Text(stringResource(R.string.accept)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        onDismissRequest = onDismissRequest,
    ) {
        val coroutineScope = rememberCoroutineScope()
        Row(Modifier.padding(16.dp)) {
            Column(Modifier.weight(.6f)) {
                val colors = listOf(0xffff1744, 0xff00c853, 0xff448aff, 0xffa0a0a0).map {
                    val tint = Color(it)
                    SliderDefaults.colors().copy(
                        activeTrackColor = tint,
                        thumbColor = tint,
                        inactiveTrackColor = tint.copy(alpha = .2f),
                    )
                }
                Slider(
                    value = color.value.red,
                    onValueChange = {
                        coroutineScope.launch { color.snapTo(color.value.copy(red = it)) }
                    },
                    colors = colors[0],
                )
                Slider(
                    colors = colors[1],
                    onValueChange = {
                        coroutineScope.launch { color.snapTo(color.value.copy(green = it)) }
                    },
                    value = color.value.green,
                )
                Slider(
                    value = color.value.blue,
                    onValueChange = {
                        coroutineScope.launch { color.snapTo(color.value.copy(blue = it)) }
                    },
                    colors = colors[2]
                )
                if (isBackgroundPick) Slider(
                    value = color.value.alpha,
                    onValueChange = {
                        coroutineScope.launch { color.snapTo(color.value.copy(alpha = it)) }
                    },
                    colors = colors[3],
                )
            }
            Spacer(Modifier.width(16.dp))
            var showEditor by remember { mutableStateOf(false) }
            Box(Modifier
                .align(Alignment.CenterVertically)
                .clickable { showEditor = !showEditor }
                .weight(.4f)
                .aspectRatio(1f)
                .border(BorderStroke(1.dp, Color(0x80808080)))
                .background(Color.White)
                .clipToBounds()
                .drawBehind {
                    drawIntoCanvas { it.nativeCanvas.drawPaint(createCheckerBoard(4.dp.toPx())) }
                }
                .background(color.value)
            ) {
                if (showEditor) CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    SelectionContainer(Modifier.align(Alignment.BottomCenter)) {
                        Text(
                            "#%08X".format(Locale.ENGLISH, color.value.toArgb()),
                            color = if (color.value.isLight) Color.Black else Color.White,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            (if (isBackgroundPick) listOf(
                Color.Transparent,
                Color(0x20000000),
                Color(0x50000000),
                Color.Black,
            ) else listOf(
                Color.White,
                Color(0xFFE65100),
                Color(0xFF00796B),
                Color(0xFFFEF200),
                Color(0xFF202020),
            )).forEach { entry ->
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .clickable { coroutineScope.launch { color.animateTo(entry) } }
                        .border(BorderStroke(1.dp, Color(0x80808080)))
                        .background(Color.White)
                        .clipToBounds()
                        .drawBehind {
                            drawIntoCanvas { it.nativeCanvas.drawPaint(createCheckerBoard(4.dp.toPx())) }
                        }
                        .background(entry)
                        .size(40.dp),
                )
            }
        }
    }
}

private fun createCheckerBoard(tileSize: Float): android.graphics.Paint {
    val paint = Paint()
    val image = ImageBitmap(tileSize.roundToInt() * 2, tileSize.roundToInt() * 2)
    image.asAndroidBitmap().applyCanvas {
        val fill = Paint().also {
            it.style = PaintingStyle.Fill
            it.color = Color(0x22000000)
        }.asFrameworkPaint()
        drawRect(0f, 0f, tileSize, tileSize, fill)
        drawRect(tileSize, tileSize, tileSize * 2, tileSize * 2, fill)
    }
    paint.shader = ImageShader(image, TileMode.Repeated, TileMode.Repeated)
    return paint.asFrameworkPaint()
}
