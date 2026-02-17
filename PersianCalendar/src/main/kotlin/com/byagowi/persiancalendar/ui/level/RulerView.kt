package com.byagowi.persiancalendar.ui.level

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastRoundToInt
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.numeral

@Composable
fun RulerView(modifier: Modifier, cmInchFlip: Boolean, isFullscreen: Boolean) {
    // xdpi/ydpi doesn't swap on screen rotation so a logic like this is needed though it isn't
    // obvious to me yet whether this is a correct one.
    val dpi = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        LocalResources.current.displayMetrics.xdpi
    } else LocalResources.current.displayMetrics.ydpi
    val textMeasurer = rememberTextMeasurer()
    val textStyle = LocalTextStyle.current.copy(
        fontSize = 12.sp * if (numeral.isArabicIndicVariants) 1.4f else 1f,
    )
    val outlineColor = MaterialTheme.colorScheme.outline
    Canvas(modifier) {
        fun ruler(gap: Float, end: Boolean, unit: String, subdivisions: Int) {
            repeat(times = (size.height / gap * subdivisions).fastRoundToInt()) {
                val y = gap * it / subdivisions
                val w = when {
                    it % subdivisions == 0 -> {
                        val textLayoutResult = textMeasurer.measure(
                            text = numeral.format(it / subdivisions) + " " + if (it == 0) unit else "",
                            style = textStyle,
                        )
                        drawText(
                            textLayoutResult = textLayoutResult,
                            color = outlineColor,
                            topLeft = Offset(
                                x = if (end) size.width - 30.dp.toPx() - textLayoutResult.size.width
                                else 30.dp.toPx(),
                                y = y - textLayoutResult.size.height * run {
                                    if (isFullscreen && it == 0) .25f else .5f
                                },
                            ),
                        )
                        25.dp
                    }

                    it % (subdivisions / 2) == 0 -> 15.dp
                    else -> 8.dp
                }.toPx()
                drawLine(
                    color = outlineColor,
                    start = Offset(x = if (end) size.width else 0f, y = y),
                    end = Offset(x = if (end) size.width - w else w, y = y),
                    strokeWidth = 1.dp.toPx(),
                )
            }
        }
        ruler(gap = dpi, end = cmInchFlip, unit = language.inch, subdivisions = 4)
        ruler(gap = dpi / 2.54f, end = !cmInchFlip, unit = language.centimeter, subdivisions = 10)
    }
}
