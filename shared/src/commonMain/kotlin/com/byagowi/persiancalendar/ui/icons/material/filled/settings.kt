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
public val settingsIcon: ImageVector
  get() {
    if (_settings != null) {
      return _settings!!
    }
    _settings =
      ImageVector.Builder(
          name = "settings",
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
            moveTo(9.25f, 22f)
            lineTo(8.85f, 18.8f)
            quadTo(8.53f, 18.68f, 8.24f, 18.5f)
            reflectiveQuadTo(7.68f, 18.13f)
            lineTo(4.7f, 19.38f)
            lineTo(1.95f, 14.63f)
            lineTo(4.53f, 12.68f)
            quadTo(4.5f, 12.5f, 4.5f, 12.34f)
            quadToRelative(0f, -0.16f, 0f, -0.34f)
            reflectiveQuadToRelative(0f, -0.34f)
            reflectiveQuadTo(4.53f, 11.33f)
            lineTo(1.95f, 9.38f)
            lineTo(4.7f, 4.63f)
            lineTo(7.68f, 5.88f)
            quadTo(7.95f, 5.68f, 8.25f, 5.5f)
            reflectiveQuadTo(8.85f, 5.2f)
            lineTo(9.25f, 2f)
            horizontalLineToRelative(5.5f)
            lineToRelative(0.4f, 3.2f)
            quadToRelative(0.33f, 0.13f, 0.61f, 0.3f)
            reflectiveQuadToRelative(0.56f, 0.38f)
            lineTo(19.3f, 4.63f)
            lineToRelative(2.75f, 4.75f)
            lineToRelative(-2.57f, 1.95f)
            quadToRelative(0.02f, 0.18f, 0.02f, 0.34f)
            reflectiveQuadToRelative(0f, 0.34f)
            reflectiveQuadToRelative(0f, 0.34f)
            reflectiveQuadToRelative(-0.05f, 0.34f)
            lineToRelative(2.57f, 1.95f)
            lineToRelative(-2.75f, 4.75f)
            lineTo(16.33f, 18.13f)
            quadToRelative(-0.27f, 0.2f, -0.57f, 0.38f)
            reflectiveQuadToRelative(-0.6f, 0.3f)
            lineTo(14.75f, 22f)
            horizontalLineTo(9.25f)
            close()
            moveToRelative(2.8f, -6.5f)
            quadToRelative(1.45f, 0f, 2.47f, -1.03f)
            reflectiveQuadTo(15.55f, 12f)
            reflectiveQuadTo(14.53f, 9.52f)
            reflectiveQuadTo(12.05f, 8.5f)
            quadToRelative(-1.47f, 0f, -2.49f, 1.02f)
            reflectiveQuadTo(8.55f, 12f)
            reflectiveQuadToRelative(1.01f, 2.47f)
            reflectiveQuadToRelative(2.49f, 1.03f)
            close()
          }
        }
        .build()
    return _settings!!
  }

private var _settings: ImageVector? = null
