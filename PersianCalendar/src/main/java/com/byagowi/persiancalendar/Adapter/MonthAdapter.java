package com.byagowi.persiancalendar.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.Entity.Day;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.view.Fragment.MonthNewFragment;

import java.util.List;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.ViewHolder> {
    private final Context context;
    private final MonthNewFragment monthNewFragment;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_DAY = 1;
    private List<Day> days;
    private int select_Day = -1;

    public MonthAdapter(Context context, MonthNewFragment monthNewFragment, List<Day> days) {
        this.monthNewFragment = monthNewFragment;
        this.context = context;
        this.days = days;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView num;
        View today;
        View selectDay;

        public ViewHolder(View itemView, int ViewType) {
            super(itemView);

            num = (TextView) itemView.findViewById(R.id.num);
            today = itemView.findViewById(R.id.today);
            selectDay = itemView.findViewById(R.id.select_day);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() - 7 - days.get(0).getDayOfWeek() >= 0) {
                monthNewFragment.onClickItem(days
                        .get(getAdapterPosition() - 7 - days.get(0).getDayOfWeek())
                        .getPersianDate());

                select_Day = getAdapterPosition();
                notifyDataSetChanged();
            }
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
                holder.num.setTextColor(context.getResources().getColor(R.color.first_row_text_color));
                holder.num.setTextSize(25);
                holder.today.setVisibility(View.GONE);
                holder.selectDay.setVisibility(View.GONE);
                holder.num.setVisibility(View.VISIBLE);

                if (days.get(position - 7 - days.get(0).getDayOfWeek()).isHoliday()) {
                    holder.num.setTextColor(context.getResources().getColor(R.color.holiday));
                }

                if (days.get(position - 7 - days.get(0).getDayOfWeek()).isToday()) {
                    holder.today.setVisibility(View.VISIBLE);
                }

                if (position == select_Day) {
                    holder.selectDay.setVisibility(View.VISIBLE);

                    if (days.get(position - 7 - days.get(0).getDayOfWeek()).isHoliday()) {
                        holder.num.setTextColor(context.getResources().getColor(R.color.holiday));
                    } else {
                        holder.num.setTextColor(context.getResources().getColor(R.color.first_row_background_color));
                    }

                }

            } else {
                holder.today.setVisibility(View.GONE);
                holder.selectDay.setVisibility(View.GONE);
                holder.num.setVisibility(View.GONE);
            }

        } else {
            holder.num.setText(Utils.firstCharOfDaysOfWeekName[position]);
            holder.num.setTextColor(context.getResources().getColor(R.color.first_row_text_color2));
            holder.num.setTextSize(20);
            holder.today.setVisibility(View.GONE);
            holder.selectDay.setVisibility(View.GONE);
            holder.num.setVisibility(View.VISIBLE);
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