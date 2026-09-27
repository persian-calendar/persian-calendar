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
public val translateIcon: ImageVector
  get() {
    if (_translate != null) {
      return _translate!!
    }
    _translate =
      ImageVector.Builder(
          name = "translate",
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
            moveTo(11.9f, 22f)
            lineTo(16.45f, 10f)
            horizontalLineToRelative(2.1f)
            lineTo(23.1f, 22f)
            horizontalLineTo(21f)
            lineTo(19.93f, 18.95f)
            horizontalLineTo(15.08f)
            lineTo(14f, 22f)
            horizontalLineTo(11.9f)
            close()
            moveTo(4f, 19f)
            lineTo(2.6f, 17.6f)
            lineTo(7.65f, 12.55f)
            quadToRelative(-0.88f, -0.88f, -1.59f, -2f)
            reflectiveQuadTo(4.75f, 8f)
            horizontalLineToRelative(2.1f)
            quadToRelative(0.5f, 0.97f, 1f, 1.7f)
            reflectiveQuadToRelative(1.2f, 1.45f)
            quadTo(9.88f, 10.33f, 10.76f, 8.84f)
            reflectiveQuadTo(12.1f, 6f)
            horizontalLineTo(1f)
            verticalLineTo(4f)
            horizontalLineTo(8f)
            verticalLineTo(2f)
            horizontalLineToRelative(2f)
            verticalLineTo(4f)
            horizontalLineToRelative(7f)
            verticalLineTo(6f)
            horizontalLineTo(14.1f)
            quadTo(13.58f, 7.8f, 12.53f, 9.7f)
            reflectiveQuadToRelative(-2.07f, 2.9f)
            lineToRelative(2.4f, 2.45f)
            lineTo(12.1f, 17.1f)
            lineTo(9.05f, 13.98f)
            lineTo(4f, 19f)
            close()
            moveTo(15.7f, 17.2f)
            horizontalLineToRelative(3.6f)
            lineTo(17.5f, 12.1f)
            lineToRelative(-1.8f, 5.1f)
            close()
          }
        }
        .build()
    return _translate!!
  }

private var _translate: ImageVector? = null
