package com.byagowi.persiancalendar.adapter;

import android.content.Context;
import android.content.res.Resources;
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
    private List<DayEntity> days;
    private int selectedDay = -1;
    private boolean persianDigit;
    private Utils utils;
    private TypedValue colorHoliday = new TypedValue();
    private TypedValue colorTextHoliday = new TypedValue();
    private TypedValue colorPrimary = new TypedValue();
    private TypedValue colorDayName = new TypedValue();
    private TypedValue shapeSelectDay = new TypedValue();
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

        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.colorHoliday, colorHoliday, true);
        theme.resolveAttribute(R.attr.colorTextHoliday, colorTextHoliday, true);
        theme.resolveAttribute(R.attr.colorPrimary, colorPrimary, true);
        theme.resolveAttribute(R.attr.colorTextDayName, colorDayName, true);
        theme.resolveAttribute(R.attr.circleSelect, shapeSelectDay, true);
    }

    public void clearSelectedDay() {
        int prevDay = selectedDay;
        selectedDay = -1;
        notifyItemChanged(fixRtlPosition(prevDay));
    }

    public void selectDay(int dayOfMonth) {
        int prevDay = selectedDay;
        selectedDay = dayOfMonth + 6 + firstDayDayOfWeek;
        notifyItemChanged(fixRtlPosition(prevDay));
        notifyItemChanged(fixRtlPosition(selectedDay));
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView num;
        View today;
        View event;

        ViewHolder(View itemView) {
            super(itemView);

            num = (TextView) itemView.findViewById(R.id.num);
            today = itemView.findViewById(R.id.today);
            event = itemView.findViewById(R.id.event);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            utils.setFontAndShape(num);
        }

        @Override
        public void onClick(View v) {
            int position = fixRtlPosition(getAdapterPosition());
            if (totalDays < position - 6 - firstDayDayOfWeek) {
                return;
            }

            if (position - 7 - firstDayDayOfWeek >= 0) {
                monthFragment.onClickItem(days
                        .get(position - 7 - firstDayDayOfWeek)
                        .getPersianDate());

                int prevDay = selectedDay;
                selectedDay = position;
                notifyItemChanged(fixRtlPosition(prevDay));
                notifyItemChanged(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int position = fixRtlPosition(getAdapterPosition());
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
        position = fixRtlPosition(position);
        if (totalDays < position - 6 - firstDayDayOfWeek) {
            setEmpty(holder);
            return;
        }

        if (!isPositionHeader(position)) {
            if (position - 7 - firstDayDayOfWeek >= 0) {
                holder.num.setText(days.get(position - 7 - days.get(0).getDayOfWeek()).getNum());
                holder.num.setVisibility(View.VISIBLE);

                DayEntity day = days.get(position - 7 - firstDayDayOfWeek);

                if (persianDigit) {
                    holder.num.setTextSize(25);
                } else {
                    holder.num.setTextSize(20);
                }

                if (day.isEvent()) {
                    holder.event.setVisibility(View.VISIBLE);
                } else {
                    holder.event.setVisibility(View.GONE);
                }

                if (day.isToday()) {
                    holder.today.setVisibility(View.VISIBLE);
                } else {
                    holder.today.setVisibility(View.GONE);
                }

                if (position == selectedDay) {
                    holder.num.setBackgroundResource(shapeSelectDay.resourceId);

                    if (day.isHoliday()) {
                        holder.num.setTextColor(ContextCompat.getColor(context, colorTextHoliday.resourceId));
                    } else {
                        holder.num.setTextColor(ContextCompat.getColor(context, colorPrimary.resourceId));
                    }

                } else {
                    holder.num.setBackgroundResource(0);

                    if (day.isHoliday()) {
                        holder.num.setTextColor(ContextCompat.getColor(context, colorHoliday.resourceId));
                    } else {
                        holder.num.setTextColor(ContextCompat.getColor(context, R.color.dark_text_day));
                    }
                }

            } else {
                setEmpty(holder);
            }

        } else {
            holder.num.setText(Constants.FIRST_CHAR_OF_DAYS_OF_WEEK_NAME[position]);
            holder.num.setTextColor(ContextCompat.getColor(context, colorDayName.resourceId));
            holder.num.setTextSize(20);
            holder.today.setVisibility(View.GONE);
            holder.num.setBackgroundResource(0);
            holder.event.setVisibility(View.GONE);
            holder.num.setVisibility(View.VISIBLE);
        }
    }

    private void setEmpty(MonthAdapter.ViewHolder holder) {
        holder.today.setVisibility(View.GONE);
        holder.num.setVisibility(View.GONE);
        holder.event.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return 7 * 7; // days of week * month view rows
    }

    private boolean isPositionHeader(int position) {
        return position < 7;
    }

    private int fixRtlPosition(int position) {
        position += 6 - (position % 7) * 2;//equal:(6 - position % 7) + position - (position % 7)
        return position;
    }
}