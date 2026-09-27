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
public val swipeDownIcon: ImageVector
  get() {
    if (_swipe_down != null) {
      return _swipe_down!!
    }
    _swipe_down =
      ImageVector.Builder(
          name = "swipe_down",
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
            moveTo(4.5f, 15f)
            lineTo(1f, 11.5f)
            lineTo(2.05f, 10.45f)
            lineTo(3.8f, 12.2f)
            quadTo(3.65f, 11.52f, 3.58f, 10.85f)
            quadTo(3.5f, 10.17f, 3.5f, 9.5f)
            quadTo(3.5f, 7.45f, 4.18f, 5.52f)
            reflectiveQuadTo(6.13f, 2f)
            lineTo(7.2f, 3.07f)
            quadTo(6.13f, 4.47f, 5.56f, 6.11f)
            reflectiveQuadTo(5f, 9.5f)
            quadToRelative(0f, 0.65f, 0.08f, 1.29f)
            reflectiveQuadToRelative(0.25f, 1.26f)
            lineToRelative(1.63f, -1.6f)
            lineTo(8f, 11.5f)
            lineTo(4.5f, 15f)
            close()
            moveToRelative(11.95f, 5.82f)
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
          }
        }
        .build()
    return _swipe_down!!
  }

private var _swipe_down: ImageVector? = null
