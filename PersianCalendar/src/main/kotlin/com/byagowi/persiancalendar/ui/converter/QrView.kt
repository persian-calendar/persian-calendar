package com.byagowi.persiancalendar.ui.converter

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withScale
import com.byagowi.persiancalendar.shared.drawQr
import com.byagowi.persiancalendar.ui.utils.shareBinaryFile
import com.byagowi.persiancalendar.ui.utils.toPngByteArray
import io.github.persiancalendar.qr.qr

@Composable
fun QrView(
    text: String,
    modifier: Modifier = Modifier,
    onShareActionChange: (() -> Unit) -> Unit,
) {
    val qr = remember(text) { qr(text) }

    val contentColor by rememberUpdatedState(LocalContentColor.current)

    var isRounded by rememberSaveable { mutableStateOf(true) }
    val roundness by animateFloatAsState(if (isRounded) 1f else 0f)

    Canvas(
        modifier
            .aspectRatio(1f)
            .clickable { isRounded = !isRounded },
    ) { drawQr(drawContext.canvas, this.size.width, qr, contentColor, roundness) }

    val context = LocalContext.current
    val surfaceColor by rememberUpdatedState(MaterialTheme.colorScheme.surface)
    LaunchedEffect(Unit) {
        onShareActionChange {
            val size = 1280
            val bitmap = createBitmap(size, size).applyCanvas {
                drawColor(surfaceColor.toArgb())
                withScale(1 - 64f / size, 1 - 64f / size, size / 2f, size / 2f) {
                    drawQr(Canvas(this), size.toFloat(), qr, contentColor, roundness)
                }
            }
            context.shareBinaryFile(bitmap.toPngByteArray(), "result.png", "image/png")
        }
    }
}

@Composable
@Preview
internal fun QrViewPreview() = Box(Modifier.background(Color.Black)) {
    CompositionLocalProvider(LocalContentColor provides Color.Gray) {
        QrView("https://example.com") {}
    }
}
