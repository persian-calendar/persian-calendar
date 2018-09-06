package com.byagowi.persiancalendar.view.selectdayview;

import calendar.CalendarType;

public interface SelectDayView {
    void setDayJdnOnView(long jdn);

    long getDayJdnFromView();

    CalendarType getSelectedCalendarType();

    interface OnSelectedDayChangedListener {
        void onSelectedDayChanged(long jdn);
    }

    void setOnSelectedDayChangedListener(OnSelectedDayChangedListener listener);
}
