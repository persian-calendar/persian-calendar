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
public val calendarViewDayIcon: ImageVector
  get() {
    if (_calendar_view_day != null) {
      return _calendar_view_day!!
    }
    _calendar_view_day =
      ImageVector.Builder(
          name = "calendar_view_day",
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
            moveTo(5f, 17f)
            quadTo(4.18f, 17f, 3.59f, 16.41f)
            reflectiveQuadTo(3f, 15f)
            verticalLineTo(9f)
            quadTo(3f, 8.17f, 3.59f, 7.59f)
            reflectiveQuadTo(5f, 7f)
            horizontalLineTo(19f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(21f, 9f)
            verticalLineToRelative(6f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(19f, 17f)
            horizontalLineTo(5f)
            close()
            moveTo(5f, 15f)
            horizontalLineTo(19f)
            verticalLineTo(9f)
            horizontalLineTo(5f)
            verticalLineToRelative(6f)
            close()
            moveTo(3f, 5f)
            verticalLineTo(3f)
            horizontalLineTo(21f)
            verticalLineTo(5f)
            horizontalLineTo(3f)
            close()
            moveTo(3f, 21f)
            verticalLineTo(19f)
            horizontalLineTo(21f)
            verticalLineToRelative(2f)
            horizontalLineTo(3f)
            close()
            moveTo(5f, 9f)
            verticalLineToRelative(6f)
            verticalLineTo(9f)
            close()
          }
        }
        .build()
    return _calendar_view_day!!
  }

private var _calendar_view_day: ImageVector? = null
