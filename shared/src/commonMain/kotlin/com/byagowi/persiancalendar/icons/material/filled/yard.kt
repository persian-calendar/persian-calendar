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
public val yardIcon: ImageVector
  get() {
    if (_yard != null) {
      return _yard!!
    }
    _yard =
      ImageVector.Builder(
          name = "yard",
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
            moveTo(12f, 19f)
            quadToRelative(0f, -2.5f, -1.75f, -4.25f)
            reflectiveQuadTo(6f, 13f)
            quadToRelative(0f, 2.5f, 1.75f, 4.25f)
            reflectiveQuadTo(12f, 19f)
            close()
            moveToRelative(1.1f, -5.5f)
            quadToRelative(0.45f, -0.45f, 0.45f, -1.1f)
            verticalLineTo(12.25f)
            quadToRelative(0.2f, 0.15f, 0.41f, 0.22f)
            reflectiveQuadToRelative(0.49f, 0.07f)
            quadToRelative(0.65f, 0f, 1.1f, -0.45f)
            reflectiveQuadTo(16f, 11f)
            quadToRelative(0f, -0.5f, -0.24f, -0.88f)
            reflectiveQuadTo(15.1f, 9.6f)
            quadTo(15.53f, 9.45f, 15.76f, 9.07f)
            reflectiveQuadTo(16f, 8.2f)
            quadTo(16f, 7.55f, 15.55f, 7.1f)
            reflectiveQuadTo(14.45f, 6.65f)
            quadToRelative(-0.28f, 0f, -0.49f, 0.07f)
            reflectiveQuadTo(13.55f, 6.95f)
            verticalLineTo(6.8f)
            quadToRelative(0f, -0.65f, -0.45f, -1.1f)
            reflectiveQuadTo(12f, 5.25f)
            reflectiveQuadTo(10.9f, 5.7f)
            reflectiveQuadTo(10.45f, 6.8f)
            verticalLineTo(6.95f)
            quadTo(10.25f, 6.8f, 10.04f, 6.72f)
            reflectiveQuadTo(9.55f, 6.65f)
            quadTo(8.9f, 6.65f, 8.45f, 7.1f)
            reflectiveQuadTo(8f, 8.2f)
            quadTo(8f, 8.7f, 8.24f, 9.07f)
            reflectiveQuadTo(8.9f, 9.6f)
            quadTo(8.48f, 9.75f, 8.24f, 10.13f)
            reflectiveQuadTo(8f, 11f)
            quadToRelative(0f, 0.65f, 0.45f, 1.1f)
            reflectiveQuadToRelative(1.1f, 0.45f)
            quadToRelative(0.28f, 0f, 0.49f, -0.07f)
            reflectiveQuadToRelative(0.41f, -0.22f)
            verticalLineTo(12.4f)
            quadToRelative(0f, 0.65f, 0.45f, 1.1f)
            reflectiveQuadTo(12f, 13.95f)
            reflectiveQuadTo(13.1f, 13.5f)
            close()
            moveTo(10.9f, 10.71f)
            quadTo(10.45f, 10.27f, 10.45f, 9.6f)
            quadToRelative(0f, -0.65f, 0.45f, -1.1f)
            reflectiveQuadTo(12f, 8.05f)
            reflectiveQuadTo(13.1f, 8.5f)
            reflectiveQuadToRelative(0.45f, 1.1f)
            quadToRelative(0f, 0.67f, -0.45f, 1.11f)
            reflectiveQuadTo(12f, 11.15f)
            reflectiveQuadTo(10.9f, 10.71f)
            close()
            moveTo(12f, 19f)
            quadToRelative(2.5f, 0f, 4.25f, -1.75f)
            reflectiveQuadTo(18f, 13f)
            quadToRelative(-2.5f, 0f, -4.25f, 1.75f)
            reflectiveQuadTo(12f, 19f)
            close()
            moveTo(4f, 22f)
            quadTo(3.18f, 22f, 2.59f, 21.41f)
            reflectiveQuadTo(2f, 20f)
            verticalLineTo(4f)
            quadTo(2f, 3.17f, 2.59f, 2.59f)
            reflectiveQuadTo(4f, 2f)
            horizontalLineTo(20f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(22f, 4f)
            verticalLineTo(20f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 22f)
            horizontalLineTo(4f)
            close()
          }
        }
        .build()
    return _yard!!
  }

private var _yard: ImageVector? = null
