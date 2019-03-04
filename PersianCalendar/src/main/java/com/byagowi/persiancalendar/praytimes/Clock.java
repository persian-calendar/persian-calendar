package com.byagowi.persiancalendar.praytimes;

import java.util.Calendar;

import static com.byagowi.persiancalendar.praytimes.Utils.fixHour;

public class Clock {
    private static final int MINUTES_PER_HOUR = 60;
    private final int hour;
    private final int minute;

    public Clock(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public Clock(Calendar calendar) {
        this(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    static Clock fromDouble(double arg) {
        arg = fixHour(arg + 0.5 / 60); // add 0.5 minutes to round
        int hour = (int) arg;
        int minute = (int) ((arg - hour) * 60d);
        return new Clock(hour, minute);
    }

    public static Clock fromInt(int minutes) {
        return new Clock(minutes / MINUTES_PER_HOUR, minutes % MINUTES_PER_HOUR);
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int toInt() {
        return (hour * MINUTES_PER_HOUR + minute);
    }
}
