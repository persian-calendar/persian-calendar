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
public val socialDistanceIcon: ImageVector
  get() {
    if (_social_distance != null) {
      return _social_distance!!
    }
    _social_distance =
      ImageVector.Builder(
          name = "social_distance",
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
            moveTo(6f, 21f)
            lineTo(2f, 17f)
            lineTo(6f, 13f)
            lineToRelative(1.4f, 1.4f)
            lineTo(5.8f, 16f)
            horizontalLineTo(18.2f)
            lineTo(16.6f, 14.4f)
            lineTo(18f, 13f)
            lineToRelative(4f, 4f)
            lineToRelative(-4f, 4f)
            lineTo(16.6f, 19.6f)
            lineTo(18.2f, 18f)
            horizontalLineTo(5.8f)
            lineToRelative(1.6f, 1.6f)
            lineTo(6f, 21f)
            close()
            moveTo(2f, 11f)
            verticalLineTo(10.43f)
            quadTo(2f, 9.82f, 2.33f, 9.32f)
            reflectiveQuadTo(3.23f, 8.57f)
            quadTo(3.88f, 8.3f, 4.56f, 8.15f)
            reflectiveQuadTo(6f, 8f)
            reflectiveQuadTo(7.44f, 8.15f)
            reflectiveQuadTo(8.78f, 8.57f)
            quadToRelative(0.57f, 0.25f, 0.9f, 0.75f)
            reflectiveQuadTo(10f, 10.43f)
            verticalLineTo(11f)
            horizontalLineTo(2f)
            close()
            moveToRelative(12f, 0f)
            verticalLineTo(10.43f)
            quadToRelative(0f, -0.6f, 0.33f, -1.1f)
            reflectiveQuadToRelative(0.9f, -0.75f)
            quadTo(15.88f, 8.3f, 16.56f, 8.15f)
            reflectiveQuadTo(18f, 8f)
            reflectiveQuadToRelative(1.44f, 0.15f)
            reflectiveQuadToRelative(1.34f, 0.43f)
            quadToRelative(0.57f, 0.25f, 0.9f, 0.75f)
            reflectiveQuadTo(22f, 10.43f)
            verticalLineTo(11f)
            horizontalLineTo(14f)
            close()
            moveTo(6f, 7f)
            quadTo(5.18f, 7f, 4.59f, 6.41f)
            reflectiveQuadTo(4f, 5f)
            quadTo(4f, 4.17f, 4.59f, 3.59f)
            reflectiveQuadTo(6f, 3f)
            quadTo(6.83f, 3f, 7.41f, 3.59f)
            reflectiveQuadTo(8f, 5f)
            quadTo(8f, 5.82f, 7.41f, 6.41f)
            reflectiveQuadTo(6f, 7f)
            close()
            moveTo(18f, 7f)
            quadTo(17.18f, 7f, 16.59f, 6.41f)
            reflectiveQuadTo(16f, 5f)
            quadTo(16f, 4.17f, 16.59f, 3.59f)
            reflectiveQuadTo(18f, 3f)
            reflectiveQuadToRelative(1.41f, 0.59f)
            reflectiveQuadTo(20f, 5f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(18f, 7f)
            close()
          }
        }
        .build()
    return _social_distance!!
  }

private var _social_distance: ImageVector? = null
