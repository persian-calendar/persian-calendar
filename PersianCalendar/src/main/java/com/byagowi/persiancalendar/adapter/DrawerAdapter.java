package com.byagowi.persiancalendar.adapter;

import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.MainActivity;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {
    private final MainActivity mainActivity;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private int selectedItem;
    private String[] drawerTitles;
    private String[] drawerSubtitles;
    private TypedArray drawerIcon;
    private Utils utils;

    public DrawerAdapter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        utils = Utils.getInstance(mainActivity);
        drawerTitles = mainActivity.getResources().getStringArray(R.array.drawerTitles);
        drawerSubtitles = mainActivity.getResources().getStringArray(R.array.drawerSubtitles);
        drawerIcon = mainActivity.getResources().obtainTypedArray(R.array.drawerIcons);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView itemTitle;
        private TextView itemSubtitle;
        private AppCompatImageView imageView;
        private View background;

        ViewHolder(View itemView, int viewType) {
            super(itemView);

            if (viewType == TYPE_ITEM) {
                itemView.setOnClickListener(this);
                itemTitle = itemView.findViewById(R.id.itemTitle);
                itemSubtitle = itemView.findViewById(R.id.itemSubtitle);
                imageView = itemView.findViewById(R.id.ItemIcon);
                background = itemView.findViewById(R.id.background);

                utils.setFont(itemTitle);
                utils.setFont(itemSubtitle);
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

    @Override
    public void onBindViewHolder(DrawerAdapter.ViewHolder holder, int position) {
        if (!isPositionHeader(position)) {
            holder.itemTitle.setText(utils.shape(drawerTitles[position - 1]));

            if (drawerSubtitles[position - 1].length() != 0) {
                holder.itemSubtitle.setVisibility(View.VISIBLE);
                holder.itemSubtitle.setText(utils.shape(drawerSubtitles[position - 1]));
            } else {
                holder.itemSubtitle.setVisibility(View.GONE);
            }

            holder.imageView.setImageResource(drawerIcon.getResourceId(position - 1, 0));

            if (selectedItem == position) {
                holder.background.setVisibility(View.VISIBLE);
            } else {
                holder.background.setVisibility(View.GONE);
            }

        } else {

            switch (utils.getSeason()) {
                case SPRING:
                    holder.imageView.setImageResource(R.drawable.spring);
                    break;

                case SUMMER:
                    holder.imageView.setImageResource(R.drawable.summer);
                    break;

                case FALL:
                    holder.imageView.setImageResource(R.drawable.fall);
                    break;

                case WINTER:
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