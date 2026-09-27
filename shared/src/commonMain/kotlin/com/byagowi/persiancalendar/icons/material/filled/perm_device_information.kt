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
public val permDeviceInformationIcon: ImageVector
  get() {
    if (_perm_device_information != null) {
      return _perm_device_information!!
    }
    _perm_device_information =
      ImageVector.Builder(
          name = "perm_device_information",
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
            moveTo(7f, 23f)
            quadTo(6.18f, 23f, 5.59f, 22.41f)
            reflectiveQuadTo(5f, 21f)
            verticalLineTo(3f)
            quadTo(5f, 2.17f, 5.59f, 1.59f)
            reflectiveQuadTo(7f, 1f)
            horizontalLineTo(17f)
            quadToRelative(0.82f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(19f, 3f)
            verticalLineTo(6.1f)
            quadToRelative(0.45f, 0.18f, 0.73f, 0.55f)
            reflectiveQuadTo(20f, 7.5f)
            verticalLineToRelative(2f)
            quadToRelative(0f, 0.47f, -0.27f, 0.85f)
            reflectiveQuadTo(19f, 10.9f)
            verticalLineTo(21f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(17f, 23f)
            horizontalLineTo(7f)
            close()
            moveToRelative(4f, -6.5f)
            horizontalLineToRelative(2f)
            verticalLineTo(11f)
            horizontalLineTo(11f)
            verticalLineToRelative(5.5f)
            close()
            moveTo(12.71f, 8.71f)
            quadTo(13f, 8.42f, 13f, 8f)
            quadTo(13f, 7.57f, 12.71f, 7.29f)
            reflectiveQuadTo(12f, 7f)
            reflectiveQuadTo(11.29f, 7.29f)
            reflectiveQuadTo(11f, 8f)
            quadToRelative(0f, 0.42f, 0.29f, 0.71f)
            reflectiveQuadTo(12f, 9f)
            reflectiveQuadTo(12.71f, 8.71f)
            close()
          }
        }
        .build()
    return _perm_device_information!!
  }

private var _perm_device_information: ImageVector? = null
