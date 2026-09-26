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
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import com.byagowi.persiancalendar.shared.ShareFacilitator
import io.github.persiancalendar.qr.qr

@Composable
fun QrView(
    text: String,
    modifier: Modifier = Modifier,
    onShareActionChange: ((ShareFacilitator) -> Unit) -> Unit,
) {
    val qr = remember(text) { qr(text) }
    val paint = remember { Paint() }
    val contentColor by rememberUpdatedState(LocalContentColor.current)
    val path = remember { Path() }
    var isRounded by rememberSaveable { mutableStateOf(true) }
    val roundness by animateFloatAsState(if (isRounded) 1f else 0f)

    fun drawQr(canvas: Canvas, size: Float) {
        paint.color = contentColor
        val roundness = roundness
        val cells = qr.size // cells in a row or a column
        val cellSize = size / (qr.size.takeIf { it != 0 } ?: return)
        val r = roundness * cellSize / 2 * 1f
        fun on(i: Int, j: Int) =
            (i > 6 || j > 6) && (cells - i > 7 || j > 6) && (i > 6 || cells - j > 7) && qr[i][j]
        repeat(cells) { i ->
            repeat(cells) { j ->
                if (on(i, j)) canvas.drawRoundRect(
                    i * cellSize, j * cellSize, (i + 1) * cellSize, (j + 1) * cellSize,
                    r, r, paint,
                )
            }
        }
        fun lines(vertical: Boolean) = repeat(cells) { i ->
            var j = 0
            while (j < cells - 1) {
                if (if (vertical) on(i, j) else on(j, i)) {
                    var k = j + 1
                    while (k < cells && if (vertical) on(i, k) else on(k, i)) ++k
                    if (j != k) canvas.drawRect(
                        cellSize * if (vertical) i + 0f else j + .5f,
                        cellSize * if (vertical) j + .5f else i + 0f,
                        cellSize * if (vertical) i + 1f else k - .5f,
                        cellSize * if (vertical) k - .5f else i + 1f,
                        paint,
                    )
                }
                ++j
            }
        }
        lines(vertical = false)
        lines(vertical = true)
        path.rewind()
        path.addRoundRect(
            RoundRect(
                0f, 0f, cellSize * 7, cellSize * 7,
                cellSize * roundness * 2, cellSize * roundness * 2,
            ),
            Path.Direction.Clockwise,
        )
        path.addRoundRect(
            RoundRect(
                cellSize, cellSize, cellSize * 6, cellSize * 6,
                cellSize * roundness * 1.5f, cellSize * roundness * 1.5f,
            ),
            Path.Direction.CounterClockwise,
        )
        path.addRoundRect(
            RoundRect(
                cellSize * 2, cellSize * 2, cellSize * 5, cellSize * 5,
                cellSize * roundness, cellSize * roundness,
            ),
            Path.Direction.Clockwise,
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
        modifier
            .aspectRatio(1f)
            .clickable { isRounded = !isRounded },
    ) { drawQr(drawContext.canvas, size = this.size.width) }

    val surfaceColor by rememberUpdatedState(MaterialTheme.colorScheme.surface)
    LaunchedEffect(Unit) {
        onShareActionChange { shareFacilitator ->
            val size = 1280
            val imageBitmap = ImageBitmap(size, size)
            Canvas(imageBitmap).also { canvas ->
                canvas.drawRect(
                    0f, 0f, size.toFloat(), size.toFloat(),
                    Paint().also { it.color = surfaceColor },
                )
                canvas.scale(1 - 64f / size)
                drawQr(canvas, size.toFloat())
            }
            shareFacilitator.shareImageBitmap(imageBitmap)
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
