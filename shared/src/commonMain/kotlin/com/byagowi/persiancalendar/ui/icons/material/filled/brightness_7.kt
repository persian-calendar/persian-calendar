package com.byagowi.persiancalendar.ui.icons.material.filled

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
            moveTo(9.88f, 14.13f)
            quadTo(9f, 13.25f, 9f, 12f)
            reflectiveQuadTo(9.88f, 9.88f)
            reflectiveQuadTo(12f, 9f)
            reflectiveQuadToRelative(2.13f, 0.88f)
            reflectiveQuadTo(15f, 12f)
            reflectiveQuadToRelative(-0.88f, 2.13f)
            reflectiveQuadTo(12f, 15f)
            reflectiveQuadTo(9.88f, 14.13f)
            close()
          }
        }
        .build()
    return _brightness_7!!
  }

private var _brightness_7: ImageVector? = null
