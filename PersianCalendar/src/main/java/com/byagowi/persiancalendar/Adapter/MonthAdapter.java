package com.byagowi.persiancalendar.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.Entity.Day;
import com.byagowi.persiancalendar.Interface.ClickListener;
import com.byagowi.persiancalendar.R;

import java.util.List;


public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.ViewHolder> {
    private final Context context;
    private final ClickListener clickListener;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_DAY = 1;
    private List<Day> days;

    public MonthAdapter(Context context, ClickListener clickListener, List<Day> days) {
        this.clickListener = clickListener;
        this.context = context;
        this.days = days;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView num;

        public ViewHolder(View itemView, int ViewType) {
            super(itemView);

            num = (TextView) itemView.findViewById(R.id.num);
        }

    }

    @Override
    public MonthAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.day_item, parent, false);

        return new ViewHolder(v, viewType);

    }

    @Override
    public void onBindViewHolder(MonthAdapter.ViewHolder holder, int position) {
        if (!isPositionHeader(position)) {
            if (position - 7 - days.get(0).getDayOfWeek() >= 0) {
                holder.num.setText(days.get(position - 7 - days.get(0).getDayOfWeek()).getNum());
            }
        } else {
            holder.num.setText("ش");
        }
    }

    @Override
    public int getItemCount() {
        return days.size() + days.get(0).getDayOfWeek() + 7;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        } else {
            return TYPE_DAY;
        }
    }

    private boolean isPositionHeader(int position) {
        return position < 7;
    }
}