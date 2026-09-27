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
public val fullscreenExitIcon: ImageVector
  get() {
    if (_fullscreen_exit != null) {
      return _fullscreen_exit!!
    }
    _fullscreen_exit =
      ImageVector.Builder(
          name = "fullscreen_exit",
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
            verticalLineTo(18f)
            horizontalLineTo(3f)
            verticalLineTo(16f)
            horizontalLineTo(8f)
            verticalLineToRelative(5f)
            horizontalLineTo(6f)
            close()
            moveToRelative(10f, 0f)
            verticalLineTo(16f)
            horizontalLineToRelative(5f)
            verticalLineToRelative(2f)
            horizontalLineTo(18f)
            verticalLineToRelative(3f)
            horizontalLineTo(16f)
            close()
            moveTo(3f, 8f)
            verticalLineTo(6f)
            horizontalLineTo(6f)
            verticalLineTo(3f)
            horizontalLineTo(8f)
            verticalLineTo(8f)
            horizontalLineTo(3f)
            close()
            moveTo(16f, 8f)
            verticalLineTo(3f)
            horizontalLineToRelative(2f)
            verticalLineTo(6f)
            horizontalLineToRelative(3f)
            verticalLineTo(8f)
            horizontalLineTo(16f)
            close()
          }
        }
        .build()
    return _fullscreen_exit!!
  }

private var _fullscreen_exit: ImageVector? = null
