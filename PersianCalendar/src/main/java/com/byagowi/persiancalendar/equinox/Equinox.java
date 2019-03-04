package com.byagowi.persiancalendar.equinox;

// Adopted from https://github.com/xyproto/calendar/blob/master/equinox.go
//// Equinox and solstice algorithms from Astronomical Algorithms by Jean Meeus
//// Adapted from jgiesen.de/astro/astroJS/seasons/seasons.js
////// Equinoxes and Solstices
////// algorithm from Meeus
////// adapted by Juergen Giesen
////// 6.5.2012

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Equinox {
    // One degree expressed in radians
    private final static double degrees = Math.PI / 180.0;

    //// Vårjevndogn / Vernal equinox / March equinox
    //func Equinox(year int) time.Time {
    //	// Source: http://www.phpro.org/examples/Get-Vernal-Equinox.html
    //	days_from_base := 79.3125 + float64(year - 1970) * 365.2425
    //	seconds_from_base := days_from_base * 86400.0
    //	return time.Unix(round(seconds_from_base), 0)
    //}

    private static double abs(double x) {
        return x < 0 ? -x : x;
    }

    private static double roundf(double x) {
        return Math.floor(0.5 + x);
    }

    private static long round(double x) {
        return (long) roundf(x);
    }

    private static double tableFormula(double x) {
        double result = 0;
        // TODO: Replace with a table and a loop
        result += 485 * Math.cos(degrees * (324.96 + x * 1934.136));
        result += 203 * Math.cos(degrees * (337.23 + x * 32964.467));
        result += 199 * Math.cos(degrees * (342.08 + x * 20.186));
        result += 182 * Math.cos(degrees * (27.85 + x * 445267.112));
        result += 156 * Math.cos(degrees * (73.14 + x * 45036.886));
        result += 136 * Math.cos(degrees * (171.52 + x * 22518.443));
        result += 77 * Math.cos(degrees * (222.54 + x * 65928.934));
        result += 74 * Math.cos(degrees * (296.72 + x * 3034.906));
        result += 70 * Math.cos(degrees * (243.58 + x * 9037.513));
        result += 58 * Math.cos(degrees * (119.81 + x * 33718.147));
        result += 52 * Math.cos(degrees * (297.17 + x * 150.678));
        result += 50 * Math.cos(degrees * (21.02 + x * 2281.226));
        result += 45 * Math.cos(degrees * (247.54 + x * 29929.562));
        result += 44 * Math.cos(degrees * (325.15 + x * 31555.956));
        result += 29 * Math.cos(degrees * (60.93 + x * 4443.417));
        result += 18 * Math.cos(degrees * (155.12 + x * 67555.328));
        result += 17 * Math.cos(degrees * (288.79 + x * 4562.452));
        result += 16 * Math.cos(degrees * (198.04 + x * 62894.029));
        result += 14 * Math.cos(degrees * (199.76 + x * 31436.921));
        result += 12 * Math.cos(degrees * (95.39 + x * 14577.848));
        result += 12 * Math.cos(degrees * (287.11 + x * 31931.756));
        result += 12 * Math.cos(degrees * (320.81 + x * 34777.259));
        result += 9 * Math.cos(degrees * (227.73 + x * 1222.114));
        result += 8 * Math.cos(degrees * (15.45 + x * 16859.074));
        return result;
    }

    // Calculate vårjevndøgn, sommersolverv, høstjevndøgn or vintersolverv
    private static Date calculateEquinoxOrSolstice(int year, double a) {
        // TODO: Simplify with a symbolic calculator
        double b = (a - 2451545.0) / 36525.0;
        double c = (35999.373 * b - 2.47) * degrees;
        double d = a + (0.00001 * tableFormula(b)) / (1.0 + 0.0334 * Math.cos(c) + 0.0007 * Math.cos(2 * c)) - (66.0 + ((double) (year - 2000)) * 1.0) / 86400.0;
        double e = roundf(d);
        double f = Math.floor((e - 1867216.25) / 36524.25);
        double g = e + f - Math.floor(f / 4) + 1525.0;
        double h = Math.floor((g - 122.1) / 365.25);
        double i = 365.0 * h + Math.floor(h / 4);
        double k = Math.floor((g - i) / 30.6001);
        double l = 24.0 * (d + 0.5 - e);
        int day = (int) (roundf(g - i) - Math.floor(30.6001 * k));
        double month = k - 1 - 12 * Math.floor(k / 14);
        int hour = (int) (Math.floor(l));
        double minutef = (abs(l) - Math.floor(abs(l))) * 60.0;
        int minute = (int) round(minutef);
        int second = (int) round((minutef - Math.floor(minutef)) * 60.0);
        if (minute == 60.0d) {
            minute = 0;
            hour += 1.0d;
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, (int) month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        return calendar.getTime();
    }

    // Spring equinox for the northern hemisphere
    public static Date northwardEquinox(int year) {
        double y = ((double) (year) - 2000.0) / 1000.0;
        double a = 2451623.80984 + 365242.37404 * y + 0.05169 * y * y - 0.00411 * y * y * y - 0.00057 * y * y * y * y;
        return calculateEquinoxOrSolstice(year, a);
    }

    // Summer solstice for the northern hemisphere
    public static Date northernSolstice(int year) {
        double y = ((double) (year) - 2000.0) / 1000.0;
        double a = 2451716.56767 + 365241.62603 * y + 0.00325 * y * y + 0.00888 * y * y * y - 0.00030 * y * y * y * y;
        return calculateEquinoxOrSolstice(year, a);
    }

    // Autumn equinox for the northern hemisphere
    public static Date southwardEquinox(int year) {
        double y = ((double) (year) - 2000.0) / 1000.0;
        double a = 2451810.21715 + 365242.01767 * y - 0.11575 * y * y + 0.00337 * y * y * y + 0.00078 * y * y * y * y;
        return calculateEquinoxOrSolstice(year, a);
    }

    // Winter solstice for the northern hemisphere
    public static Date southernSolstice(int year) {
        double y = ((double) (year) - 2000.0) / 1000.0;
        double a = 2451900.05952 + 365242.74049 * y - 0.06223 * y * y - 0.00823 * y * y * y + 0.00032 * y * y * y * y;
        return calculateEquinoxOrSolstice(year, a);
    }
}
