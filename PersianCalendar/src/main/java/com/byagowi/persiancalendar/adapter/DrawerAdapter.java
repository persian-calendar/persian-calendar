package com.byagowi.persiancalendar.adapter;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.ItemDrawerBinding;
import com.byagowi.persiancalendar.view.activity.MainActivity;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {
    private final MainActivity mainActivity;
    private int selectedItem;
    private String[] drawerTitles;
    private String[] drawerSubtitles;
    private TypedArray drawerIcon;

    @ColorInt
    private int selectedBackgroundColor;

    @DrawableRes
    private int selectableBackgroundResource;

    public DrawerAdapter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        drawerTitles = mainActivity.getResources().getStringArray(R.array.drawerTitles);
        drawerSubtitles = mainActivity.getResources().getStringArray(R.array.drawerSubtitles);
        drawerIcon = mainActivity.getResources().obtainTypedArray(R.array.drawerIcons);

        Resources.Theme theme = mainActivity.getTheme();
        TypedValue selectableBackground = new TypedValue();
        theme.resolveAttribute(R.attr.selectableItemBackground, selectableBackground, true);

        TypedValue selectedBackground = new TypedValue();
        theme.resolveAttribute(R.attr.colorDrawerSelect, selectedBackground, true);

        selectedBackgroundColor = ContextCompat.getColor(mainActivity, selectedBackground.resourceId);
        selectableBackgroundResource = selectableBackground.resourceId;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ItemDrawerBinding binding;

        ViewHolder(ItemDrawerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mainActivity.selectItem(getAdapterPosition());
        }
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
        holder.binding.setTitle(drawerTitles[position]);
        holder.binding.setSubtitle(drawerSubtitles[position]);
        holder.binding.setShowSubtitle(!TextUtils.isEmpty(drawerSubtitles[position]));

        holder.binding.itemIcon.setImageResource(drawerIcon.getResourceId(position, 0));
        if (selectedItem == position) {
            holder.itemView.setBackgroundColor(selectedBackgroundColor);
        } else {
            holder.itemView.setBackgroundResource(selectableBackgroundResource);
        }
    }

    public void setSelectedItem(int item) {
        selectedItem = item;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return drawerTitles.length;
    }
}