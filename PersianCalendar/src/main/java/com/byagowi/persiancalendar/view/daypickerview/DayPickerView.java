package com.byagowi.persiancalendar.view.daypickerview;

import com.byagowi.persiancalendar.util.CalendarType;

public interface DayPickerView {
    void setDayJdnOnView(long jdn);

    long getDayJdnFromView();

    CalendarType getSelectedCalendarType();

    void setOnSelectedDayChangedListener(OnSelectedDayChangedListener listener);

    interface OnSelectedDayChangedListener {
        void onSelectedDayChanged(long jdn);
    }
}
