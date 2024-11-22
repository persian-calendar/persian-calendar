package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.util.fastForEachIndexed
import kotlin.math.roundToInt

@Composable
fun FixedSizeHorizontalGrid(
    columnsCount: Int,
    rowsCount: Int,
    cellHeight: Float?,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content, modifier = modifier) { measurables, constraints ->
        val totalWidth = constraints.maxWidth
        val totalHeight = constraints.maxHeight
        val cellWidth = totalWidth / columnsCount.toFloat()
        val cellHeightPx = cellHeight ?: (totalHeight / rowsCount.toFloat())
        val cellsConstraints = Constraints.fixed(cellWidth.roundToInt(), cellHeightPx.roundToInt())
        layout(totalWidth, totalHeight) {
            measurables.fastForEachIndexed { cellIndex: Int, measurable: Measurable ->
                val column = cellIndex % columnsCount
                val row = cellIndex / columnsCount
                measurable.measure(cellsConstraints).placeRelative(
                    (column * cellWidth).roundToInt(),
                    (row * cellHeightPx).roundToInt(),
                )
            }
        }
    }
}
