package com.byagowi.persiancalendar.praytimes;

class Utils {
    static double dtr(double d) {
        return (d * Math.PI) / 180d;
    }

    static double rtd(double r) {
        return (r * 180d) / Math.PI;
    }

    static MinuteOrAngleDouble deg(int value) {
        return deg((double) value);
    }

    public static MinuteOrAngleDouble min(int value) {
        return new MinuteOrAngleDouble((double) value, true);
    }

    static MinuteOrAngleDouble deg(double value) {
        return new MinuteOrAngleDouble(value, false);
    }

    static double fixHour(double a) {
        double result = a % 24;
        return result < 0 ? 24 + result : result;
    }
}
