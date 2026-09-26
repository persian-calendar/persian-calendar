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
public val _3dRotationIcon: ImageVector
  get() {
    if (_3d_rotation != null) {
      return _3d_rotation!!
    }
    _3d_rotation =
      ImageVector.Builder(
          name = "3d_rotation",
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
            moveTo(12f, 22f)
            quadTo(9.93f, 22f, 8.1f, 21.21f)
            quadTo(6.28f, 20.43f, 4.93f, 19.08f)
            quadTo(3.58f, 17.73f, 2.79f, 15.9f)
            reflectiveQuadTo(2f, 12f)
            horizontalLineTo(4f)
            quadToRelative(0f, 2.88f, 1.81f, 5.07f)
            reflectiveQuadToRelative(4.64f, 2.78f)
            lineTo(9f, 18.4f)
            lineTo(10.4f, 17f)
            lineToRelative(4.55f, 4.55f)
            quadTo(14.23f, 21.8f, 13.49f, 21.9f)
            reflectiveQuadTo(12f, 22f)
            close()
            moveToRelative(0.5f, -7f)
            verticalLineTo(9f)
            horizontalLineToRelative(3f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(16.5f, 10f)
            verticalLineToRelative(4f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(15.5f, 15f)
            horizontalLineToRelative(-3f)
            close()
            moveToRelative(-5f, 0f)
            verticalLineTo(13.5f)
            horizontalLineTo(10f)
            verticalLineToRelative(-1f)
            horizontalLineTo(8.5f)
            verticalLineToRelative(-1f)
            horizontalLineTo(10f)
            verticalLineToRelative(-1f)
            horizontalLineTo(7.5f)
            verticalLineTo(9f)
            horizontalLineToRelative(3f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(11.5f, 10f)
            verticalLineToRelative(4f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(10.5f, 15f)
            horizontalLineToRelative(-3f)
            close()
            moveTo(14f, 13.5f)
            horizontalLineToRelative(1f)
            verticalLineToRelative(-3f)
            horizontalLineTo(14f)
            verticalLineToRelative(3f)
            close()
            moveTo(20f, 12f)
            quadTo(20f, 9.13f, 18.19f, 6.93f)
            quadTo(16.38f, 4.72f, 13.55f, 4.15f)
            lineTo(15f, 5.6f)
            lineTo(13.6f, 7f)
            lineTo(9.05f, 2.45f)
            quadTo(9.78f, 2.2f, 10.51f, 2.1f)
            reflectiveQuadTo(12f, 2f)
            quadToRelative(2.08f, 0f, 3.9f, 0.79f)
            reflectiveQuadToRelative(3.17f, 2.14f)
            quadToRelative(1.35f, 1.35f, 2.14f, 3.17f)
            quadTo(22f, 9.92f, 22f, 12f)
            horizontalLineTo(20f)
            close()
          }
        }
        .build()
    return _3d_rotation!!
  }

private var _3d_rotation: ImageVector? = null
