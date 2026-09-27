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
public val brightness4Icon: ImageVector
  get() {
    if (_brightness_4 != null) {
      return _brightness_4!!
    }
    _brightness_4 =
      ImageVector.Builder(
          name = "brightness_4",
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
            moveTo(12.3f, 17f)
            quadToRelative(2.07f, 0f, 3.54f, -1.46f)
            reflectiveQuadTo(17.3f, 12f)
            quadToRelative(0f, -2.08f, -1.46f, -3.54f)
            reflectiveQuadTo(12.3f, 7f)
            quadTo(11.75f, 7f, 11.23f, 7.11f)
            quadTo(10.7f, 7.22f, 10.2f, 7.45f)
            quadToRelative(1.35f, 0.63f, 2.14f, 1.85f)
            reflectiveQuadTo(13.13f, 12f)
            reflectiveQuadToRelative(-0.79f, 2.7f)
            reflectiveQuadTo(10.2f, 16.55f)
            quadToRelative(0.5f, 0.22f, 1.03f, 0.34f)
            reflectiveQuadTo(12.3f, 17f)
            close()
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
          }
        }
        .build()
    return _brightness_4!!
  }

private var _brightness_4: ImageVector? = null
