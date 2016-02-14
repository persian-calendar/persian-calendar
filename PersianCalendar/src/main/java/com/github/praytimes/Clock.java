package com.github.praytimes;

import static com.github.praytimes.StaticUtils.fixHour;

public class Clock {
    private final int hour;
    private final int minute;

    public Clock(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    static Clock fromDouble(double arg) {
        arg = fixHour(arg + 0.5 / 60); // add 0.5 minutes to round
        int hour = (int) arg;
        int minute = (int) ((arg - hour) * 60d);
        return new Clock(hour, minute);
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getInt() {
        return (hour * 100) + minute;
    }
}