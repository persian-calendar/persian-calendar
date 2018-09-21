package com.byagowi.persiancalendar.view.daypickerview;

import calendar.CalendarType;

public interface DayPickerView {
    void setDayJdnOnView(long jdn);

    long getDayJdnFromView();

    CalendarType getSelectedCalendarType();

    interface OnSelectedDayChangedListener {
        void onSelectedDayChanged(long jdn);
    }

    void setOnSelectedDayChangedListener(OnSelectedDayChangedListener listener);
}
