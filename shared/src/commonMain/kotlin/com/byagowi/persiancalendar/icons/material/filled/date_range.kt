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
public val dateRangeIcon: ImageVector
  get() {
    if (_date_range != null) {
      return _date_range!!
    }
    _date_range =
      ImageVector.Builder(
          name = "date_range",
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
            moveTo(7.29f, 13.71f)
            quadTo(7f, 13.43f, 7f, 13f)
            reflectiveQuadTo(7.29f, 12.29f)
            reflectiveQuadTo(8f, 12f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(9f, 13f)
            reflectiveQuadTo(8.71f, 13.71f)
            reflectiveQuadTo(8f, 14f)
            quadTo(7.58f, 14f, 7.29f, 13.71f)
            close()
            moveToRelative(4f, 0f)
            quadTo(11f, 13.43f, 11f, 13f)
            reflectiveQuadToRelative(0.29f, -0.71f)
            reflectiveQuadTo(12f, 12f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(13f, 13f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(12f, 14f)
            reflectiveQuadTo(11.29f, 13.71f)
            close()
            moveToRelative(4f, 0f)
            quadTo(15f, 13.43f, 15f, 13f)
            reflectiveQuadToRelative(0.29f, -0.71f)
            reflectiveQuadTo(16f, 12f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(17f, 13f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(16f, 14f)
            reflectiveQuadTo(15.29f, 13.71f)
            close()
            moveTo(5f, 22f)
            quadTo(4.18f, 22f, 3.59f, 21.41f)
            reflectiveQuadTo(3f, 20f)
            verticalLineTo(6f)
            quadTo(3f, 5.18f, 3.59f, 4.59f)
            reflectiveQuadTo(5f, 4f)
            horizontalLineTo(6f)
            verticalLineTo(2f)
            horizontalLineTo(8f)
            verticalLineTo(4f)
            horizontalLineToRelative(8f)
            verticalLineTo(2f)
            horizontalLineToRelative(2f)
            verticalLineTo(4f)
            horizontalLineToRelative(1f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(21f, 5.18f, 21f, 6f)
            verticalLineTo(20f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(19f, 22f)
            horizontalLineTo(5f)
            close()
            moveTo(5f, 20f)
            horizontalLineTo(19f)
            verticalLineTo(10f)
            horizontalLineTo(5f)
            verticalLineTo(20f)
            close()
            moveTo(5f, 8f)
            horizontalLineTo(19f)
            verticalLineTo(6f)
            horizontalLineTo(5f)
            verticalLineTo(8f)
            close()
            moveTo(5f, 8f)
            verticalLineTo(6f)
            verticalLineTo(8f)
            close()
          }
        }
        .build()
    return _date_range!!
  }

private var _date_range: ImageVector? = null
