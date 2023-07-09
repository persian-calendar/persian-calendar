package com.byagowi.persiancalendar.ui.converter

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.shareBinaryFile
import com.byagowi.persiancalendar.ui.utils.toByteArray
import com.byagowi.persiancalendar.utils.logException
import kotlin.math.min

class QrView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var qr: List<List<Boolean>> = emptyList()
    private var roundness = 1f
    private var viewSize = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = context.resolveColor(android.R.attr.textColorPrimary)
    }

    override fun onDraw(canvas: Canvas) = drawQr(canvas, viewSize)

    private var animator: ValueAnimator? = null

    init {
        setOnClickListener {
            animator?.cancel()
            ValueAnimator.ofFloat(roundness, if (roundness > .5f) 0f else 1f).also {
                animator = it
                it.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
                it.interpolator = AccelerateDecelerateInterpolator()
                it.addUpdateListener { _ ->
                    roundness = it.animatedValue as? Float ?: return@addUpdateListener
                    invalidate()
                }
            }.start()
        }
    }

    private val rect = RectF()
    private val path = Path()
    private fun drawQr(canvas: Canvas, size: Int) {
        val cellSize = size.toFloat() / (qr.size.takeIf { it != 0 } ?: return)
        qr.forEachIndexed { i, row ->
            row.forEachIndexed { j, v ->
                val s = qr.size
                if (v && (i > 6 || j > 6) && (s - i > 7 || j > 6) && (i > 6 || s - j > 7))
                    drawDot(canvas, i, j, cellSize)
            }
        }
        path.rewind()
        rect.set(0f, 0f, cellSize * 7, cellSize * 7)
        val round = cellSize * roundness
        path.addRoundRect(rect, round * 2, round * 2, Path.Direction.CW)
        rect.set(cellSize, cellSize, cellSize * 6, cellSize * 6)
        path.addRoundRect(rect, round * 1.5f, round * 1.5f, Path.Direction.CCW)
        rect.set(cellSize * 2, cellSize * 2, cellSize * 5, cellSize * 5)
        path.addRoundRect(rect, round, round, Path.Direction.CW)
        canvas.drawPath(path, paint)
        canvas.withTranslation(0f, cellSize * (qr.size - 7)) { canvas.drawPath(path, paint) }
        canvas.withTranslation(cellSize * (qr.size - 7), 0f) { canvas.drawPath(path, paint) }
    }

    private fun drawDot(canvas: Canvas, i: Int, j: Int, cellSize: Float) {
        rect.set(i * cellSize, j * cellSize, (i + 1) * cellSize, (j + 1) * cellSize)
        rect.inset(-.25f * (1 - roundness), -.25f * (1 - roundness))
        canvas.drawRoundRect(rect, roundness * cellSize / 2, roundness * cellSize / 2, paint)
    }

    fun share(activity: FragmentActivity?) {
        val size = 1280f
        val bitmap = createBitmap(size.toInt(), size.toInt()).applyCanvas {
            drawColor(context.resolveColor(com.google.android.material.R.attr.colorSurface))
            withScale(1 - 64 / size, 1 - 64 / size, size / 2, size / 2) {
                drawQr(this, size.toInt())
            }
        }
        activity?.shareBinaryFile(bitmap.toByteArray(), "result.png", "image/png")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        viewSize = min(
            MeasureSpec.getSize(widthMeasureSpec),
            context.resources.displayMetrics.let {
                if (it.heightPixels > it.widthPixels) it.widthPixels
                else it.heightPixels * 2 / 3
            }
        )
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(viewSize, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(viewSize, MeasureSpec.EXACTLY)
        )
    }

    fun update(text: String) {
        runCatching {
            qr = qr(text)
            if (!isVisible) isVisible = true
            invalidate()
        }.onFailure(logException).onFailure {
            isVisible = false
        }
    }
}
