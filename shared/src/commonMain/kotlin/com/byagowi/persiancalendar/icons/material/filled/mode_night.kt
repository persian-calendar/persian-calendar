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
public val modeNightIcon: ImageVector
  get() {
    if (_mode_night != null) {
      return _mode_night!!
    }
    _mode_night =
      ImageVector.Builder(
          name = "mode_night",
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
            moveTo(9.5f, 20f)
            quadToRelative(3.33f, 0f, 5.66f, -2.34f)
            reflectiveQuadTo(17.5f, 12f)
            quadToRelative(0f, -3.33f, -2.34f, -5.66f)
            reflectiveQuadTo(9.5f, 4f)
            quadTo(9.23f, 4f, 8.98f, 4f)
            reflectiveQuadTo(8.5f, 4.05f)
            quadTo(9.93f, 5.7f, 10.71f, 7.74f)
            quadTo(11.5f, 9.77f, 11.5f, 12f)
            reflectiveQuadToRelative(-0.79f, 4.26f)
            reflectiveQuadTo(8.5f, 19.95f)
            quadTo(8.73f, 20f, 8.98f, 20f)
            reflectiveQuadTo(9.5f, 20f)
            close()
            moveToRelative(0f, 2f)
            quadTo(8.18f, 22f, 6.91f, 21.66f)
            reflectiveQuadTo(4.5f, 20.65f)
            quadTo(6.83f, 19.3f, 8.16f, 17f)
            reflectiveQuadTo(9.5f, 12f)
            reflectiveQuadTo(8.16f, 7f)
            quadTo(6.83f, 4.7f, 4.5f, 3.35f)
            quadTo(5.65f, 2.67f, 6.91f, 2.34f)
            reflectiveQuadTo(9.5f, 2f)
            quadToRelative(2.08f, 0f, 3.9f, 0.79f)
            reflectiveQuadToRelative(3.17f, 2.14f)
            quadToRelative(1.35f, 1.35f, 2.14f, 3.17f)
            quadTo(19.5f, 9.92f, 19.5f, 12f)
            reflectiveQuadToRelative(-0.79f, 3.9f)
            reflectiveQuadToRelative(-2.14f, 3.17f)
            quadToRelative(-1.35f, 1.35f, -3.17f, 2.14f)
            reflectiveQuadTo(9.5f, 22f)
            close()
            moveToRelative(2f, -10f)
            close()
          }
        }
        .build()
    return _mode_night!!
  }

private var _mode_night: ImageVector? = null
