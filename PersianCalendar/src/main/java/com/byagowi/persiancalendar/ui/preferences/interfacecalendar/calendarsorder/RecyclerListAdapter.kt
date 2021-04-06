package com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder

/*
 * Copyright (C) 2015 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.databinding.CalendarTypeItemBinding

import com.byagowi.persiancalendar.utils.layoutInflater


class RecyclerListAdapter(
    private val itemCallback: CalendarsOrderItemCallback,
) : ListAdapter<RecyclerListAdapter.Item, RecyclerListAdapter.VH>(DEFAULT_DIFF_UTIL) {

    data class Item(val title: String, val key: String, val enabled: Boolean)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        CalendarTypeItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind()
    }

    inner class VH(
        private val binding: CalendarTypeItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.checkTextView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    itemCallback.onItemToggle(adapterPosition)
            }
        }

        fun bind() {
            with(getItem(adapterPosition)) {
                binding.checkTextView.text = title
                binding.checkTextView.isChecked = enabled
            }
        }
    }

    interface CalendarsOrderItemCallback {
        fun onItemToggle(itemPosition: Int)
    }

    companion object {
        val DEFAULT_DIFF_UTIL = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item) = oldItem.key == newItem.key
            override fun areContentsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem
        }
    }
}
