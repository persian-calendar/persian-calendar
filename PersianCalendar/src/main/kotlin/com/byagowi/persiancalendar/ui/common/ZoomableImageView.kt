package com.byagowi.persiancalendar.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import com.byagowi.persiancalendar.ui.map.ZoomableView

class ZoomableImageView(context: Context, attrs: AttributeSet? = null) :
    ZoomableView(context, attrs) {
    private var bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    fun setImageBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        contentWidth = bitmap.width.toFloat()
        contentHeight = bitmap.height.toFloat()
        invalidate()
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.isFilterBitmap = true }
    override fun zoomableDraw(canvas: Canvas, matrix: Matrix) {
        canvas.drawBitmap(bitmap, matrix, paint)
    }
}
