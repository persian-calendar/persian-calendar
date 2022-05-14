package com.byagowi.persiancalendar.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.roundToInt

// Based on https://stackoverflow.com/a/17649895 but modified to draw itself instead
open class ZoomableView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val matrix = FloatArray(9)
    private val viewMatrix = Matrix()
    private val start = PointF()
    private val last = PointF()

    private var mode = NONE
    private var minScale = 1f
    var maxScale = 16f
    private var redundantXSpace = 0f
    private var redundantYSpace = 0f
    protected var saveScale = 1f
    private var right = 0f
    private var bottom = 0f
    private var originalWidth = 0f
    private var originalHeight = 0f
    var contentWidth = Float.NaN // Be sure to set these two before use
    var contentHeight = Float.NaN

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var scaleFactor = detector.scaleFactor
            val origScale = saveScale
            saveScale *= scaleFactor
            if (saveScale > maxScale) {
                saveScale = maxScale
                scaleFactor = maxScale / origScale
            } else if (saveScale < minScale) {
                saveScale = minScale
                scaleFactor = minScale / origScale
            }
            right = width * saveScale - width - 2 * redundantXSpace * saveScale
            bottom = height * saveScale - height - 2 * redundantYSpace * saveScale
            if (originalWidth * saveScale <= width || originalHeight * saveScale <= height) {
                viewMatrix.postScale(scaleFactor, scaleFactor, width / 2f, height / 2f)
                if (scaleFactor < 1) {
                    viewMatrix.getValues(matrix)
                    val x = matrix[Matrix.MTRANS_X]
                    val y = matrix[Matrix.MTRANS_Y]
                    if (scaleFactor < 1) {
                        if ((originalWidth * saveScale).roundToInt() < width) {
                            if (y < -bottom) viewMatrix.postTranslate(0f, -(y + bottom))
                            else if (y > 0) viewMatrix.postTranslate(0f, -y)
                        } else {
                            if (x < -right) viewMatrix.postTranslate(-(x + right), 0f)
                            else if (x > 0) viewMatrix.postTranslate(-x, 0f)
                        }
                    }
                }
            } else {
                viewMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                viewMatrix.getValues(matrix)
                val x = matrix[Matrix.MTRANS_X]
                val y = matrix[Matrix.MTRANS_Y]
                if (scaleFactor < 1) {
                    if (x < -right) viewMatrix.postTranslate(-(x + right), 0f)
                    else if (x > 0) viewMatrix.postTranslate(-x, 0f)

                    if (y < -bottom) viewMatrix.postTranslate(0f, -(y + bottom))
                    else if (y > 0) viewMatrix.postTranslate(0f, -y)
                }
            }
            return true
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (contentWidth.isNaN()) contentWidth = w.toFloat()
        if (contentHeight.isNaN()) contentHeight = h.toFloat()
        // Fit to screen.
        val scaleX = width / contentWidth
        val scaleY = height / contentHeight
        val scale = min(scaleX, scaleY)
        viewMatrix.setScale(scale, scale)
        saveScale = 1f

        // Center the image
        redundantYSpace = height - scale * contentHeight
        redundantXSpace = width - scale * contentWidth
        redundantYSpace /= 2
        redundantXSpace /= 2
        viewMatrix.postTranslate(redundantXSpace, redundantYSpace)
        originalWidth = width - 2 * redundantXSpace
        originalHeight = height - 2 * redundantYSpace
        right = width * saveScale - width - 2 * redundantXSpace * saveScale
        bottom = height * saveScale - height - 2 * redundantYSpace * saveScale
    }

    var onClick = fun(_: Float, _: Float) {}

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        viewMatrix.getValues(matrix)
        val x = matrix[Matrix.MTRANS_X]
        val y = matrix[Matrix.MTRANS_Y]
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                last.set(event.x, event.y)
                start.set(last)
                mode = DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                last.set(event.x, event.y)
                start.set(last)
                mode = ZOOM
            }
            MotionEvent.ACTION_MOVE -> //if the mode is ZOOM or
                //if the mode is DRAG and already zoomed
                if (mode == ZOOM || mode == DRAG && saveScale > minScale) {
                    var deltaX = event.x - last.x // x difference
                    var deltaY = event.y - last.y // y difference
                    // width after applying current scale
                    val scaleWidth = (originalWidth * saveScale).roundToInt()
                    // height after applying current scale
                    val scaleHeight = (originalHeight * saveScale).roundToInt()
                    // if scaleWidth is smaller than the views width
                    // in other words if the image width fits in the view
                    // limit left and right movement
                    if (scaleWidth < width) {
                        deltaX = 0f
                        if (y + deltaY > 0) deltaY = -y
                        else if (y + deltaY < -bottom) deltaY = -(y + bottom)
                    } else if (scaleHeight < height) {
                        deltaY = 0f
                        if (x + deltaX > 0) deltaX = -x
                        else if (x + deltaX < -right) deltaX = -(x + right)
                    } else {
                        if (x + deltaX > 0) deltaX = -x
                        else if (x + deltaX < -right) deltaX = -(x + right)

                        if (y + deltaY > 0) deltaY = -y
                        else if (y + deltaY < -bottom) deltaY = -(y + bottom)
                    }
                    // move the image with the matrix
                    viewMatrix.postTranslate(deltaX, deltaY)
                    // set the last touch location to the current
                    last.set(event.x, event.y)
                }
            MotionEvent.ACTION_UP -> {
                mode = NONE
                if ((event.x - start.x).absoluteValue < 5 && (event.y - start.y).absoluteValue < 5) {
                    performClick()
                    // https://stackoverflow.com/a/7418428
                    val inverse = Matrix()
                    viewMatrix.invert(inverse)
                    val touchPoint = floatArrayOf(event.x, event.y)
                    inverse.mapPoints(touchPoint)
                    onClick(touchPoint[0], touchPoint[1])
                }
            }
            MotionEvent.ACTION_POINTER_UP -> mode = NONE
        }
        invalidate()
        return true
    }

    var onDraw = fun(_: Canvas, _: Matrix) {}
    override fun onDraw(canvas: Canvas) = onDraw(canvas, viewMatrix)

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }
}
