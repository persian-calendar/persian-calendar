package com.byagowi.persiancalendar.icons.material.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val grid3x3Icon: ImageVector
  get() {
    if (_grid_3x3 != null) {
      return _grid_3x3!!
    }
    _grid_3x3 =
      ImageVector.Builder(
          name = "grid_3x3",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(8f, 20f)
            verticalLineTo(16f)
            horizontalLineTo(4f)
            verticalLineTo(14f)
            horizontalLineTo(8f)
            verticalLineTo(10f)
            horizontalLineTo(4f)
            verticalLineTo(8f)
            horizontalLineTo(8f)
            verticalLineTo(4f)
            horizontalLineToRelative(2f)
            verticalLineTo(8f)
            horizontalLineToRelative(4f)
            verticalLineTo(4f)
            horizontalLineToRelative(2f)
            verticalLineTo(8f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(2f)
            horizontalLineTo(16f)
            verticalLineToRelative(4f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(2f)
            horizontalLineTo(16f)
            verticalLineToRelative(4f)
            horizontalLineTo(14f)
            verticalLineTo(16f)
            horizontalLineTo(10f)
            verticalLineToRelative(4f)
            horizontalLineTo(8f)
            close()
            moveToRelative(2f, -6f)
            horizontalLineToRelative(4f)
            verticalLineTo(10f)
            horizontalLineTo(10f)
            verticalLineToRelative(4f)
            close()
          }
        }
        .build()
    return _grid_3x3!!
  }

private var _grid_3x3: ImageVector? = null
