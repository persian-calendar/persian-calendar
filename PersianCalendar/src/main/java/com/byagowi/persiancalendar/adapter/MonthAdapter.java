package com.byagowi.persiancalendar.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.entity.Day;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.view.fragment.MonthNewFragment;

import java.util.List;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.ViewHolder> {
    private final Context context;
    private final MonthNewFragment monthNewFragment;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_DAY = 1;
    private List<Day> days;
    public int select_Day = -1;
    private boolean pesianDigit;

    public MonthAdapter(Context context, MonthNewFragment monthNewFragment, List<Day> days) {
        this.monthNewFragment = monthNewFragment;
        this.context = context;
        this.days = days;
        pesianDigit = Utils.isPersianDigitSelected(context);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView num;
        View today;
        View selectDay;
        View event;

        public ViewHolder(View itemView) {
            super(itemView);

            num = (TextView) itemView.findViewById(R.id.num);
            today = itemView.findViewById(R.id.today);
            selectDay = itemView.findViewById(R.id.select_day);
            event = itemView.findViewById(R.id.event);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
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

        @Override
        public boolean onLongClick(View v) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                try {
                    monthNewFragment.onLongClickItem(days
                            .get(getAdapterPosition() - 7 - days.get(0).getDayOfWeek())
                            .getPersianDate());
                } catch (Exception e) {
                    // Ignore it for now
                    // I guess it will occur on CyanogenMod phones
                    // where Google extra things is not installed
                }
            }
            return false;
        }
    }

    @Override
    public MonthAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.day_item, parent, false);

        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(MonthAdapter.ViewHolder holder, int position) {
        if (!isPositionHeader(position)) {
            if (position - 7 - days.get(0).getDayOfWeek() >= 0) {
                holder.num.setText(days.get(position - 7 - days.get(0).getDayOfWeek()).getNum());
                holder.num.setVisibility(View.VISIBLE);

                if (pesianDigit) {
                    holder.num.setTextSize(25);
                } else {
                    holder.num.setTextSize(20);
                }

                if (days.get(position - 7 - days.get(0).getDayOfWeek()).isHoliday()) {
                    holder.num.setTextColor(ContextCompat.getColor(context, R.color.holiday));
                } else {
                    holder.num.setTextColor(ContextCompat.getColor(context, R.color.first_row_text_color));
                }

                if (days.get(position - 7 - days.get(0).getDayOfWeek()).isEvent()) {
                    holder.event.setVisibility(View.VISIBLE);
                } else {
                    holder.event.setVisibility(View.GONE);
                }

                if (days.get(position - 7 - days.get(0).getDayOfWeek()).isToday()) {
                    holder.today.setVisibility(View.VISIBLE);
                } else {
                    holder.today.setVisibility(View.GONE);
                }

                if (position == select_Day) {
                    holder.selectDay.setVisibility(View.VISIBLE);

                    if (days.get(position - 7 - days.get(0).getDayOfWeek()).isHoliday()) {
                        holder.num.setTextColor(ContextCompat.getColor(context, R.color.holiday));
                    } else {
                        holder.num.setTextColor(ContextCompat.getColor(context, R.color.winter_color));
                    }
                } else {
                    holder.selectDay.setVisibility(View.GONE);
                }

            } else {
                holder.today.setVisibility(View.GONE);
                holder.selectDay.setVisibility(View.GONE);
                holder.num.setVisibility(View.GONE);
                holder.event.setVisibility(View.GONE);
            }

        } else {
            holder.num.setText(Utils.firstCharOfDaysOfWeekName[position]);
            holder.num.setTextColor(ContextCompat.getColor(context, R.color.first_row_text_color2));
            holder.num.setTextSize(20);
            holder.today.setVisibility(View.GONE);
            holder.selectDay.setVisibility(View.GONE);
            holder.event.setVisibility(View.GONE);
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