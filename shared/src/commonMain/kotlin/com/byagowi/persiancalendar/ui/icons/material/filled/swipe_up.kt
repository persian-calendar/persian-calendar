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
public val swipeUpIcon: ImageVector
  get() {
    if (_swipe_up != null) {
      return _swipe_up!!
    }
    _swipe_up =
      ImageVector.Builder(
          name = "swipe_up",
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
            moveTo(16.45f, 20.83f)
            quadToRelative(-0.57f, 0.2f, -1.16f, 0.19f)
            reflectiveQuadTo(14.15f, 20.73f)
            lineTo(7.6f, 17.68f)
            lineToRelative(0.45f, -1f)
            quadToRelative(0.25f, -0.5f, 0.7f, -0.81f)
            reflectiveQuadToRelative(1f, -0.36f)
            lineToRelative(1.7f, -0.13f)
            lineTo(8.65f, 7.7f)
            quadTo(8.5f, 7.3f, 8.68f, 6.94f)
            reflectiveQuadTo(9.25f, 6.43f)
            quadToRelative(0.4f, -0.15f, 0.76f, 0.02f)
            reflectiveQuadToRelative(0.51f, 0.57f)
            lineToRelative(2.4f, 6.58f)
            lineToRelative(0.95f, -0.35f)
            lineTo(12.85f, 10.43f)
            quadTo(12.7f, 10.02f, 12.88f, 9.66f)
            reflectiveQuadTo(13.45f, 9.15f)
            reflectiveQuadToRelative(0.76f, 0.03f)
            reflectiveQuadToRelative(0.51f, 0.58f)
            lineToRelative(1.02f, 2.82f)
            lineToRelative(0.93f, -0.35f)
            lineTo(16f, 10.35f)
            quadTo(15.85f, 9.95f, 16.03f, 9.59f)
            quadTo(16.2f, 9.23f, 16.6f, 9.07f)
            reflectiveQuadTo(17.36f, 9.1f)
            quadToRelative(0.36f, 0.17f, 0.51f, 0.57f)
            lineToRelative(0.68f, 1.88f)
            lineTo(19.5f, 11.2f)
            quadToRelative(-0.15f, -0.4f, 0.03f, -0.76f)
            quadTo(19.7f, 10.07f, 20.1f, 9.92f)
            reflectiveQuadToRelative(0.76f, 0.03f)
            reflectiveQuadToRelative(0.51f, 0.57f)
            lineToRelative(1.38f, 3.75f)
            quadToRelative(0.58f, 1.58f, -0.11f, 3.06f)
            quadToRelative(-0.69f, 1.49f, -2.26f, 2.06f)
            lineToRelative(-3.93f, 1.43f)
            close()
            moveTo(6.13f, 14f)
            quadTo(4.85f, 12.4f, 4.18f, 10.48f)
            reflectiveQuadTo(3.5f, 6.5f)
            quadTo(3.5f, 5.82f, 3.58f, 5.15f)
            reflectiveQuadTo(3.8f, 3.8f)
            lineTo(2.05f, 5.55f)
            lineTo(1f, 4.5f)
            lineTo(4.5f, 1f)
            lineTo(8f, 4.5f)
            lineTo(6.95f, 5.55f)
            lineTo(5.33f, 3.95f)
            quadTo(5.15f, 4.57f, 5.08f, 5.21f)
            reflectiveQuadTo(5f, 6.5f)
            quadTo(5f, 8.25f, 5.56f, 9.89f)
            reflectiveQuadTo(7.2f, 12.93f)
            lineTo(6.13f, 14f)
            close()
          }
        }
        .build()
    return _swipe_up!!
  }

private var _swipe_up: ImageVector? = null
