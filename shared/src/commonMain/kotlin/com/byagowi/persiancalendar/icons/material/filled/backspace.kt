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
public val backspaceIcon: ImageVector
  get() {
    if (_backspace != null) {
      return _backspace!!
    }
    _backspace =
      ImageVector.Builder(autoMirror = true, 
          name = "backspace",
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
            moveTo(11.4f, 16f)
            lineTo(14f, 13.4f)
            lineTo(16.6f, 16f)
            lineTo(18f, 14.6f)
            lineTo(15.4f, 12f)
            lineTo(18f, 9.4f)
            lineTo(16.6f, 8f)
            lineTo(14f, 10.6f)
            lineTo(11.4f, 8f)
            lineTo(10f, 9.4f)
            lineTo(12.6f, 12f)
            lineTo(10f, 14.6f)
            lineTo(11.4f, 16f)
            close()
            moveTo(9f, 20f)
            quadTo(8.53f, 20f, 8.1f, 19.79f)
            quadTo(7.68f, 19.58f, 7.4f, 19.2f)
            lineTo(2f, 12f)
            lineTo(7.4f, 4.8f)
            quadTo(7.68f, 4.42f, 8.1f, 4.21f)
            quadTo(8.53f, 4f, 9f, 4f)
            horizontalLineTo(20f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(22f, 5.18f, 22f, 6f)
            verticalLineTo(18f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 20f)
            horizontalLineTo(9f)
            close()
          }
        }
        .build()
    return _backspace!!
  }

private var _backspace: ImageVector? = null
