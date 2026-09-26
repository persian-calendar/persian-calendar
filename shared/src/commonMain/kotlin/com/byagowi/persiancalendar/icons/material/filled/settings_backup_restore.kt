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
public val settingsBackupRestoreIcon: ImageVector
  get() {
    if (_settings_backup_restore != null) {
      return _settings_backup_restore!!
    }
    _settings_backup_restore =
      ImageVector.Builder(
          name = "settings_backup_restore",
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
            moveTo(12f, 14f)
            quadToRelative(-0.82f, 0f, -1.41f, -0.59f)
            reflectiveQuadTo(10f, 12f)
            reflectiveQuadToRelative(0.59f, -1.41f)
            reflectiveQuadTo(12f, 10f)
            reflectiveQuadToRelative(1.41f, 0.59f)
            quadTo(14f, 11.18f, 14f, 12f)
            reflectiveQuadToRelative(-0.59f, 1.41f)
            reflectiveQuadTo(12f, 14f)
            close()
            moveToRelative(0f, 7f)
            quadTo(8.53f, 21f, 5.98f, 18.71f)
            quadTo(3.43f, 16.43f, 3.05f, 13f)
            horizontalLineTo(5.1f)
            quadToRelative(0.35f, 2.6f, 2.31f, 4.3f)
            reflectiveQuadTo(12f, 19f)
            quadToRelative(2.93f, 0f, 4.96f, -2.04f)
            quadTo(19f, 14.93f, 19f, 12f)
            quadTo(19f, 9.07f, 16.96f, 7.04f)
            reflectiveQuadTo(12f, 5f)
            quadTo(10.28f, 5f, 8.78f, 5.8f)
            reflectiveQuadTo(6.25f, 8f)
            horizontalLineTo(9f)
            verticalLineToRelative(2f)
            horizontalLineTo(3f)
            verticalLineTo(4f)
            horizontalLineTo(5f)
            verticalLineTo(6.35f)
            quadTo(6.28f, 4.75f, 8.11f, 3.88f)
            reflectiveQuadTo(12f, 3f)
            quadToRelative(1.88f, 0f, 3.51f, 0.71f)
            reflectiveQuadToRelative(2.85f, 1.93f)
            reflectiveQuadToRelative(1.93f, 2.85f)
            reflectiveQuadTo(21f, 12f)
            reflectiveQuadToRelative(-0.71f, 3.51f)
            reflectiveQuadToRelative(-1.93f, 2.85f)
            reflectiveQuadToRelative(-2.85f, 1.93f)
            reflectiveQuadTo(12f, 21f)
            close()
          }
        }
        .build()
    return _settings_backup_restore!!
  }

private var _settings_backup_restore: ImageVector? = null
