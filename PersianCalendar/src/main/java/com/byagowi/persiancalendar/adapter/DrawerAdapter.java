package com.byagowi.persiancalendar.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.ItemDrawerBinding;
import com.byagowi.persiancalendar.view.activity.MainActivity;
import com.byagowi.persiancalendar.viewmodel.DrawerItemViewModel;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {
    private int selectedItem;
    private String[] drawerTitles;
    private String[] drawerSubtitles;
    private TypedArray drawerIcon;

    @ColorInt
    private int selectedBackgroundColor;

    @DrawableRes
    private int selectableBackgroundResource;

    public DrawerAdapter(Context context) {
        drawerTitles = context.getResources().getStringArray(R.array.drawerTitles);
        drawerSubtitles = context.getResources().getStringArray(R.array.drawerSubtitles);
        drawerIcon = context.getResources().obtainTypedArray(R.array.drawerIcons);

        Resources.Theme theme = context.getTheme();
        TypedValue value = new TypedValue();

        theme.resolveAttribute(R.attr.selectableItemBackground, value, true);
        selectableBackgroundResource = value.resourceId;

        theme.resolveAttribute(R.attr.colorDrawerSelect, value, true);
        selectedBackgroundColor = ContextCompat.getColor(context, value.resourceId);
    }

    @Override
    public DrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.item_drawer, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(DrawerAdapter.ViewHolder holder, int position) {
        holder.bind(position);
    }

    public void setSelectedItem(int item) {
        selectedItem = item;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return drawerTitles.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ItemDrawerBinding binding;

        ViewHolder(ItemDrawerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onClick(View view) {
            Context ctx = view.getContext();
            if (ctx != null && ctx instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) ctx;
                mainActivity.selectItem(getAdapterPosition());
            }
        }

        void bind(int position) {
            binding.setModel(new DrawerItemViewModel(drawerIcon.getResourceId(position, 0),
                    drawerTitles[position], drawerSubtitles[position], this));
            binding.executePendingBindings();

            // These also should be moved to a better place
            if (selectedItem == position) {
                itemView.setBackgroundColor(selectedBackgroundColor);
            } else {
                itemView.setBackgroundResource(selectableBackgroundResource);
            }
        }
    }
}