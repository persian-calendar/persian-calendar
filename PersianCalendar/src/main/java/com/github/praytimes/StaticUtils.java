package com.github.praytimes;

class StaticUtils {
    public static double dtr(double d) {
        return (d * Math.PI) / 180d;
    }

    public static double rtd(double r) {
        return (r * 180d) / Math.PI;
    }

    public static MinuteOrAngleDouble deg(int value) {
        return deg((double) value);
    }

    public static MinuteOrAngleDouble min(int value) {
        return min((double) value);
    }

    public static MinuteOrAngleDouble deg(double value) {
        return new MinuteOrAngleDouble(value, false);
    }

    private static MinuteOrAngleDouble min(double value) {
        return new MinuteOrAngleDouble(value, true);
    }

    public static double fixHour(double a) {
        return fix(a, 24);
    }

    private static double fix(double a, double b) {
        double result = a % b;
        if (result < 0)
            result = b + result;
        return result;
    }
}
