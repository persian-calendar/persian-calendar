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
public val mapIcon: ImageVector
  get() {
    if (_map != null) {
      return _map!!
    }
    _map =
      ImageVector.Builder(
          name = "map",
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
            moveTo(15f, 21f)
            lineTo(9f, 18.9f)
            lineTo(4.35f, 20.7f)
            quadTo(3.85f, 20.9f, 3.43f, 20.59f)
            reflectiveQuadTo(3f, 19.75f)
            verticalLineToRelative(-14f)
            quadTo(3f, 5.43f, 3.19f, 5.18f)
            reflectiveQuadTo(3.7f, 4.8f)
            lineTo(9f, 3f)
            lineToRelative(6f, 2.1f)
            lineTo(19.65f, 3.3f)
            quadToRelative(0.5f, -0.2f, 0.93f, 0.11f)
            reflectiveQuadTo(21f, 4.25f)
            verticalLineToRelative(14f)
            quadToRelative(0f, 0.32f, -0.19f, 0.57f)
            reflectiveQuadTo(20.3f, 19.2f)
            lineTo(15f, 21f)
            close()
            moveTo(14f, 18.55f)
            verticalLineTo(6.85f)
            lineTo(10f, 5.45f)
            verticalLineToRelative(11.7f)
            lineToRelative(4f, 1.4f)
            close()
            moveToRelative(2f, 0f)
            lineToRelative(3f, -1f)
            verticalLineTo(5.7f)
            lineTo(16f, 6.85f)
            verticalLineToRelative(11.7f)
            close()
            moveTo(5f, 18.3f)
            lineTo(8f, 17.15f)
            verticalLineTo(5.45f)
            lineToRelative(-3f, 1f)
            verticalLineTo(18.3f)
            close()
            moveTo(16f, 6.85f)
            verticalLineToRelative(11.7f)
            verticalLineTo(6.85f)
            close()
            moveTo(8f, 5.45f)
            verticalLineToRelative(11.7f)
            verticalLineTo(5.45f)
            close()
          }
        }
        .build()
    return _map!!
  }

private var _map: ImageVector? = null
