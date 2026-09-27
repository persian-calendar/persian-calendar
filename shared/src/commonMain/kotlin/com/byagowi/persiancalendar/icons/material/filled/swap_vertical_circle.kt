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
public val swapVerticalCircleIcon: ImageVector
  get() {
    if (_swap_vertical_circle != null) {
      return _swap_vertical_circle!!
    }
    _swap_vertical_circle =
      ImageVector.Builder(
          name = "swap_vertical_circle",
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
            moveTo(14f, 19f)
            lineToRelative(4f, -4f)
            lineTo(16.6f, 13.6f)
            lineTo(15f, 15.15f)
            verticalLineTo(11f)
            horizontalLineTo(13f)
            verticalLineToRelative(4.15f)
            lineTo(11.4f, 13.6f)
            lineTo(10f, 15f)
            lineToRelative(4f, 4f)
            close()
            moveTo(9f, 13f)
            horizontalLineToRelative(2f)
            verticalLineTo(8.85f)
            lineToRelative(1.6f, 1.55f)
            lineTo(14f, 9f)
            lineTo(10f, 5f)
            lineTo(6f, 9f)
            lineToRelative(1.4f, 1.4f)
            lineTo(9f, 8.85f)
            verticalLineTo(13f)
            close()
            moveToRelative(3f, 9f)
            quadTo(9.93f, 22f, 8.1f, 21.21f)
            quadTo(6.28f, 20.43f, 4.93f, 19.08f)
            quadTo(3.58f, 17.73f, 2.79f, 15.9f)
            reflectiveQuadTo(2f, 12f)
            quadTo(2f, 9.92f, 2.79f, 8.1f)
            quadTo(3.58f, 6.27f, 4.93f, 4.93f)
            quadTo(6.28f, 3.57f, 8.1f, 2.79f)
            quadTo(9.93f, 2f, 12f, 2f)
            reflectiveQuadToRelative(3.9f, 0.79f)
            reflectiveQuadToRelative(3.17f, 2.14f)
            quadToRelative(1.35f, 1.35f, 2.14f, 3.17f)
            quadTo(22f, 9.92f, 22f, 12f)
            reflectiveQuadToRelative(-0.79f, 3.9f)
            reflectiveQuadToRelative(-2.14f, 3.17f)
            quadToRelative(-1.35f, 1.35f, -3.17f, 2.14f)
            reflectiveQuadTo(12f, 22f)
            close()
          }
        }
        .build()
    return _swap_vertical_circle!!
  }

private var _swap_vertical_circle: ImageVector? = null
