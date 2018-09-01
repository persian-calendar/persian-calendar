package com.byagowi.persiancalendar.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.entity.AbstractEvent;
import com.byagowi.persiancalendar.entity.DayEntity;
import com.byagowi.persiancalendar.entity.DeviceCalendarEvent;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.fragment.CalendarFragment;
import com.byagowi.persiancalendar.view.itemdayview.DaysPaintResources;
import com.byagowi.persiancalendar.view.itemdayview.ItemDayView;

import java.util.List;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.ViewHolder> {
    private SparseArray<List<DeviceCalendarEvent>> monthEvents = new SparseArray<>();
    private List<DayEntity> days;
    private boolean isArabicDigit;
    private final int startingDayOfWeek;
    private final int totalDays;
    private int weekOfYearStart;
    private int weeksCount;
    private final ViewGroup.LayoutParams layoutParams;
    private final DaysPaintResources daysPaintResources;

    public void initializeMonthEvents(Context context) {
        if (Utils.isShowDeviceCalendarEvents()) {
            monthEvents = CalendarUtils.readMonthDeviceEvents(context, days.get(0).getJdn());
        }
    }

    public MonthAdapter(Context context, List<DayEntity> days,
                        int startingDayOfWeek, int weekOfYearStart, int weeksCount) {
        this.startingDayOfWeek = Utils.fixDayOfWeekReverse(startingDayOfWeek);
        totalDays = days.size();
        this.days = days;
        this.weekOfYearStart = weekOfYearStart;
        this.weeksCount = weeksCount;
        initializeMonthEvents(context);
        isArabicDigit = Utils.isArabicDigitSelected();

        layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                context.getResources().getDimensionPixelSize(R.dimen.day_item_size));
        daysPaintResources = new DaysPaintResources(context);
    }

    private int selectedDay = -1;

    public void selectDay(int dayOfMonth) {
        int prevDay = selectedDay;
        selectedDay = -1;
        notifyItemChanged(prevDay);

        if (dayOfMonth == -1) return;

        selectedDay = dayOfMonth + 6 + startingDayOfWeek;
        if (Utils.isWeekOfYearEnabled()) {
            selectedDay = selectedDay + selectedDay / 7 + 1;
        }

        notifyItemChanged(selectedDay);
    }

    @Override
    public MonthAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemDayView itemDayView = new ItemDayView(parent.getContext(), daysPaintResources);
        itemDayView.setLayoutParams(layoutParams);
        return new ViewHolder(itemDayView);
    }

    private boolean hasAnyHolidays(List<AbstractEvent> dayEvents) {
        for (AbstractEvent event : dayEvents) {
            if (event.isHoliday()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDeviceEvents(List<AbstractEvent> dayEvents) {
        for (AbstractEvent event : dayEvents) {
            if (event instanceof DeviceCalendarEvent) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBindViewHolder(MonthAdapter.ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return 7 * (Utils.isWeekOfYearEnabled() ? 8 : 7); // days of week * month view rows
    }

    private static CalendarFragment getCalendarFragment(View view) {
        Context ctx = view.getContext();
        if (ctx != null && ctx instanceof FragmentActivity) {
            return (CalendarFragment) ((FragmentActivity) ctx).getSupportFragmentManager()
                    .findFragmentByTag(CalendarFragment.class.getName());
        }
        return null;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ViewHolder(ItemDayView itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ItemDayView itemDayView = (ItemDayView) v;
            long jdn = itemDayView.getJdn();
            if (jdn == -1) return;

            CalendarFragment calendarFragment = getCalendarFragment(v);
            if (calendarFragment != null) {
                calendarFragment.selectDay(jdn);
            }
            MonthAdapter.this.selectDay(itemDayView.getDayOfMonth());
        }

        @Override
        public boolean onLongClick(View v) {
            onClick(v);

            ItemDayView itemDayView = (ItemDayView) v;
            long jdn = itemDayView.getJdn();
            if (jdn == -1) return false;

            CalendarFragment calendarFragment = getCalendarFragment(v);
            if (calendarFragment != null) {
                calendarFragment.addEventOnCalendar(jdn);
            }

            return false;
        }

        void bind(int position) {
            int originalPosition = position;
            ItemDayView itemDayView = (ItemDayView) itemView;
            if (Utils.isWeekOfYearEnabled()) {
                if (position % 8 == 0) {
                    int row = position / 8;
                    if (row > 0 && row <= weeksCount) {
                        itemDayView.setNonDayOfMonthItem(
                                Utils.formatNumber(weekOfYearStart + row - 1),
                                daysPaintResources.weekNumberTextSize);

                        itemDayView.setVisibility(View.VISIBLE);
                    } else setEmpty();
                    return;
                }

                position = position - position / 8 - 1;
            }

            if (totalDays < position - 6 - startingDayOfWeek) {
                setEmpty();
            } else if (position < 7) {
                itemDayView.setNonDayOfMonthItem(
                        Utils.getInitialOfWeekDay(Utils.fixDayOfWeek(position)),
                        daysPaintResources.weekDaysInitialTextSize);

                itemDayView.setVisibility(View.VISIBLE);
            } else {
                if (position - 7 - startingDayOfWeek >= 0) {
                    DayEntity day = days.get(position - 7 - startingDayOfWeek);
                    List<AbstractEvent> events = Utils.getEvents(day.getJdn(), monthEvents);
                    boolean isHoliday = Utils.isWeekEnd(day.getDayOfWeek()) || hasAnyHolidays(events);

                    itemDayView.setDayOfMonthItem(day.isToday(), originalPosition == selectedDay,
                            events.size() > 0, hasDeviceEvents(events), isHoliday,
                            isArabicDigit
                                    ? daysPaintResources.arabicDigitsTextSize
                                    : daysPaintResources.persianDigitsTextSize,
                            day.getJdn(), position - 6 - startingDayOfWeek);

                    itemDayView.setVisibility(View.VISIBLE);
                } else {
                    setEmpty();
                }

            }
        }

        private void setEmpty() {
            itemView.setVisibility(View.GONE);
        }
    }
}