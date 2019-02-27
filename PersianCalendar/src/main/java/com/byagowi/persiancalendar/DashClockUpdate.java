package com.byagowi.persiancalendar;

import android.content.Intent;

import com.byagowi.persiancalendar.calendar.AbstractDate;
import com.byagowi.persiancalendar.ui.MainActivity;
import com.byagowi.persiancalendar.utils.CalendarType;
import com.byagowi.persiancalendar.utils.Utils;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

public class DashClockUpdate extends DashClockExtension {
    @Override
    protected void onUpdateData(int reason) {
        setUpdateWhenScreenOn(true);
        CalendarType mainCalendar = Utils.getMainCalendar();
        long jdn = Utils.getTodayJdn();
        AbstractDate date = Utils.getDateFromJdnOfCalendar(mainCalendar, jdn);
        publishUpdate(new ExtensionData().visible(true)
                .icon(Utils.getDayIconResource(date.getDayOfMonth()))
                .status(Utils.getMonthName(date))
                .expandedTitle(Utils.dayTitleSummary(date))
                .expandedBody(Utils.dateStringOfOtherCalendars(jdn, Utils.getSpacedComma()))
                .clickIntent(new Intent(getApplicationContext(), MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
    }
}
