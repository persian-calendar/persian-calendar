package com.byagowi.persiancalendar.icons.material.outlined

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val locationOnOutlinedIcon: ImageVector
  get() {
    if (_location_on != null) {
      return _location_on!!
    }
    _location_on =
      ImageVector.Builder(
          name = "location_on",
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
            moveTo(13.41f, 11.41f)
            quadTo(14f, 10.83f, 14f, 10f)
            quadTo(14f, 9.17f, 13.41f, 8.59f)
            reflectiveQuadTo(12f, 8f)
            reflectiveQuadTo(10.59f, 8.59f)
            reflectiveQuadTo(10f, 10f)
            reflectiveQuadToRelative(0.59f, 1.41f)
            reflectiveQuadTo(12f, 12f)
            reflectiveQuadToRelative(1.41f, -0.59f)
            close()
            moveTo(12f, 19.35f)
            quadToRelative(3.05f, -2.8f, 4.53f, -5.09f)
            quadTo(18f, 11.98f, 18f, 10.2f)
            quadTo(18f, 7.47f, 16.26f, 5.74f)
            quadTo(14.53f, 4f, 12f, 4f)
            reflectiveQuadTo(7.74f, 5.74f)
            quadTo(6f, 7.47f, 6f, 10.2f)
            quadToRelative(0f, 1.78f, 1.48f, 4.06f)
            reflectiveQuadTo(12f, 19.35f)
            close()
            moveTo(12f, 22f)
            quadTo(7.98f, 18.58f, 5.99f, 15.64f)
            reflectiveQuadTo(4f, 10.2f)
            quadTo(4f, 6.45f, 6.41f, 4.22f)
            reflectiveQuadTo(12f, 2f)
            reflectiveQuadToRelative(5.59f, 2.22f)
            reflectiveQuadTo(20f, 10.2f)
            quadToRelative(0f, 2.5f, -1.99f, 5.44f)
            quadTo(16.03f, 18.58f, 12f, 22f)
            close()
            moveTo(12f, 10f)
            close()
          }
        }
        .build()
    return _location_on!!
  }

private var _location_on: ImageVector? = null
