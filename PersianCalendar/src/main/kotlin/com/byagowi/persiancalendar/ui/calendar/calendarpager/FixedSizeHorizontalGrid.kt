package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.util.fastForEachIndexed
import kotlin.math.roundToInt

@Composable
fun FixedSizeHorizontalGrid(columnsCount: Int, rowsCount: Int, content: @Composable () -> Unit) {
    Layout(content = content) { measurables, constraints ->
        val widthPx = constraints.maxWidth
        val heightPx = constraints.maxHeight
        val cellWidthPx = widthPx / columnsCount.toFloat()
        val cellHeightPx = heightPx / rowsCount.toFloat()
        val cellsConstraints =
            Constraints.fixed(cellWidthPx.roundToInt(), cellHeightPx.roundToInt())
        layout(widthPx, heightPx) {
            measurables.fastForEachIndexed { cellIndex, measurable ->
                val column = cellIndex % columnsCount
                val row = cellIndex / columnsCount
                measurable.measure(cellsConstraints).placeRelative(
                    (column * cellWidthPx).roundToInt(),
                    (row * cellHeightPx).roundToInt(),
                )
            }
        }
    }
}
