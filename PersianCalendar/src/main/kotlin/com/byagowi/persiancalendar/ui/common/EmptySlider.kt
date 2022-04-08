package com.byagowi.persiancalendar.ui.common

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.ui.utils.dp

// Is an RecyclerView based pseduo-infinite scroll provider that its children may also draw
// things on the screen. It was better to write it using  androidx.dynamicanimation but it
// had too many boilerplate.
open class EmptySlider(context: Context, attrs: AttributeSet? = null) :
    RecyclerView(context, attrs) {

    private val itemsCount = 500000

    init {
        this.setHasFixedSize(true)
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
        this.post { scrollToPosition(itemsCount / 2) }
    }

    override fun onDraw(canvas: Canvas) {
    }
}
