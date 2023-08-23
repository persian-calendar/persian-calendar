package com.byagowi.persiancalendar.ui.settings.interfacecalendar.calendarsorder

/*
 * Copyright (C) 2015 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.databinding.CalendarTypeItemBinding
import com.byagowi.persiancalendar.ui.utils.layoutInflater

class RecyclerListAdapter(private var items: List<Item>) :
    RecyclerView.Adapter<RecyclerListAdapter.ItemViewHolder>() {

    data class Item(val title: String, val key: String, val enabled: Boolean)

    val result: List<String> get() = items.filter { it.enabled }.map { it.key }

    private val itemTouchHelper = ItemTouchHelper(SimpleItemTouchHelperCallback(this))

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemViewHolder(
        CalendarTypeItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(position)

        // Start a drag whenever the handle view it touched
        holder.itemView.setOnTouchListener { _, event ->
            val rippleDrawable = holder.itemView.background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                rippleDrawable is RippleDrawable
            ) rippleDrawable.setHotspot(event.x, event.y)

            if (event.action == MotionEvent.ACTION_DOWN) {
                itemTouchHelper.startDrag(holder)
            }
            false
        }
    }

    fun onItemMoved(fromPosition: Int, toPosition: Int) {
        items = items.mapIndexed { i, x ->
            // swap from and to in the new object
            when (i) {
                fromPosition -> items[toPosition]
                toPosition -> items[fromPosition]
                else -> x
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    fun onItemDismissed(position: Int) {
        items = items.filterIndexed { i, _ -> i != position }
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(private val binding: CalendarTypeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.checkTextView.setOnClickListener {
                val newState = !binding.checkTextView.isChecked
                binding.checkTextView.isChecked = newState
                items = items.mapIndexed { i, x ->
                    if (i == layoutPosition) Item(x.title, x.key, newState) else x
                }
            }
        }

        fun bind(position: Int) = binding.let {
            it.checkTextView.text = items[position].title
            it.checkTextView.isChecked = items[position].enabled
        }

        fun onItemSelected() {
            binding.root.isPressed = true
        }

        fun onItemCleared() {
            binding.root.isPressed = false
        }
    }
}
