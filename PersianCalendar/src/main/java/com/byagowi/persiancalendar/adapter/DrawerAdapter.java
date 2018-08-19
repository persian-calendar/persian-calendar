package com.byagowi.persiancalendar.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.view.activity.MainActivity;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {
    private int selectedItem;
    private String[] drawerTitles;
    private String[] drawerSubtitles;
    @IdRes
    private int[] drawerIcons;

    @ColorInt
    private int selectedBackgroundColor;

    @DrawableRes
    private int selectableBackgroundResource;

    public DrawerAdapter(Context context) {
        drawerTitles = context.getResources().getStringArray(R.array.drawerTitles);
        drawerSubtitles = context.getResources().getStringArray(R.array.drawerSubtitles);
        TypedArray typedArray = context.getResources().obtainTypedArray(R.array.drawerIcons);
        drawerIcons = new int[drawerTitles.length];
        for (int i = 0; i < drawerTitles.length; ++i) {
            drawerIcons[i] = typedArray.getResourceId(i, 0);
        }
        typedArray.recycle();

        Resources.Theme theme = context.getTheme();
        TypedValue value = new TypedValue();

        theme.resolveAttribute(R.attr.selectableItemBackground, value, true);
        selectableBackgroundResource = value.resourceId;

        theme.resolveAttribute(R.attr.colorDrawerSelect, value, true);
        selectedBackgroundColor = ContextCompat.getColor(context, value.resourceId);
    }

    @Override
    public DrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_drawer, parent, false));
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
        private final AppCompatImageView icon;
        private final AppCompatTextView title;
        private final AppCompatTextView subtitle;

        ViewHolder(View itemView) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.icon);
            this.title = itemView.findViewById(R.id.title);
            this.subtitle = itemView.findViewById(R.id.subtitle);
        }

        @Override
        public void onClick(View view) {
            Context ctx = view.getContext();
            if (ctx instanceof MainActivity) {
                ((MainActivity) ctx).selectItem(getAdapterPosition());
            }
        }

        void bind(int position) {
            icon.setImageResource(drawerIcons[position]);
            title.setText(drawerTitles[position]);
            subtitle.setText(drawerSubtitles[position]);
            subtitle.setVisibility(TextUtils.isEmpty(drawerSubtitles[position])
                    ? View.GONE
                    : View.VISIBLE);

            // These also should be moved to a better place
            if (selectedItem == position) {
                itemView.setBackgroundColor(selectedBackgroundColor);
            } else {
                itemView.setBackgroundResource(selectableBackgroundResource);
            }

            itemView.setOnClickListener(this);
        }
    }
}