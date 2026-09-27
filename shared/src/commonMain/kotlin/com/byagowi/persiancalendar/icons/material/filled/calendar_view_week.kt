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
public val calendarViewWeekIcon: ImageVector
  get() {
    if (_calendar_view_week != null) {
      return _calendar_view_week!!
    }
    _calendar_view_week =
      ImageVector.Builder(
          name = "calendar_view_week",
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
            moveTo(13.75f, 19f)
            quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
            quadTo(12.75f, 18.43f, 12.75f, 18f)
            verticalLineTo(6f)
            quadToRelative(0f, -0.43f, 0.29f, -0.71f)
            reflectiveQuadTo(13.75f, 5f)
            horizontalLineToRelative(1.38f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(16.13f, 6f)
            verticalLineTo(18f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(15.13f, 19f)
            horizontalLineTo(13.75f)
            close()
            moveTo(8.88f, 19f)
            quadTo(8.45f, 19f, 8.16f, 18.71f)
            quadTo(7.88f, 18.43f, 7.88f, 18f)
            verticalLineTo(6f)
            quadToRelative(0f, -0.43f, 0.29f, -0.71f)
            reflectiveQuadTo(8.88f, 5f)
            horizontalLineToRelative(1.38f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(11.25f, 6f)
            verticalLineTo(18f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(10.25f, 19f)
            horizontalLineTo(8.88f)
            close()
            moveTo(4f, 19f)
            quadTo(3.58f, 19f, 3.29f, 18.71f)
            quadTo(3f, 18.43f, 3f, 18f)
            verticalLineTo(6f)
            quadTo(3f, 5.57f, 3.29f, 5.29f)
            reflectiveQuadTo(4f, 5f)
            horizontalLineTo(5.38f)
            quadTo(5.8f, 5f, 6.09f, 5.29f)
            reflectiveQuadTo(6.38f, 6f)
            verticalLineTo(18f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(5.38f, 19f)
            horizontalLineTo(4f)
            close()
            moveToRelative(14.63f, 0f)
            quadTo(18.2f, 19f, 17.91f, 18.71f)
            quadTo(17.63f, 18.43f, 17.63f, 18f)
            verticalLineTo(6f)
            quadToRelative(0f, -0.43f, 0.29f, -0.71f)
            reflectiveQuadTo(18.63f, 5f)
            horizontalLineTo(20f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(21f, 6f)
            verticalLineTo(18f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(20f, 19f)
            horizontalLineTo(18.63f)
            close()
          }
        }
        .build()
    return _calendar_view_week!!
  }

private var _calendar_view_week: ImageVector? = null
