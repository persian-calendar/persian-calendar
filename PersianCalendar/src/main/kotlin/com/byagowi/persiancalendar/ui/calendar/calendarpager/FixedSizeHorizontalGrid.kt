package com.byagowi.persiancalendar.ui.calendar.calendarpager

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.util.fastForEachIndexed
import kotlin.math.roundToInt

@Composable
fun FixedSizeHorizontalGrid(columnsCount: Int, rowsCount: Int, content: @Composable () -> Unit) {
    Layout(content) { measurables: List<Measurable>, constraints: Constraints ->
        val totalWidth = constraints.maxWidth
        val totalHeight = constraints.maxHeight
        val cellWidth = totalWidth / columnsCount.toFloat()
        val cellHeight = totalHeight / rowsCount.toFloat()
        val cellsConstraints = Constraints.fixed(cellWidth.roundToInt(), cellHeight.roundToInt())
        layout(totalWidth, totalHeight) {
            measurables.fastForEachIndexed { cellIndex: Int, measurable: Measurable ->
                val column = cellIndex % columnsCount
                val row = cellIndex / columnsCount
                measurable.measure(cellsConstraints).placeRelative(
                    (column * cellWidth).roundToInt(),
                    (row * cellHeight).roundToInt(),
                )
            }
        }
    }
}
