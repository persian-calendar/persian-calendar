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
public val androidIcon: ImageVector
  get() {
    if (_android != null) {
      return _android!!
    }
    _android =
      ImageVector.Builder(
          name = "android",
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
            moveTo(1f, 18f)
            quadTo(1.25f, 15.38f, 2.6f, 13.23f)
            reflectiveQuadTo(6.08f, 9.73f)
            lineTo(4.15f, 6.5f)
            quadTo(3.93f, 6.15f, 4.04f, 5.75f)
            reflectiveQuadTo(4.5f, 5.15f)
            quadTo(4.85f, 4.93f, 5.25f, 5.02f)
            quadToRelative(0.4f, 0.1f, 0.6f, 0.45f)
            lineToRelative(2f, 3.33f)
            quadTo(8.8f, 8.4f, 9.84f, 8.2f)
            reflectiveQuadTo(12f, 8f)
            reflectiveQuadToRelative(2.16f, 0.2f)
            reflectiveQuadToRelative(1.99f, 0.6f)
            lineToRelative(2f, -3.33f)
            quadToRelative(0.2f, -0.35f, 0.6f, -0.45f)
            quadToRelative(0.4f, -0.1f, 0.75f, 0.13f)
            quadToRelative(0.35f, 0.2f, 0.45f, 0.6f)
            reflectiveQuadTo(19.85f, 6.5f)
            lineTo(17.93f, 9.73f)
            quadToRelative(2.13f, 1.35f, 3.48f, 3.5f)
            reflectiveQuadTo(23f, 18f)
            horizontalLineTo(1f)
            close()
            moveTo(18.01f, 14.21f)
            quadToRelative(0.04f, -0.44f, -0.24f, -0.86f)
            reflectiveQuadTo(17.1f, 12.8f)
            reflectiveQuadToRelative(-0.73f, 0.1f)
            quadTo(16.05f, 13.13f, 16f, 13.55f)
            quadToRelative(-0.05f, 0.43f, 0.23f, 0.85f)
            quadToRelative(0.27f, 0.43f, 0.69f, 0.56f)
            reflectiveQuadToRelative(0.74f, -0.09f)
            reflectiveQuadToRelative(0.36f, -0.66f)
            close()
            moveTo(7.1f, 14.95f)
            quadTo(7.5f, 14.83f, 7.78f, 14.4f)
            quadTo(8.05f, 13.98f, 8.01f, 13.54f)
            reflectiveQuadTo(7.65f, 12.88f)
            reflectiveQuadTo(6.91f, 12.79f)
            reflectiveQuadTo(6.23f, 13.35f)
            quadTo(5.95f, 13.77f, 6f, 14.2f)
            reflectiveQuadToRelative(0.38f, 0.65f)
            reflectiveQuadToRelative(0.73f, 0.1f)
            close()
          }
        }
        .build()
    return _android!!
  }

private var _android: ImageVector? = null
