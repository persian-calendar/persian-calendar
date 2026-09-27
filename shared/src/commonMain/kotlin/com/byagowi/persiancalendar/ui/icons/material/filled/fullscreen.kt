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
public val fullscreenIcon: ImageVector
  get() {
    if (_fullscreen != null) {
      return _fullscreen!!
    }
    _fullscreen =
      ImageVector.Builder(
          name = "fullscreen",
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
            moveTo(3f, 21f)
            verticalLineTo(16f)
            horizontalLineTo(5f)
            verticalLineToRelative(3f)
            horizontalLineTo(8f)
            verticalLineToRelative(2f)
            horizontalLineTo(3f)
            close()
            moveToRelative(13f, 0f)
            verticalLineTo(19f)
            horizontalLineToRelative(3f)
            verticalLineTo(16f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(5f)
            horizontalLineTo(16f)
            close()
            moveTo(3f, 8f)
            verticalLineTo(3f)
            horizontalLineTo(8f)
            verticalLineTo(5f)
            horizontalLineTo(5f)
            verticalLineTo(8f)
            horizontalLineTo(3f)
            close()
            moveTo(19f, 8f)
            verticalLineTo(5f)
            horizontalLineTo(16f)
            verticalLineTo(3f)
            horizontalLineToRelative(5f)
            verticalLineTo(8f)
            horizontalLineTo(19f)
            close()
          }
        }
        .build()
    return _fullscreen!!
  }

private var _fullscreen: ImageVector? = null
