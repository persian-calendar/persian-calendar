/*
 * This file was originally a part of Level (an Android Bubble Level).
 * <https://github.com/avianey/Level>
 *
 * Copyright (C) 2014 Antoine Vianey
 *
 * Level is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Level is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Level. If not, see <http://www.gnu.org/licenses/>
 */
package net.androgames.level

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withRotation
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class LevelView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val angleDisplay = AngleDisplay(context)
    private val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.BLACK
    }

    /**
     * Dimensions
     */
    private var canvasWidth = 0
    private var canvasHeight = 0
    private var minLevelX = 0
    private var maxLevelX = 0
    private var levelWidth = 0
    private var levelHeight = 0
    private var levelMinusBubbleWidth = 0
    private var levelMinusBubbleHeight = 0
    private var middleX = 0
    private var middleY = 0
    private var halfBubbleWidth = 0
    private var halfBubbleHeight = 0
    private var halfMarkerGap = 0
    private var minLevelY = 0
    private var maxLevelY = 0
    private var minBubble = 0
    private var maxBubble = 0
    private val markerThickness = resources.getDimensionPixelSize(R.dimen.marker_thickness)
    private val levelBorderWidth = resources.getDimensionPixelSize(R.dimen.level_border_width)
    private val levelBorderHeight = resources.getDimensionPixelSize(R.dimen.level_border_height)
    private val sensorGap = 5.dp.toInt()
    private var levelMaxDimension = 0

    /**
     * Angles
     */
    private var angle1 = 0f
    private var angle2 = 0f

    /**
     * Orientation
     */
    private var orientation: Orientation? = null
    private var lastTime: Long = 0
    private var lastTimeShowAngle: Long = 0
    private var angleToShow1 = 0f
    private var angleToShow2 = 0f
    private var angleX = 0.0
    private var angleY = 0.0
    private var speedX = 0.0
    private var speedY = 0.0
    private var x = 0.0
    private var y = 0.0

    /**
     * Drawables
     */
    private val level1D = context.getCompatDrawable(R.drawable.level_1d)
    private val bubble1D = context.getCompatDrawable(R.drawable.bubble_1d)
    private val marker1D = context.getCompatDrawable(R.drawable.marker_1d)
    private val level2D = context.getCompatDrawable(R.drawable.level_2d)
    private val bubble2D = context.getCompatDrawable(R.drawable.bubble_2d)
    private val marker2D = context.getCompatDrawable(R.drawable.marker_2d)

    /**
     * Ajustement de la vitesse
     */
    private var viscosityValue = 1.0
    private var firstTime = true

    fun setOrientation(
        newOrientation: Orientation, newPitch: Float, newRoll: Float, newBalance: Float
    ) {
        if (orientation == null || orientation != newOrientation) {
            orientation = newOrientation
            middleX = canvasWidth / 2
            middleY = canvasHeight / 2 - angleDisplay.displayGap
            when (newOrientation) {
                Orientation.LANDING -> {
                    levelWidth = levelMaxDimension
                    levelHeight = levelMaxDimension
                }
                Orientation.TOP, Orientation.BOTTOM, Orientation.LEFT, Orientation.RIGHT -> {
                    levelWidth = canvasWidth - 2 * angleDisplay.displayGap
                    levelHeight = (levelWidth * LEVEL_ASPECT_RATIO).toInt()
                }
            }
            viscosityValue = levelWidth.toDouble()
            minLevelX = middleX - levelWidth / 2
            maxLevelX = middleX + levelWidth / 2
            minLevelY = middleY - levelHeight / 2
            maxLevelY = middleY + levelHeight / 2

            // bubble
            halfBubbleWidth = (levelWidth * BUBBLE_WIDTH / 2).toInt()
            halfBubbleHeight = (halfBubbleWidth * BUBBLE_ASPECT_RATIO).toInt()
            val bubbleWidth = 2 * halfBubbleWidth
            val bubbleHeight = 2 * halfBubbleHeight
            maxBubble = (maxLevelY - bubbleHeight * BUBBLE_CROPPING).toInt()
            minBubble = maxBubble - bubbleHeight

            // display
            val displayY = when (newOrientation) {
                Orientation.LEFT, Orientation.RIGHT ->
                    (canvasHeight - canvasWidth) / 2 + canvasWidth - angleDisplay.displayGap
                else -> canvasHeight
            }
            angleDisplay.updatePlacement(middleX, displayY)

            // marker
            halfMarkerGap = (levelWidth * MARKER_GAP / 2).toInt()

            // autres
            levelMinusBubbleWidth = levelWidth - bubbleWidth - 2 * levelBorderWidth
            levelMinusBubbleHeight = levelHeight - bubbleHeight - 2 * levelBorderWidth

            // positionnement
            level1D.setBounds(minLevelX, minLevelY, maxLevelX, maxLevelY)
            level2D.setBounds(minLevelX, minLevelY, maxLevelX, maxLevelY)
            marker2D.setBounds(
                middleX - halfMarkerGap - markerThickness,
                middleY - halfMarkerGap - markerThickness,
                middleX + halfMarkerGap + markerThickness,
                middleY + halfMarkerGap + markerThickness
            )
            x = (maxLevelX + minLevelX).toDouble() / 2
            y = (maxLevelY + minLevelY).toDouble() / 2
        }
        when (orientation) {
            Orientation.TOP, Orientation.BOTTOM -> {
                angle1 = abs(newBalance)
                angleX = sin(Math.toRadians(newBalance.toDouble())) / MAX_SINUS
            }
            Orientation.LANDING -> {
                angle2 = abs(newRoll)
                angleX = sin(Math.toRadians(newRoll.toDouble())) / MAX_SINUS
                angle1 = abs(newPitch)
                angleY = sin(Math.toRadians(newPitch.toDouble())) / MAX_SINUS
                if (angle1 > 90) {
                    angle1 = 180 - angle1
                }
            }
            Orientation.RIGHT, Orientation.LEFT -> {
                angle1 = abs(newPitch)
                angleY = sin(Math.toRadians(newPitch.toDouble())) / MAX_SINUS
                if (angle1 > 90) {
                    angle1 = 180 - angle1
                }
            }
            else -> Unit
        }
        // correction des angles affiches
        angle1 = angle1.coerceAtMost(99.9f)
        angle2 = angle2.coerceAtMost(99.9f)
        // correction des angles aberrants
        // pour ne pas que la bulle sorte de l'ecran
        angleX = angleX.coerceIn(-1.0, 1.0)
        angleY = angleY.coerceIn(-1.0, 1.0)
        // correction des angles a plat
        // la bulle ne doit pas sortir du niveau
        if (orientation == Orientation.LANDING && angleX != 0.0 && angleY != 0.0) {
            val n = hypot(angleX, angleY)
            val teta = acos(abs(angleX) / n)
            val l = 1 / max(abs(cos(teta)), abs(sin(teta)))
            angleX /= l
            angleY /= l
        }
        onIsLevel(newOrientation.isLevel(newPitch, newRoll, newBalance, .8f))
        invalidate()
    }

    var onIsLevel = fun(_: Boolean) {}

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasWidth = w
        canvasHeight = h
        levelMaxDimension = min(
            min(h, w) - 2 * angleDisplay.displayGap,
            max(h, w) - 2 * (sensorGap + 3 * angleDisplay.displayGap + angleDisplay.lcdHeight)
        )
        angleDisplay.updatePlacement(canvasWidth / 2, canvasHeight)
    }

    public override fun onDraw(canvas: Canvas) {
        if (firstTime) {
            setOrientation(Orientation.LANDING, 0f, 0f, 0f)
            firstTime = false
        }
        val orientation = orientation ?: return

        // update physics
        val currentTime = System.currentTimeMillis()
        val timeDiff = (currentTime - lastTime) / 1000.0
        lastTime = currentTime
        if (currentTime - lastTimeShowAngle > 500) {
            angleToShow1 = angle1
            angleToShow2 = angle2
            lastTimeShowAngle = currentTime
        }
        val posX = orientation.reverse * (2 * x - minLevelX - maxLevelX) / levelMinusBubbleWidth
        when (orientation) {
            Orientation.TOP, Orientation.BOTTOM ->
                speedX = orientation.reverse * (angleX - posX) * viscosityValue
            Orientation.LEFT, Orientation.RIGHT ->
                speedX = orientation.reverse * (angleY - posX) * viscosityValue
            Orientation.LANDING -> {
                val posY = (2 * y - minLevelY - maxLevelY) / levelMinusBubbleHeight
                speedX = (angleX - posX) * viscosityValue
                speedY = (angleY - posY) * viscosityValue
                y += speedY * timeDiff
            }
        }
        x += speedX * timeDiff
        // en cas de latence elevee
        // si la bubble a trop deviee
        // elle est replacee correctement
        if (orientation == Orientation.LANDING) {
            if (hypot(middleX - x, middleY - y) > levelMaxDimension / 2 - halfBubbleWidth) {
                x = (angleX * levelMinusBubbleWidth + minLevelX + maxLevelX) / 2
                y = (angleY * levelMinusBubbleHeight + minLevelY + maxLevelY) / 2
            }
        } else {
            if (x < minLevelX + halfBubbleWidth || x > maxLevelX - halfBubbleWidth) {
                x = (angleX * levelMinusBubbleWidth + minLevelX + maxLevelX) / 2
            }
        }
        if (orientation == Orientation.LANDING) {
            bubble2D.setBounds(
                (x - halfBubbleWidth).toInt(),
                (y - halfBubbleHeight).toInt(),
                (x + halfBubbleWidth).toInt(),
                (y + halfBubbleHeight).toInt()
            )
            level2D.draw(canvas)
            bubble2D.draw(canvas)
            marker2D.draw(canvas)
            canvas.drawLine(
                minLevelX.toFloat(), middleY.toFloat(),
                (middleX - halfMarkerGap).toFloat(), middleY.toFloat(), infoPaint
            )
            canvas.drawLine(
                (middleX + halfMarkerGap).toFloat(), middleY.toFloat(),
                maxLevelX.toFloat(), middleY.toFloat(), infoPaint
            )
            canvas.drawLine(
                middleX.toFloat(), minLevelY.toFloat(),
                middleX.toFloat(), (middleY - halfMarkerGap).toFloat(), infoPaint
            )
            canvas.drawLine(
                middleX.toFloat(), (middleY + halfMarkerGap).toFloat(),
                middleX.toFloat(), maxLevelY.toFloat(), infoPaint
            )

            angleDisplay.draw(canvas, angleToShow1, offsetXFactor = -1)
            angleDisplay.draw(canvas, angleToShow2, offsetXFactor = 1)
        } else canvas.withRotation(
            orientation.rotation.toFloat(), middleX.toFloat(), middleY.toFloat()
        ) {
            angleDisplay.draw(canvas, angleToShow1)
            // level
            level1D.draw(canvas)
            // bubble
            canvas.clipRect(
                minLevelX + levelBorderWidth, minLevelY + levelBorderHeight,
                maxLevelX - levelBorderWidth, maxLevelY - levelBorderHeight
            )
            bubble1D.setBounds(
                (x - halfBubbleWidth).toInt(), minBubble,
                (x + halfBubbleWidth).toInt(), maxBubble
            )
            bubble1D.draw(canvas)
            // marker
            marker1D.setBounds(
                middleX - halfMarkerGap - markerThickness, minLevelY,
                middleX - halfMarkerGap, maxLevelY
            )
            marker1D.draw(canvas)
            marker1D.setBounds(
                middleX + halfMarkerGap, minLevelY,
                middleX + halfMarkerGap + markerThickness, maxLevelY
            )
            marker1D.draw(canvas)
        }
    }

    companion object {
        private const val LEVEL_ASPECT_RATIO = 0.150
        private const val BUBBLE_WIDTH = 0.150
        private const val BUBBLE_ASPECT_RATIO = 1.000
        private const val BUBBLE_CROPPING = 0.500
        private const val MARKER_GAP = BUBBLE_WIDTH + 0.020
        private val MAX_SINUS = sin(PI / 4) // Angle max
    }
}
