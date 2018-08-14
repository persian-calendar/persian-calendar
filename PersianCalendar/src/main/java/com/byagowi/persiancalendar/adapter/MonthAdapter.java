package com.byagowi.persiancalendar.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.entity.AbstractEvent;
import com.byagowi.persiancalendar.entity.DayEntity;
import com.byagowi.persiancalendar.entity.DeviceCalendarEvent;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.fragment.MonthFragment;

import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.ViewHolder> {
    private Context context;
    private MonthFragment monthFragment;
    private List<DayEntity> days;
    private boolean isArabicDigit;
    private final int startingDayOfWeek;
    private final int totalDays;
    private int weekOfYearStart;
    private int weeksCount;

    @ColorInt
    private int colorHoliday;
    @ColorInt
    private int colorTextHoliday;
    @ColorInt
    private int colorTextDay;
    @ColorInt
    private int colorPrimary;
    @ColorInt
    private int colorDayName;
    @DrawableRes
    private int shapeSelectDay;

    public MonthAdapter(Context context, MonthFragment monthFragment, List<DayEntity> days,
                        int startingDayOfWeek, int weekOfYearStart, int weeksCount) {
        this.startingDayOfWeek = Utils.fixDayOfWeekReverse(startingDayOfWeek);
        totalDays = days.size();
        this.monthFragment = monthFragment;
        this.context = context;
        this.days = days;
        this.weekOfYearStart = weekOfYearStart;
        this.weeksCount = weeksCount;
        isArabicDigit = Utils.isArabicDigitSelected();

        Resources.Theme theme = context.getTheme();

        TypedValue colorHolidayAttr = new TypedValue();
        theme.resolveAttribute(R.attr.colorHoliday, colorHolidayAttr, true);
        colorHoliday = ContextCompat.getColor(context, colorHolidayAttr.resourceId);

        TypedValue colorTextHolidayAttr = new TypedValue();
        theme.resolveAttribute(R.attr.colorTextHoliday, colorTextHolidayAttr, true);
        colorTextHoliday = ContextCompat.getColor(context, colorTextHolidayAttr.resourceId);

        TypedValue colorTextDayAttr = new TypedValue();
        theme.resolveAttribute(R.attr.colorTextDay, colorTextDayAttr, true);
        colorTextDay = ContextCompat.getColor(context, colorTextDayAttr.resourceId);

        TypedValue colorPrimaryAttr = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimary, colorPrimaryAttr, true);
        colorPrimary = ContextCompat.getColor(context, colorPrimaryAttr.resourceId);

        TypedValue colorDayNameAttr = new TypedValue();
        theme.resolveAttribute(R.attr.colorTextDayName, colorDayNameAttr, true);
        colorDayName = ContextCompat.getColor(context, colorDayNameAttr.resourceId);

        TypedValue shapeSelectDayAttr = new TypedValue();
        theme.resolveAttribute(R.attr.circleSelect, shapeSelectDayAttr, true);
        shapeSelectDay = shapeSelectDayAttr.resourceId;
    }

    private int selectedDay = -1;

    public void selectDay(int dayOfMonth) {
        int prevDay = selectedDay;
        selectedDay = -1;
        notifyItemChanged(prevDay);

        if (dayOfMonth == -1) {
            return;
        }

        selectedDay = dayOfMonth + 6 + startingDayOfWeek;
        if (Utils.isWeekOfYearEnabled()) {
            selectedDay = selectedDay + selectedDay / 7 + 1;
        }

        notifyItemChanged(selectedDay);
    }

    @Override
    public MonthAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_day, parent, false);

        return new ViewHolder(v);
    }

    private boolean hasAnyHolidays(List<AbstractEvent> dayEvents) {
        for (AbstractEvent event : dayEvents)
            if (event.isHoliday())
                return true;
        return false;
    }

    private boolean hasDeviceEvents(List<AbstractEvent> dayEvents) {
        for (AbstractEvent event : dayEvents)
            if (event instanceof DeviceCalendarEvent)
                return true;
        return false;
    }

    @Override
    public void onBindViewHolder(MonthAdapter.ViewHolder holder, int position) {
        holder.bindMonth(position);
    }

    @Override
    public int getItemCount() {
        return 7 * (Utils.isWeekOfYearEnabled() ? 8 : 7); // days of week * month view rows
    }

    private boolean isPositionHeader(int position) {
        return position < 7;
    }

    private int fixForWeekOfYearNumber(int position) {
        return position - position / 8 - 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView num;
        View today;
        View event;
        View deviceEvent;

        ViewHolder(View itemView) {
            super(itemView);

            // We deliberately like to avoid DataBinding thing here, at least for now
            num = itemView.findViewById(R.id.num);
            today = itemView.findViewById(R.id.today);
            event = itemView.findViewById(R.id.event);
            deviceEvent = itemView.findViewById(R.id.and_device_event);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (Utils.isWeekOfYearEnabled()) {
                if (position % 8 == 0) {
                    return;
                }

                position = fixForWeekOfYearNumber(position);
            }

            if (totalDays < position - 6 - startingDayOfWeek ||
                    position - 7 - startingDayOfWeek < 0) {
                return;
            }

            monthFragment.onClickItem(days.get(position - 7 - startingDayOfWeek).getJdn());
            MonthAdapter.this.selectDay(1 + position - 7 - startingDayOfWeek);
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            if (Utils.isWeekOfYearEnabled()) {
                if (position % 8 == 0) {
                    return false;
                }

                position = fixForWeekOfYearNumber(position);
            }

            if (totalDays < position - 6 - startingDayOfWeek ||
                    position - 7 - startingDayOfWeek < 0) {
                return false;
            }

            monthFragment.onLongClickItem(days.get(position - 7 - startingDayOfWeek).getJdn());
            onClick(v);

            return false;
        }

        void bindMonth(int position) {
            int originalPosition = position;
            if (Utils.isWeekOfYearEnabled()) {
                if (position % 8 == 0) {
                    int row = position / 8;
                    if (row > 0 && row <= weeksCount) {
                        num.setText(Utils.formatNumber(weekOfYearStart + row - 1));
                        num.setTextColor(colorDayName);
                        num.setTextSize(12);
                        num.setBackgroundResource(0);
                        num.setVisibility(View.VISIBLE);
                        today.setVisibility(View.GONE);
                        event.setVisibility(View.GONE);
                        deviceEvent.setVisibility(View.GONE);
                    } else setEmpty();
                    return;
                }

                position = fixForWeekOfYearNumber(position);
            }

            if (totalDays < position - 6 - startingDayOfWeek) {
                setEmpty();
            } else if (isPositionHeader(position)) {
                num.setText(Utils.getInitialOfWeekDay(Utils.fixDayOfWeek(position)));
                num.setTextColor(colorDayName);
                num.setTextSize(20);
                today.setVisibility(View.GONE);
                num.setBackgroundResource(0);
                event.setVisibility(View.GONE);
                deviceEvent.setVisibility(View.GONE);
                num.setVisibility(View.VISIBLE);
            } else {
                if (position - 7 - startingDayOfWeek >= 0) {
                    num.setText(Utils.formatNumber(1 + position - 7 - startingDayOfWeek));
                    num.setVisibility(View.VISIBLE);

                    DayEntity day = days.get(position - 7 - startingDayOfWeek);

                    num.setTextSize(isArabicDigit ? 20 : 25);

                    List<AbstractEvent> events = Utils.getEvents(day.getJdn());
                    boolean isEvent = false,
                            isHoliday = false;
                    if (Utils.isWeekEnd(day.getDayOfWeek()) || hasAnyHolidays(events)) {
                        isHoliday = true;
                    }
                    if (events.size() > 0) {
                        isEvent = true;
                    }

                    event.setVisibility(isEvent ? View.VISIBLE : View.GONE);
                    deviceEvent.setVisibility(hasDeviceEvents(events) ? View.VISIBLE : View.GONE);
                    today.setVisibility(day.isToday() ? View.VISIBLE : View.GONE);

                    if (originalPosition == selectedDay) {
                        num.setBackgroundResource(shapeSelectDay);
                        num.setTextColor(isHoliday ? colorTextHoliday : colorPrimary);

                    } else {
                        num.setBackgroundResource(0);
                        num.setTextColor(isHoliday ? colorHoliday : colorTextDay);
                    }

                } else {
                    setEmpty();
                }

            }
        }

        private void setEmpty() {
            today.setVisibility(View.GONE);
            num.setVisibility(View.GONE);
            event.setVisibility(View.GONE);
            deviceEvent.setVisibility(View.GONE);
        }
    }
}