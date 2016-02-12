package com.byagowi.persiancalendar.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.view.activity.MainActivity;
import com.malinskiy.materialicons.widget.IconTextView;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {
    private final MainActivity mainActivity;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    public int selectedItem = 0;
    private String[] drawerTitles;
    private String[] drawerSubtitles;
    private String[] drawerIcon;

    public DrawerAdapter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        drawerTitles = mainActivity.getResources().getStringArray(R.array.drawerTitles);
        drawerSubtitles = mainActivity.getResources().getStringArray(R.array.drawerSubtitles);
        drawerIcon = mainActivity.getResources().getStringArray(R.array.drawerIcons);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView itemTitle;
        private TextView itemSubtitle;
        private IconTextView itemIcon;
        private View background;

        public ViewHolder(View itemView, int ViewType) {
            super(itemView);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);

            if (ViewType == TYPE_ITEM) {
                itemTitle = (TextView) itemView.findViewById(R.id.itemTitle);
                itemSubtitle = (TextView) itemView.findViewById(R.id.itemSubtitle);
                itemIcon = (IconTextView) itemView.findViewById(R.id.ItemIcon);
                background = itemView.findViewById(R.id.background);
            }
        }

        @Override
        public void onClick(View view) {
            mainActivity.onClickItem(getAdapterPosition());
            selectedItem = getAdapterPosition();
            notifyDataSetChanged();
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
            holder.itemTitle.setText(drawerTitles[position - 1]);
            if (drawerSubtitles[position - 1].length() != 0) {
                holder.itemSubtitle.setVisibility(View.VISIBLE);
                holder.itemSubtitle.setText(drawerSubtitles[position - 1]);
            }
            holder.itemIcon.setText(drawerIcon[position - 1]);
            if (selectedItem == position) {
                holder.background.setVisibility(View.VISIBLE);
            } else {
                holder.background.setVisibility(View.GONE);
            }
        }
        holder.itemView.setSelected(false);
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