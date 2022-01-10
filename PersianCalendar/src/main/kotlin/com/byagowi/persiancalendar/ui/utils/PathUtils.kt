package com.byagowi.persiancalendar.ui.utils

import android.graphics.Matrix
import android.graphics.Path

fun List<Pair<Float, Float>>.toPath(close: Boolean = true) = Path().also {
    if (size >= 1) it.moveTo(this[0].first, this[0].second)
    this.drop(1).map { (x, y) -> it.lineTo(x, y) }
    if (close) it.close()
}

fun Path.rotateBy(degree: Float, pivotX: Float, pivotY: Float) =
    Path().also { it.addPath(this, Matrix().apply { setRotate(degree, pivotX, pivotY) }) }
