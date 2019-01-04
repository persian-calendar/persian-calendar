package com.byagowi.persiancalendar;

import android.content.Intent;

import com.byagowi.persiancalendar.calendar.AbstractDate;
import com.byagowi.persiancalendar.util.CalendarType;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.MainActivity;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

public class DashClockUpdate extends DashClockExtension {
    @Override
    protected void onUpdateData(int reason) {
        setUpdateWhenScreenOn(true);
        CalendarType mainCalendar = Utils.getMainCalendar();
        long jdn = CalendarUtils.getTodayJdn();
        AbstractDate date = CalendarUtils.getDateFromJdnOfCalendar(mainCalendar, jdn);
        publishUpdate(new ExtensionData().visible(true)
                .icon(Utils.getDayIconResource(date.getDayOfMonth()))
                .status(CalendarUtils.getMonthName(date))
                .expandedTitle(CalendarUtils.dayTitleSummary(date))
                .expandedBody(Utils.dateStringOfOtherCalendars(jdn, Utils.getSpacedComma()))
                .clickIntent(new Intent(getApplicationContext(), MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
    }
}
