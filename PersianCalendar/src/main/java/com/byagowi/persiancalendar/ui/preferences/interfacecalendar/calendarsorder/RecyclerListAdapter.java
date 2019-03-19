package com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder;
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

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.byagowi.persiancalendar.databinding.CalendarTypeItemBinding;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerListAdapter.ItemViewHolder> {

    private final List<String> titles;
    private final List<String> values;
    private final List<Boolean> enabled;
    private final CalendarPreferenceDialog calendarPreferenceDialog;
    private final MainActivityDependency mainActivityDependency;

    RecyclerListAdapter(CalendarPreferenceDialog calendarPreferenceDialog,
                        MainActivityDependency mainActivityDependency,
                        List<String> titles, List<String> values, List<Boolean> enabled) {
        this.calendarPreferenceDialog = calendarPreferenceDialog;
        this.titles = new ArrayList<>(titles);
        this.values = new ArrayList<>(values);
        this.enabled = new ArrayList<>(enabled);
        this.mainActivityDependency = mainActivityDependency;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CalendarTypeItemBinding binding = CalendarTypeItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(position);

        // Start a drag whenever the handle view it touched
        holder.itemView.setOnTouchListener((v, event) -> {
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                calendarPreferenceDialog.onStartDrag(holder);
            }
            return false;
        });
    }

    void onItemMoved(int fromPosition, int toPosition) {
        Collections.swap(titles, fromPosition, toPosition);
        Collections.swap(values, fromPosition, toPosition);
        Collections.swap(enabled, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    void onItemDismissed(int position) {
        titles.remove(position);
        values.remove(position);
        enabled.remove(position);
        notifyItemRemoved(position);

        // Easter egg when all are swiped
        if (titles.size() == 0) {
            try {
                View view = mainActivityDependency.getMainActivity().getCoordinator();
                ValueAnimator animator = ValueAnimator.ofFloat(0, 360);
                animator.setDuration(3000L);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.addUpdateListener(value -> view.setRotation((float) value.getAnimatedValue()));
                animator.start();
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
            } catch (Exception ignored) {
            }
            calendarPreferenceDialog.dismiss();
        }
    }

    public List<String> getResult() {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < values.size(); ++i) {
            if (enabled.get(i)) {
                result.add(values.get(i));
            }
        }
        return result;
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private CalendarTypeItemBinding binding;

        ItemViewHolder(CalendarTypeItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(int position) {
            binding.checkTextView.setText(titles.get(position));
            binding.checkTextView.setChecked(enabled.get(position));

            binding.checkTextView.setOnClickListener(v -> {
                boolean newState = !binding.checkTextView.isChecked();
                binding.checkTextView.setChecked(newState);
                enabled.set(position, newState);
            });
        }

        void onItemSelected() {
            binding.getRoot().setBackgroundColor(Color.LTGRAY);
        }

        void onItemCleared() {
            binding.getRoot().setBackgroundColor(0);
        }
    }
}
