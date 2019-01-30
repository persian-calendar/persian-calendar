package com.byagowi.persiancalendar.util;

import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.icu.util.ChineseCalendar;
import android.icu.util.IslamicCalendar;
import android.icu.util.ULocale;
import android.os.Build;

import com.byagowi.persiancalendar.calendar.CivilDate;

import androidx.annotation.RequiresApi;

public class AndroidCalendar {
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static long chineseToJdn(int year, int month, int day) {
        Calendar instance = IslamicCalendar.getInstance();
        instance.set(year, month, day);

        instance.getTime();
        CivilDate civilDate = Utils.calendarToCivilDate(Utils.makeCalendarFromDate(instance.getTime()));
        return civilDate.toJdn();
    }

//    @RequiresApi(api = Build.VERSION_CODES.N)
//    public static int[] jdnToChinese(long jdn) {
//        Calendar instance = new ChineseCalendar();
//        instance.setTime(Utils.civilDateToCalendar(new CivilDate(jdn)).getTime());
//
//        return new int[]{instance.get(IslamicCalendar.YEAR), instance.get(Calendar.MONTH),
//                instance.get(Calendar.DATE)};
//    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String jdnToChineseString(long jdn) {
        ChineseCalendar instance = new ChineseCalendar();
        instance.setTime(Utils.civilDateToCalendar(new CivilDate(jdn)).getTime());

//        return instance.getDateTimeFormat(DateFormat.MEDIUM, DateFormat.NONE, new ULocale("zh", "CH", ""))
//                .format(instance.getTime());
        return instance.get(ChineseCalendar.YEAR) + "//" + instance.get(ChineseCalendar.MONTH) + "/" +
                instance.get(ChineseCalendar.DATE);
    }
}
