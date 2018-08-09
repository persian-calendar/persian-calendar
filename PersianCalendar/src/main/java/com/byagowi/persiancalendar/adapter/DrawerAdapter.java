package com.byagowi.persiancalendar.adapter;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.MainActivity;
import com.github.praytimes.Coordinate;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {
    private final MainActivity mainActivity;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
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
        private TextView itemTitle;
        private TextView itemSubtitle;
        private AppCompatImageView imageView;

        ViewHolder(View itemView, int viewType) {
            super(itemView);

            if (viewType == TYPE_ITEM) {
                itemView.setOnClickListener(this);
                itemTitle = itemView.findViewById(R.id.itemTitle);
                itemSubtitle = itemView.findViewById(R.id.itemSubtitle);
                imageView = itemView.findViewById(R.id.ItemIcon);
            } else {
                imageView = itemView.findViewById(R.id.image);
            }
        }

        @Override
        public void onClick(View view) {
            mainActivity.selectItem(getAdapterPosition());
        }
    }

    @Override
    public DrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_drawer, parent, false);

            return new ViewHolder(v, viewType);

        } else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.header_drawer, parent, false);

            return new ViewHolder(v, viewType);
        }
        return null;
    }

    private String getSeason() {
        boolean isSouthernHemisphere = false;
        Coordinate coordinate = Utils.getCoordinate(mainActivity);
        if (coordinate != null && coordinate.getLatitude() < 0) {
            isSouthernHemisphere = true;
        }

        int month = Utils.getPersianToday().getMonth();
        if (isSouthernHemisphere) month = ((month + 6 - 1) % 12) + 1;

        if (month < 4) return "SPRING";
        else if (month < 7) return "SUMMER";
        else if (month < 10) return "FALL";
        else return "WINTER";
    }

    @Override
    public void onBindViewHolder(DrawerAdapter.ViewHolder holder, int position) {
        if (!isPositionHeader(position)) {
            holder.itemTitle.setText(drawerTitles[position - 1]);

            if (drawerSubtitles[position - 1].length() != 0) {
                holder.itemSubtitle.setVisibility(View.VISIBLE);
                holder.itemSubtitle.setText(drawerSubtitles[position - 1]);
            } else {
                holder.itemSubtitle.setVisibility(View.GONE);
            }

            holder.imageView.setImageResource(drawerIcon.getResourceId(position - 1, 0));

            if (selectedItem == position) {
                holder.itemView.setBackgroundColor(selectedBackgroundColor);
            } else {
                holder.itemView.setBackgroundResource(selectableBackgroundResource);
            }

        } else {

            switch (getSeason()) {
                case "SPRING":
                    holder.imageView.setImageResource(R.drawable.spring);
                    break;

                case "SUMMER":
                    holder.imageView.setImageResource(R.drawable.summer);
                    break;

                case "FALL":
                    holder.imageView.setImageResource(R.drawable.fall);
                    break;

                case "WINTER":
                    holder.imageView.setImageResource(R.drawable.winter);
                    break;
            }
        }
    }

    public void setSelectedItem(int item) {
        selectedItem = item;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return drawerTitles.length + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }
}