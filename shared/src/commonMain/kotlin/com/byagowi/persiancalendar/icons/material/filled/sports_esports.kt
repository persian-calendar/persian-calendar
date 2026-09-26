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
public val sportsEsportsIcon: ImageVector
  get() {
    if (_sports_esports != null) {
      return _sports_esports!!
    }
    _sports_esports =
      ImageVector.Builder(
          name = "sports_esports",
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
            moveTo(4.55f, 19f)
            quadTo(3.28f, 19f, 2.58f, 18.11f)
            reflectiveQuadTo(2.05f, 15.95f)
            lineTo(3.1f, 8.45f)
            quadTo(3.33f, 6.95f, 4.44f, 5.97f)
            reflectiveQuadTo(7.05f, 5f)
            horizontalLineToRelative(9.9f)
            quadToRelative(1.5f, 0f, 2.61f, 0.97f)
            reflectiveQuadTo(20.9f, 8.45f)
            lineToRelative(1.05f, 7.5f)
            quadToRelative(0.17f, 1.28f, -0.53f, 2.16f)
            reflectiveQuadTo(19.45f, 19f)
            quadToRelative(-0.53f, 0f, -0.98f, -0.19f)
            reflectiveQuadTo(17.65f, 18.25f)
            lineTo(15.4f, 16f)
            horizontalLineTo(8.6f)
            lineTo(6.35f, 18.25f)
            quadTo(5.98f, 18.63f, 5.53f, 18.81f)
            reflectiveQuadTo(4.55f, 19f)
            close()
            moveToRelative(0.4f, -2.15f)
            lineTo(7.8f, 14f)
            horizontalLineToRelative(8.4f)
            lineToRelative(2.85f, 2.85f)
            quadTo(19.1f, 16.9f, 19.45f, 17f)
            quadToRelative(0.27f, 0f, 0.44f, -0.16f)
            quadTo(20.05f, 16.68f, 20f, 16.4f)
            lineTo(18.9f, 8.7f)
            quadTo(18.8f, 7.97f, 18.25f, 7.49f)
            reflectiveQuadTo(16.95f, 7f)
            horizontalLineTo(7.05f)
            quadTo(6.3f, 7f, 5.75f, 7.49f)
            quadTo(5.2f, 7.97f, 5.1f, 8.7f)
            lineTo(4f, 16.4f)
            quadToRelative(-0.05f, 0.28f, 0.11f, 0.44f)
            quadTo(4.28f, 17f, 4.55f, 17f)
            quadToRelative(0.05f, 0f, 0.4f, -0.15f)
            close()
            moveTo(17.71f, 12.71f)
            quadTo(18f, 12.43f, 18f, 12f)
            reflectiveQuadTo(17.71f, 11.29f)
            reflectiveQuadTo(17f, 11f)
            reflectiveQuadToRelative(-0.71f, 0.29f)
            reflectiveQuadTo(16f, 12f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            reflectiveQuadTo(17f, 13f)
            reflectiveQuadToRelative(0.71f, -0.29f)
            close()
            moveToRelative(-2f, -3f)
            quadTo(16f, 9.42f, 16f, 9f)
            quadTo(16f, 8.57f, 15.71f, 8.29f)
            reflectiveQuadTo(15f, 8f)
            reflectiveQuadTo(14.29f, 8.29f)
            reflectiveQuadTo(14f, 9f)
            quadToRelative(0f, 0.42f, 0.29f, 0.71f)
            reflectiveQuadTo(15f, 10f)
            reflectiveQuadTo(15.71f, 9.71f)
            close()
            moveTo(7.75f, 13f)
            horizontalLineToRelative(1.5f)
            verticalLineTo(11.25f)
            horizontalLineTo(11f)
            verticalLineTo(9.75f)
            horizontalLineTo(9.25f)
            verticalLineTo(8f)
            horizontalLineTo(7.75f)
            verticalLineTo(9.75f)
            horizontalLineTo(6f)
            verticalLineToRelative(1.5f)
            horizontalLineTo(7.75f)
            verticalLineTo(13f)
            close()
            moveTo(12f, 12f)
            close()
          }
        }
        .build()
    return _sports_esports!!
  }

private var _sports_esports: ImageVector? = null
