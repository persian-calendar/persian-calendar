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
public val brightness7Icon: ImageVector
  get() {
    if (_brightness_7 != null) {
      return _brightness_7!!
    }
    _brightness_7 =
      ImageVector.Builder(
          name = "brightness_7",
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
            moveTo(12f, 23.3f)
            lineTo(8.65f, 20f)
            horizontalLineTo(4f)
            verticalLineTo(15.35f)
            lineTo(0.7f, 12f)
            lineTo(4f, 8.65f)
            verticalLineTo(4f)
            horizontalLineTo(8.65f)
            lineTo(12f, 0.7f)
            lineTo(15.35f, 4f)
            horizontalLineTo(20f)
            verticalLineTo(8.65f)
            lineTo(23.3f, 12f)
            lineTo(20f, 15.35f)
            verticalLineTo(20f)
            horizontalLineTo(15.35f)
            lineTo(12f, 23.3f)
            close()
            moveToRelative(3.54f, -7.76f)
            quadTo(17f, 14.08f, 17f, 12f)
            quadTo(17f, 9.92f, 15.54f, 8.46f)
            reflectiveQuadTo(12f, 7f)
            quadTo(9.93f, 7f, 8.46f, 8.46f)
            reflectiveQuadTo(7f, 12f)
            reflectiveQuadToRelative(1.46f, 3.54f)
            reflectiveQuadTo(12f, 17f)
            reflectiveQuadToRelative(3.54f, -1.46f)
            close()
            moveTo(12f, 12f)
            close()
            moveToRelative(0f, 8.5f)
            lineTo(14.5f, 18f)
            horizontalLineTo(18f)
            verticalLineTo(14.5f)
            lineTo(20.5f, 12f)
            lineTo(18f, 9.5f)
            verticalLineTo(6f)
            horizontalLineTo(14.5f)
            lineTo(12f, 3.5f)
            lineTo(9.5f, 6f)
            horizontalLineTo(6f)
            verticalLineTo(9.5f)
            lineTo(3.5f, 12f)
            lineTo(6f, 14.5f)
            verticalLineTo(18f)
            horizontalLineTo(9.5f)
            lineTo(12f, 20.5f)
            close()
            moveTo(12f, 12f)
            close()
          }
        }
        .build()
    return _brightness_7!!
  }

private var _brightness_7: ImageVector? = null
