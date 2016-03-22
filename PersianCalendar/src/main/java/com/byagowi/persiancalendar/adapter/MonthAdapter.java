package com.byagowi.persiancalendar.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.entity.DayEntity;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.fragment.MonthFragment;

import java.util.List;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.ViewHolder> {
    private Context context;
    private MonthFragment monthFragment;
    private final int TYPE_HEADER = 0;
    private final int TYPE_DAY = 1;
    private List<DayEntity> days;
    private int selectedDay = -1;
    private boolean persianDigit;
    private Utils utils;
    private TypedValue colorHoliday = new TypedValue();
    private TypedValue colorTextHoliday = new TypedValue();
    private TypedValue colorPrimary = new TypedValue();
    private TypedValue colorDayName = new TypedValue();
    private final int firstDayDayOfWeek;
    private final int totalDays;

    public MonthAdapter(Context context, MonthFragment monthFragment, List<DayEntity> days) {
        firstDayDayOfWeek = days.get(0).getDayOfWeek();
        totalDays = days.size();
        this.monthFragment = monthFragment;
        this.context = context;
        this.days = days;
        utils = Utils.getInstance(context);
        persianDigit = utils.isPersianDigitSelected();
        context.getTheme().resolveAttribute(R.attr.colorHoliday, colorHoliday, true);
        context.getTheme().resolveAttribute(R.attr.colorTextHoliday, colorTextHoliday, true);
        context.getTheme().resolveAttribute(R.attr.colorPrimary, colorPrimary, true);
        context.getTheme().resolveAttribute(R.attr.colorTextDayName, colorDayName, true);
    }

    public void clearSelectedDay() {
        selectedDay = -1;
        notifyDataSetChanged();
    }

    public void selectDay(int dayOfMonth) {
        selectedDay = dayOfMonth + 6 + firstDayDayOfWeek;
        notifyDataSetChanged();
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
            int position = getAdapterPosition();
            position += 6 - (position % 7) * 2;
            if (totalDays < position - 6 - firstDayDayOfWeek) {
                return;
            }

            if (position - 7 - firstDayDayOfWeek >= 0) {
                monthFragment.onClickItem(days
                        .get(position - 7 - firstDayDayOfWeek)
                        .getPersianDate());

                selectedDay = position;
                notifyDataSetChanged();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            position += 6 - (position % 7) * 2;
            if (totalDays < position - 6 - firstDayDayOfWeek) {
                return false;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                try {
                    monthFragment.onLongClickItem(days
                            .get(position - 7 - firstDayDayOfWeek)
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
        View v = LayoutInflater.from(context).inflate(R.layout.item_day, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MonthAdapter.ViewHolder holder, int position) {
        position += 6 - (position % 7) * 2;
        if (totalDays < position - 6 - firstDayDayOfWeek) {
            return;
        }
        if (!isPositionHeader(position)) {
            if (position - 7 - firstDayDayOfWeek >= 0) {
                holder.num.setText(days.get(position - 7 - days.get(0).getDayOfWeek()).getNum());
                holder.num.setVisibility(View.VISIBLE);

                if (persianDigit) {
                    holder.num.setTextSize(25);
                } else {
                    holder.num.setTextSize(20);
                }

                if (days.get(position - 7 - firstDayDayOfWeek).isHoliday()) {
                    holder.num.setTextColor(ContextCompat.getColor(context, colorHoliday.resourceId));
                } else {
                    holder.num.setTextColor(ContextCompat.getColor(context, R.color.dark_text_day));
                }

                if (days.get(position - 7 - firstDayDayOfWeek).isEvent()) {
                    holder.event.setVisibility(View.VISIBLE);
                } else {
                    holder.event.setVisibility(View.GONE);
                }

                if (days.get(position - 7 - firstDayDayOfWeek).isToday()) {
                    holder.today.setVisibility(View.VISIBLE);
                } else {
                    holder.today.setVisibility(View.GONE);
                }

                if (position == selectedDay) {
                    holder.selectDay.setVisibility(View.VISIBLE);

                    if (days.get(position - 7 - firstDayDayOfWeek).isHoliday()) {
                        holder.num.setTextColor(ContextCompat.getColor(context, colorTextHoliday.resourceId));
                    } else {
                        holder.num.setTextColor(ContextCompat.getColor(context, colorPrimary.resourceId));
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
            utils.setFontAndShape(holder.num);
        } else {
            holder.num.setText(Constants.FIRST_CHAR_OF_DAYS_OF_WEEK_NAME[position]);
            holder.num.setTextColor(ContextCompat.getColor(context, colorDayName.resourceId));
            holder.num.setTextSize(20);
            holder.today.setVisibility(View.GONE);
            holder.selectDay.setVisibility(View.GONE);
            holder.event.setVisibility(View.GONE);
            holder.num.setVisibility(View.VISIBLE);
            utils.setFont(holder.num);
        }
    }

    @Override
    public int getItemCount() {
        return 7 * 7; // days of week * month view rows
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