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
public val motorcycleIcon: ImageVector
  get() {
    if (_motorcycle != null) {
      return _motorcycle!!
    }
    _motorcycle =
      ImageVector.Builder(
          name = "motorcycle",
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
            moveTo(5f, 19f)
            quadTo(2.93f, 19f, 1.46f, 17.54f)
            reflectiveQuadTo(0f, 14f)
            reflectiveQuadTo(1.46f, 10.46f)
            reflectiveQuadTo(5f, 9f)
            horizontalLineTo(16.6f)
            lineToRelative(-2f, -2f)
            horizontalLineTo(11f)
            verticalLineTo(5f)
            horizontalLineToRelative(3.58f)
            quadToRelative(0.4f, 0f, 0.76f, 0.15f)
            reflectiveQuadToRelative(0.64f, 0.43f)
            lineToRelative(3.48f, 3.47f)
            quadTo(21.4f, 9.2f, 22.7f, 10.63f)
            reflectiveQuadTo(24f, 14f)
            quadToRelative(0f, 2.07f, -1.46f, 3.54f)
            reflectiveQuadTo(19f, 19f)
            quadToRelative(-2.07f, 0f, -3.54f, -1.46f)
            reflectiveQuadTo(14f, 14f)
            quadToRelative(0f, -0.45f, 0.06f, -0.89f)
            reflectiveQuadTo(14.3f, 12.25f)
            lineTo(11.55f, 15f)
            horizontalLineTo(9.9f)
            quadTo(9.55f, 16.75f, 8.18f, 17.88f)
            reflectiveQuadTo(5f, 19f)
            close()
            moveTo(19f, 17f)
            quadToRelative(1.25f, 0f, 2.13f, -0.88f)
            reflectiveQuadTo(22f, 14f)
            reflectiveQuadTo(21.13f, 11.88f)
            reflectiveQuadTo(19f, 11f)
            reflectiveQuadToRelative(-2.13f, 0.88f)
            reflectiveQuadTo(16f, 14f)
            reflectiveQuadToRelative(0.88f, 2.13f)
            reflectiveQuadTo(19f, 17f)
            close()
            moveTo(5f, 17f)
            quadToRelative(0.95f, 0f, 1.71f, -0.55f)
            reflectiveQuadTo(7.8f, 15f)
            horizontalLineTo(5f)
            verticalLineTo(13f)
            horizontalLineTo(7.8f)
            quadTo(7.48f, 12.1f, 6.71f, 11.55f)
            reflectiveQuadTo(5f, 11f)
            quadTo(3.75f, 11f, 2.88f, 11.88f)
            reflectiveQuadTo(2f, 14f)
            reflectiveQuadToRelative(0.88f, 2.13f)
            reflectiveQuadTo(5f, 17f)
            close()
          }
        }
        .build()
    return _motorcycle!!
  }

private var _motorcycle: ImageVector? = null
