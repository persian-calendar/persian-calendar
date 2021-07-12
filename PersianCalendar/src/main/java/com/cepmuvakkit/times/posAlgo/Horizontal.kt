package com.cepmuvakkit.times.posAlgo

import android.graphics.Canvas
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class Horizontal(
    val azimuth: Double, // Az  azimuth
    val altitude: Double // h   altitude / elevation
) {
    fun toScreenPosition(canvas: Canvas, offset: Int, flipX: Boolean): ScreenPosition {
        val midX = canvas.width / 2
        val midY = canvas.height / 2
        val maxR = min(midX, midY)
        val r = (90 - altitude) / 90 * maxR
        val azimuth = Math.toRadians(azimuth - offset)
        return ScreenPosition(
            x = (sin(azimuth) * r).toInt() * (if (flipX) -1 else 1) + midX,
            y = (cos(azimuth) * -r).toInt() + midY
        )
    }

    fun toScreenPosition(midX: Int, midY: Int): ScreenPosition {
        val maxR = min(midX, midY)
        val r = (90 - altitude) / 90 * maxR
        val azimuth = Math.toRadians(azimuth)
        return ScreenPosition(
            x = (sin(azimuth) * r).toInt() + midX,
            y = (cos(azimuth) * -r).toInt() + midY
        )
    }
}
