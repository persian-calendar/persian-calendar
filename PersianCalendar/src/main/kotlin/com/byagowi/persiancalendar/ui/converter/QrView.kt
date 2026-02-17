package com.byagowi.persiancalendar.ui.converter

import android.graphics.Path.Direction
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withScale
import com.byagowi.persiancalendar.ui.utils.shareBinaryFile
import com.byagowi.persiancalendar.ui.utils.toPngByteArray
import io.github.persiancalendar.qr.qr

@Composable
fun QrView(text: String, setShareAction: (() -> Unit) -> Unit) {
    val qr = remember(text) { qr(text) }
    val paint = remember { Paint() }
    paint.color = LocalContentColor.current
    val path = remember { Path() }
    var isRounded by rememberSaveable { mutableStateOf(true) }
    val roundness by animateFloatAsState(if (isRounded) 1f else 0f)

    fun drawQr(canvas: Canvas, size: Float) {
        val roundness = roundness
        val cells = qr.size // cells in a row or a column
        val cellSize = size / (qr.size.takeIf { it != 0 } ?: return)
        val r = roundness * cellSize / 2 * 1f
        repeat(cells) { i ->
            repeat(cells) { j ->
                if ((i > 6 || j > 6) && (cells - i > 7 || j > 6) && (i > 6 || cells - j > 7)) {
                    if (qr[i][j]) canvas.drawRoundRect(
                        i * cellSize, j * cellSize, (i + 1) * cellSize, (j + 1) * cellSize,
                        r, r, paint,
                    )
                }
            }
        }
        path.rewind()
        path.asAndroidPath().addRoundRect(
            0f, 0f, cellSize * 7, cellSize * 7,
            cellSize * roundness * 2, cellSize * roundness * 2,
            Direction.CW,
        )
        path.asAndroidPath().addRoundRect(
            cellSize, cellSize, cellSize * 6, cellSize * 6,
            cellSize * roundness * 1.5f, cellSize * roundness * 1.5f,
            Direction.CCW,
        )
        path.asAndroidPath().addRoundRect(
            cellSize * 2, cellSize * 2, cellSize * 5, cellSize * 5,
            cellSize * roundness, cellSize * roundness,
            Direction.CW,
        )
        canvas.drawPath(path, paint)
        val d = cellSize * (qr.size - 7)
        canvas.translate(0f, d)
        canvas.drawPath(path, paint)
        canvas.translate(d, -d)
        canvas.drawPath(path, paint)
        canvas.translate(-d, 0f)
    }
    Canvas(
        Modifier
            .aspectRatio(1f)
            .clickable { isRounded = !isRounded },
    ) { drawQr(drawContext.canvas, size = this.size.width) }

    val context = LocalContext.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    LaunchedEffect(Unit) {
        setShareAction {
            val size = 1280f
            val bitmap = createBitmap(size.toInt(), size.toInt()).applyCanvas {
                val canvas = Canvas(this)
                drawColor(surfaceColor.toArgb())
                withScale(1 - 64 / size, 1 - 64 / size, size / 2, size / 2) {
                    drawQr(canvas, size)
                }
            }
            context.shareBinaryFile(bitmap.toPngByteArray(), "result.png", "image/png")
        }
    }
}
