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
public val myLocationIcon: ImageVector
  get() {
    if (_my_location != null) {
      return _my_location!!
    }
    _my_location =
      ImageVector.Builder(
          name = "my_location",
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
            moveTo(11f, 22.95f)
            verticalLineToRelative(-2f)
            quadTo(7.88f, 20.6f, 5.64f, 18.36f)
            reflectiveQuadTo(3.05f, 13f)
            horizontalLineToRelative(-2f)
            verticalLineTo(11f)
            horizontalLineToRelative(2f)
            quadTo(3.4f, 7.88f, 5.64f, 5.64f)
            reflectiveQuadTo(11f, 3.05f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(2f)
            quadToRelative(3.13f, 0.35f, 5.36f, 2.59f)
            reflectiveQuadTo(20.95f, 11f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(-2f)
            quadToRelative(-0.35f, 3.13f, -2.59f, 5.36f)
            reflectiveQuadTo(13f, 20.95f)
            verticalLineToRelative(2f)
            horizontalLineTo(11f)
            close()
            moveToRelative(5.95f, -6f)
            quadTo(19f, 14.9f, 19f, 12f)
            reflectiveQuadTo(16.95f, 7.05f)
            reflectiveQuadTo(12f, 5f)
            reflectiveQuadTo(7.05f, 7.05f)
            reflectiveQuadTo(5f, 12f)
            reflectiveQuadToRelative(2.05f, 4.95f)
            reflectiveQuadTo(12f, 19f)
            reflectiveQuadToRelative(4.95f, -2.05f)
            close()
            moveTo(9.18f, 14.83f)
            quadTo(8f, 13.65f, 8f, 12f)
            reflectiveQuadTo(9.18f, 9.17f)
            reflectiveQuadTo(12f, 8f)
            reflectiveQuadToRelative(2.83f, 1.17f)
            reflectiveQuadTo(16f, 12f)
            reflectiveQuadToRelative(-1.17f, 2.82f)
            reflectiveQuadTo(12f, 16f)
            reflectiveQuadTo(9.18f, 14.83f)
            close()
          }
        }
        .build()
    return _my_location!!
  }

private var _my_location: ImageVector? = null
