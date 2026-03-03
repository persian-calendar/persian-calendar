package com.byagowi.persiancalendar.ui.utils

import android.graphics.Matrix
import android.graphics.Path

fun Path.rotateBy(degree: Float, pivotX: Float, pivotY: Float) =
    Path().also { it.addPath(this, Matrix().apply { setRotate(degree, pivotX, pivotY) }) }

fun Path.translateBy(dx: Float, dy: Float) =
    Path().also { it.addPath(this, Matrix().apply { setTranslate(dx, dy) }) }

fun Path.scaleBy(sx: Float, sy: Float) =
    Path().also { it.addPath(this, Matrix().apply { setScale(sx, sy) }) }
