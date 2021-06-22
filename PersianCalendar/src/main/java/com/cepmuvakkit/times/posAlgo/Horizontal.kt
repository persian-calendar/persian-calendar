/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cepmuvakkit.times.posAlgo

import android.graphics.Canvas

class Horizontal {
    var elevation //h         Altitude
            = 0.0
    var azimuth //Az       Azimuth
            = 0.0

    constructor() {}
    constructor(Azimuth: Double, Altitude: Double) {
        elevation = Altitude
        azimuth = Azimuth
    }

    fun toScreenPosition(canvas: Canvas, offset: Int, flipX: Boolean): ScreenPosition {
        val midX = canvas.width / 2
        val midY = canvas.height / 2
        val maxR = Math.min(midX, midY)
        val screenPosition = ScreenPosition()
        val r = (90 - elevation) / 90 * maxR
        val azimuth = Math.toRadians(azimuth - offset)
        screenPosition.x = (Math.sin(azimuth) * r).toInt() * (if (flipX) -1 else 1) + midX
        screenPosition.y = (Math.cos(azimuth) * -r).toInt() + midY
        return screenPosition
    }

    fun toScreenPosition(midX: Int, midY: Int): ScreenPosition {
        val maxR = Math.min(midX, midY)
        val screenPosition = ScreenPosition()
        val r = (90 - elevation) / 90 * maxR
        val azimuth = Math.toRadians(azimuth)
        screenPosition.x = (Math.sin(azimuth) * r).toInt() + midX
        screenPosition.y = (Math.cos(azimuth) * -r).toInt() + midY
        return screenPosition
    }
}
