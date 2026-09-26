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
public val keyboardArrowDownIcon: ImageVector
  get() {
    if (_keyboard_arrow_down != null) {
      return _keyboard_arrow_down!!
    }
    _keyboard_arrow_down =
      ImageVector.Builder(
          name = "keyboard_arrow_down",
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
            moveTo(12f, 15.4f)
            lineTo(6f, 9.4f)
            lineTo(7.4f, 8f)
            lineTo(12f, 12.6f)
            lineTo(16.6f, 8f)
            lineTo(18f, 9.4f)
            lineToRelative(-6f, 6f)
            close()
          }
        }
        .build()
    return _keyboard_arrow_down!!
  }

private var _keyboard_arrow_down: ImageVector? = null
