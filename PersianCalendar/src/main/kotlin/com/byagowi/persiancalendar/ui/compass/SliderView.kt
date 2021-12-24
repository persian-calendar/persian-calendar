package com.byagowi.persiancalendar.ui.compass

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.resolveColor
import kotlin.math.abs
import kotlin.math.sqrt

class SliderView(context: Context, attrs: AttributeSet? = null) :
    RecyclerView(context, attrs) {

    private val itemsCount = 500000
    private var positionOffset = 0

    init {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }
        adapter = object : RecyclerView.Adapter<ViewHolder>() {
            private val commonLayoutParams = ViewGroup.LayoutParams(
                10.dp.toInt(), ViewGroup.LayoutParams.MATCH_PARENT
            )

            override fun onBindViewHolder(holder: ViewHolder, position: Int) = Unit
            override fun getItemCount() = itemsCount
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                object : RecyclerView.ViewHolder(object : View(parent.context) {
                    init {
                        layoutParams = commonLayoutParams
                    }
                }) {}
        }
        scrollToPosition(itemsCount / 2)

        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                positionOffset -= dx
            }
        })
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.strokeWidth = 1.5.dp
        it.color = context.resolveColor(com.google.android.material.R.attr.colorAccent)
    }

    private var lines = FloatArray(8) { 0f }
    private val space = 10.dp

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        lines[1] = 0f
        lines[3] = height / 2f
        lines[5] = height / 2f
        lines[7] = height.toFloat()
        val linesCount = width / space.toInt()
        (0..linesCount).forEachIndexed { index, it ->
            val x = it * space + positionOffset % space
            val centrality = (index - linesCount / 2f) / linesCount
            paint.alpha = ((1 - sqrt(abs(centrality))) * 255).toInt()
            val indentX = x + 80 * centrality
            if (indentX >= 0 && indentX <= width) {
                lines[0] = x
                lines[2] = indentX
                lines[4] = indentX
                lines[6] = x
                canvas.drawLines(lines, paint)
            }
        }
    }
}
