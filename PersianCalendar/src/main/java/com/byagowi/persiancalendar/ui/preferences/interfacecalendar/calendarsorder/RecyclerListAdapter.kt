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

import android.animation.ValueAnimator
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.databinding.CalendarTypeItemBinding
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency
import java.util.*

class RecyclerListAdapter constructor(private val calendarPreferenceDialog: CalendarPreferenceDialog,
                                      private val mainActivityDependency: MainActivityDependency,
                                      titles: List<String>, values: List<String>, enabled: List<Boolean>) : RecyclerView.Adapter<RecyclerListAdapter.ItemViewHolder>() {

    private val titles: MutableList<String>
    private val values: MutableList<String>
    private val enabled: MutableList<Boolean>

    val result: List<String>
        get() {
            val result = ArrayList<String>()
            for (i in values.indices) {
                if (enabled[i]) {
                    result.add(values[i])
                }
            }
            return result
        }

    init {
        this.titles = ArrayList(titles)
        this.values = ArrayList(values)
        this.enabled = ArrayList(enabled)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = CalendarTypeItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)

        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(position)

        // Start a drag whenever the handle view it touched
        holder.itemView.setOnTouchListener { _, event ->
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                calendarPreferenceDialog.onStartDrag(holder)
            }
            false
        }
    }

    fun onItemMoved(fromPosition: Int, toPosition: Int) {
        Collections.swap(titles, fromPosition, toPosition)
        Collections.swap(values, fromPosition, toPosition)
        Collections.swap(enabled, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun onItemDismissed(position: Int) {
        titles.removeAt(position)
        values.removeAt(position)
        enabled.removeAt(position)
        notifyItemRemoved(position)

        // Easter egg when all are swiped
        if (titles.size == 0) {
            try {
                val view = mainActivityDependency.mainActivity.coordinator
                ValueAnimator.ofFloat(0f, 360f).apply {
                    duration = 3000L
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { value -> view.rotation = value.animatedValue as Float }
                }.start()
                //                Context context = calendarPreferenceDialog.getContext();
                //                MediaPlayer mediaPlayer = MediaPlayer.create(context,
                //                        R.raw.bach_invention_01);
                //                if (!mediaPlayer.isPlaying()) {
                //                    mediaPlayer.start();
                //                }
                //                AppCompatImageButton imageButton = new AppCompatImageButton(context);
                //                imageButton.setImageResource(R.drawable.ic_stop);
                //                AlertDialog alertDialog = new AlertDialog.Builder(context)
                //                        .setView(imageButton).create();
                //                imageButton.setOnClickListener(v -> {
                //                    try {
                //                        mediaPlayer.stop();
                //                    } catch (Exception ignore) {
                //                    }
                //                    alertDialog.dismiss();
                //                });
                //                alertDialog.show();
            } catch (ignored: Exception) {
            }

            calendarPreferenceDialog.dismiss()
        }
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    inner class ItemViewHolder constructor(private val binding: CalendarTypeItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            binding.checkTextView.text = titles[position]
            binding.checkTextView.isChecked = enabled[position]

            binding.checkTextView.setOnClickListener {
                val newState = !binding.checkTextView.isChecked
                binding.checkTextView.isChecked = newState
                enabled[position] = newState
            }
        }

        fun onItemSelected() = binding.root.setBackgroundColor(Color.LTGRAY)

        fun onItemCleared() = binding.root.setBackgroundColor(0)
    }
}
