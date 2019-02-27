package com.byagowi.persiancalendar.ui.shared;

import com.byagowi.persiancalendar.utils.CalendarType;

public interface DayPickerView {
    void setDayJdnOnView(long jdn);

    long getDayJdnFromView();

    CalendarType getSelectedCalendarType();

    void setOnSelectedDayChangedListener(OnSelectedDayChangedListener listener);

    interface OnSelectedDayChangedListener {
        void onSelectedDayChanged(long jdn);
    }
}
