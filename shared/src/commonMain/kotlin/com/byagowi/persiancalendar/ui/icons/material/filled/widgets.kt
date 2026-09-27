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
public val widgetsIcon: ImageVector
  get() {
    if (_widgets != null) {
      return _widgets!!
    }
    _widgets =
      ImageVector.Builder(
          name = "widgets",
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
            moveTo(16.65f, 13f)
            lineTo(11f, 7.35f)
            lineTo(16.65f, 1.7f)
            lineTo(22.3f, 7.35f)
            lineTo(16.65f, 13f)
            close()
            moveTo(3f, 11f)
            verticalLineTo(3f)
            horizontalLineToRelative(8f)
            verticalLineToRelative(8f)
            horizontalLineTo(3f)
            close()
            moveTo(13f, 21f)
            verticalLineTo(13f)
            horizontalLineToRelative(8f)
            verticalLineToRelative(8f)
            horizontalLineTo(13f)
            close()
            moveTo(3f, 21f)
            verticalLineTo(13f)
            horizontalLineToRelative(8f)
            verticalLineToRelative(8f)
            horizontalLineTo(3f)
            close()
          }
        }
        .build()
    return _widgets!!
  }

private var _widgets: ImageVector? = null
