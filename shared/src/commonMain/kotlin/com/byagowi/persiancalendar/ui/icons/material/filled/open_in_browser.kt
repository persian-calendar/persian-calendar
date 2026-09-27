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
public val openInBrowserIcon: ImageVector
  get() {
    if (_open_in_browser != null) {
      return _open_in_browser!!
    }
    _open_in_browser =
      ImageVector.Builder(
          name = "open_in_browser",
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
            moveTo(5f, 21f)
            quadTo(4.18f, 21f, 3.59f, 20.41f)
            reflectiveQuadTo(3f, 19f)
            verticalLineTo(5f)
            quadTo(3f, 4.17f, 3.59f, 3.59f)
            reflectiveQuadTo(5f, 3f)
            horizontalLineTo(19f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(21f, 5f)
            verticalLineTo(19f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(19f, 21f)
            horizontalLineTo(15f)
            verticalLineTo(19f)
            horizontalLineToRelative(4f)
            verticalLineTo(7f)
            horizontalLineTo(5f)
            verticalLineTo(19f)
            horizontalLineTo(9f)
            verticalLineToRelative(2f)
            horizontalLineTo(5f)
            close()
            moveToRelative(6f, 0f)
            verticalLineTo(14.85f)
            lineToRelative(-1.6f, 1.6f)
            lineTo(8f, 15f)
            lineToRelative(4f, -4f)
            lineToRelative(4f, 4f)
            lineToRelative(-1.4f, 1.45f)
            lineTo(13f, 14.85f)
            verticalLineTo(21f)
            horizontalLineTo(11f)
            close()
          }
        }
        .build()
    return _open_in_browser!!
  }

private var _open_in_browser: ImageVector? = null
