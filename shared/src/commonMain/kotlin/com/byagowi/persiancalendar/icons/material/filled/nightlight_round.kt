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
public val nightlightRoundIcon: ImageVector
  get() {
    if (_nightlight_round != null) {
      return _nightlight_round!!
    }
    _nightlight_round =
      ImageVector.Builder(
          name = "nightlight_round",
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
            moveTo(14f, 22f)
            quadToRelative(-2.05f, 0f, -3.88f, -0.79f)
            quadTo(8.3f, 20.43f, 6.94f, 19.06f)
            quadTo(5.58f, 17.7f, 4.79f, 15.88f)
            reflectiveQuadTo(4f, 11.99f)
            reflectiveQuadTo(4.79f, 8.11f)
            reflectiveQuadTo(6.94f, 4.94f)
            reflectiveQuadTo(10.13f, 2.79f)
            reflectiveQuadTo(14f, 2f)
            quadToRelative(1.35f, 0f, 2.63f, 0.35f)
            reflectiveQuadToRelative(2.38f, 1f)
            quadTo(16.73f, 4.67f, 15.36f, 6.94f)
            reflectiveQuadTo(14f, 12f)
            reflectiveQuadToRelative(1.36f, 5.06f)
            reflectiveQuadTo(19f, 20.65f)
            quadToRelative(-1.1f, 0.65f, -2.38f, 1f)
            reflectiveQuadTo(14f, 22f)
            close()
            moveToRelative(0f, -2f)
            quadToRelative(0.28f, 0f, 0.53f, 0f)
            reflectiveQuadTo(15f, 19.95f)
            quadTo(13.58f, 18.3f, 12.79f, 16.26f)
            quadTo(12f, 14.23f, 12f, 12f)
            quadTo(12f, 9.77f, 12.79f, 7.74f)
            reflectiveQuadTo(15f, 4.05f)
            quadTo(14.78f, 4f, 14.53f, 4f)
            reflectiveQuadTo(14f, 4f)
            quadTo(10.68f, 4f, 8.34f, 6.34f)
            reflectiveQuadTo(6f, 12f)
            reflectiveQuadToRelative(2.34f, 5.66f)
            reflectiveQuadTo(14f, 20f)
            close()
            moveTo(12f, 12f)
            close()
          }
        }
        .build()
    return _nightlight_round!!
  }

private var _nightlight_round: ImageVector? = null
