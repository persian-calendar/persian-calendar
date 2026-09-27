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
public val deleteIcon: ImageVector
  get() {
    if (_delete != null) {
      return _delete!!
    }
    _delete =
      ImageVector.Builder(
          name = "delete",
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
            moveTo(7f, 21f)
            quadTo(6.18f, 21f, 5.59f, 20.41f)
            reflectiveQuadTo(5f, 19f)
            verticalLineTo(6f)
            horizontalLineTo(4f)
            verticalLineTo(4f)
            horizontalLineTo(9f)
            verticalLineTo(3f)
            horizontalLineToRelative(6f)
            verticalLineTo(4f)
            horizontalLineToRelative(5f)
            verticalLineTo(6f)
            horizontalLineTo(19f)
            verticalLineTo(19f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(17f, 21f)
            horizontalLineTo(7f)
            close()
            moveTo(9f, 17f)
            horizontalLineToRelative(2f)
            verticalLineTo(8f)
            horizontalLineTo(9f)
            verticalLineToRelative(9f)
            close()
            moveToRelative(4f, 0f)
            horizontalLineToRelative(2f)
            verticalLineTo(8f)
            horizontalLineTo(13f)
            verticalLineToRelative(9f)
            close()
          }
        }
        .build()
    return _delete!!
  }

private var _delete: ImageVector? = null
