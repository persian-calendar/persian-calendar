package com.byagowi.persiancalendar.ui.level

import androidx.compose.foundation.Canvas
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastRoundToInt
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.numeral

@Composable
fun RulerView(modifier: Modifier, cmInchFlip: Boolean) {
    val ydpi = LocalResources.current.displayMetrics.ydpi
    val textMeasurer = rememberTextMeasurer()
    val textStyle = LocalTextStyle.current.copy(
        fontSize = 12.sp * if (numeral.isArabicIndicVariants) 1.4f else 1f,
    )
    val color = Color.Gray
    Canvas(modifier) {
        fun ruler(gap: Float, end: Boolean, unit: String, steps: Int) {
            repeat(times = (size.height / gap * steps).fastRoundToInt()) {
                val y = gap * it / steps
                val w = when {
                    it % steps == 0 -> {
                        val textLayoutResult = textMeasurer.measure(
                            text = numeral.format(it / steps) + " " + if (it == 0) unit else "",
                            style = textStyle,
                        )
                        drawText(
                            textLayoutResult = textLayoutResult,
                            color = color,
                            topLeft = Offset(
                                x = if (end) size.width - 30.dp.toPx() - textLayoutResult.size.width
                                else 30.dp.toPx(),
                                y = y - textLayoutResult.size.height / 2,
                            ),
                        )
                        25.dp
                    }

                    it % (steps / 2) == 0 -> 15.dp
                    else -> 8.dp
                }.toPx()
                val start = Offset(x = if (end) size.width else 0f, y = y)
                val end = Offset(x = if (end) size.width - w else w, y = y)
                drawLine(color = color, start = start, end = end, strokeWidth = 1.dp.toPx())
            }
        }
        ruler(gap = ydpi, end = cmInchFlip, unit = language.inch, steps = 4)
        ruler(gap = ydpi / 2.54f, end = !cmInchFlip, unit = language.centimeter, steps = 10)
    }
}
