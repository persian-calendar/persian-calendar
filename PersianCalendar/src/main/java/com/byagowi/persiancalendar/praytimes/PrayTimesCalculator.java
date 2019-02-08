package com.byagowi.persiancalendar.praytimes;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.byagowi.persiancalendar.praytimes.Utils.deg;
import static com.byagowi.persiancalendar.praytimes.Utils.dtr;
import static com.byagowi.persiancalendar.praytimes.Utils.fixHour;
import static com.byagowi.persiancalendar.praytimes.Utils.min;
import static com.byagowi.persiancalendar.praytimes.Utils.rtd;

public class PrayTimesCalculator {
    // default times
    private static final double DEFAULT_IMSAK = 5d / 24;
    private static final double DEFAULT_FAJR = 5d / 24;
    private static final double DEFAULT_SUNRISE = 6d / 24;
    private static final double DEFAULT_DHUHR = 12d / 24;
    private static final double DEFAULT_ASR = 13d / 24;
    private static final double DEFAULT_SUNSET = 18d / 24;
    private static final double DEFAULT_MAGHRIB = 18d / 24;
    private static final double DEFAULT_ISHA = 18d / 24;
    private static final MinuteOrAngleDouble DEFAULT_TIME_IMSAK = min(10);
    private static final MinuteOrAngleDouble DEFAULT_TIME_DHUHR = min(0);
    private static final CalculationMethod.AsrJuristics ASR_METHOD = CalculationMethod.AsrJuristics.Standard;
    private static final CalculationMethod.HighLatMethods HIGH_LATS_METHOD = CalculationMethod.HighLatMethods.NightMiddle;

    //
    // Calculation Logic
    //
    //
    public static PrayTimes calculate(CalculationMethod method, Date date, Coordinate coordinate,
                                      Double timeZone, Boolean dst) {
        double mTimeZone = timeZone != null ? timeZone : getTimeZone(date);
        boolean _dst = dst != null ? dst : getDst(date);
        if (_dst) {
            mTimeZone++;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        double jdate = julian(year, month, day) - coordinate.getLongitude() / (15d * 24d);

        // compute prayer times at given julian date
        double imsak = sunAngleTime(jdate, DEFAULT_TIME_IMSAK, DEFAULT_IMSAK, true, coordinate);
        double fajr = sunAngleTime(jdate, method.getFajr(), DEFAULT_FAJR, true, coordinate);
        double sunrise = sunAngleTime(jdate, riseSetAngle(coordinate), DEFAULT_SUNRISE, true, coordinate);
        double dhuhr = midDay(jdate, DEFAULT_DHUHR);
        double asr = asrTime(jdate, asrFactor(), DEFAULT_ASR, coordinate);
        double sunset = sunAngleTime(jdate, riseSetAngle(coordinate), DEFAULT_SUNSET, coordinate);
        double maghrib = sunAngleTime(jdate, method.getMaghrib(), DEFAULT_MAGHRIB, coordinate);
        double isha = sunAngleTime(jdate, method.getIsha(), DEFAULT_ISHA, coordinate);

        // Adjust times
        {
            double addToAll = mTimeZone - coordinate.getLongitude() / 15d;
            imsak += addToAll;
            fajr += addToAll;
            sunrise += addToAll;
            dhuhr += addToAll;
            asr += addToAll;
            sunset += addToAll;
            maghrib += addToAll;
            isha += addToAll;

            if (HIGH_LATS_METHOD != CalculationMethod.HighLatMethods.None) {
                // adjust times for locations in higher latitudes
                double nightTime = timeDiff(sunset, sunrise);

                imsak = adjustHLTime(imsak, sunrise, DEFAULT_TIME_IMSAK.getValue(), nightTime, true);
                fajr = adjustHLTime(fajr, sunrise, method.getFajr().getValue(), nightTime, true);
                isha = adjustHLTime(isha, sunset, method.getIsha().getValue(), nightTime);
                maghrib = adjustHLTime(maghrib, sunset, method.getMaghrib().getValue(), nightTime);
            }

            if (DEFAULT_TIME_IMSAK.isMinute()) {
                imsak = fajr - DEFAULT_TIME_IMSAK.getValue() / 60;
            }
            if (method.getMaghrib().isMinute()) {
                maghrib = sunset + method.getMaghrib().getValue() / 60d;
            }
            if (method.getIsha().isMinute()) {
                isha = maghrib + method.getIsha().getValue() / 60d;
            }
            dhuhr = dhuhr + DEFAULT_TIME_DHUHR.getValue() / 60d;
        }

        // add midnight time
        double midnight = method.getMidnight() == CalculationMethod.MidnightType.Jafari
                ? sunset + timeDiff(sunset, fajr) / 2
                : sunset + timeDiff(sunset, sunrise) / 2;

        return new PrayTimes(imsak, fajr, sunrise, dhuhr, asr, sunset, maghrib, isha, midnight);
    }

    public static PrayTimes calculate(CalculationMethod method, Date date, Coordinate coordinate) {
        return calculate(method, date, coordinate, null, null);
    }

    // compute mid-day time
    private static double midDay(double jdate, double time) {
        double eqt = sunPosition(jdate + time).getEquation();
        return fixHour(12 - eqt);
    }

    // compute the time at which sun reaches a specific angle below horizon
    private static double sunAngleTime(double jdate, MinuteOrAngleDouble angle, double time, boolean ccw, Coordinate coordinate) {
        // TODO: I must enable below line!
        // if (angle.isMinute()) throw new IllegalArgumentException("angle argument must be degree, not minute!");
        double decl = sunPosition(jdate + time).getDeclination();
        double noon = dtr(midDay(jdate, time));
        double t = Math.acos((-Math.sin(dtr(angle.getValue())) - Math.sin(decl)
                * Math.sin(dtr(coordinate.getLatitude())))
                / (Math.cos(decl) * Math.cos(dtr(coordinate.getLatitude())))) / 15d;
        return rtd(noon + (ccw ? -t : t));
    }

    private static double sunAngleTime(double jdate, MinuteOrAngleDouble angle, double time, Coordinate coordinate) {
        return sunAngleTime(jdate, angle, time, false, coordinate);
    }

    // compute asr time
    private static double asrTime(double jdate, double factor, double time, Coordinate coordinate) {
        double decl = sunPosition(jdate + time).getDeclination();
        double angle = -Math.atan(1 / (factor + Math.tan(dtr(coordinate.getLatitude()) - decl)));
        return sunAngleTime(jdate, deg(rtd(angle)), time, coordinate);
    }

    // compute declination angle of sun and equation of time
    // Ref: http://aa.usno.navy.mil/faq/docs/SunApprox.php
    private static DeclEqt sunPosition(double jd) {
        double D = jd - 2451545d;
        double g = (357.529 + 0.98560028 * D) % 360;
        double q = (280.459 + 0.98564736 * D) % 360;
        double L = (q + 1.915 * Math.sin(dtr(g)) + 0.020 * Math.sin(dtr(2d * g))) % 360;

        // weird!
        // double R = 1.00014 - 0.01671 * Math.cos(dtr(g)) - 0.00014 *
        // Math.cos(dtr(2d * g));

        double e = 23.439 - 0.00000036 * D;

        double RA = rtd(Math.atan2(Math.cos(dtr(e)) * Math.sin(dtr(L)), Math.cos(dtr(L)))) / 15d;
        double eqt = q / 15d - fixHour(RA);
        double decl = Math.asin(Math.sin(dtr(e)) * Math.sin(dtr(L)));

        return new DeclEqt(decl, eqt);
    }

    // convert Gregorian date to Julian day
    // Ref: Astronomical Algorithms by Jean Meeus
    private static double julian(int year, int month, int day) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        double A = Math.floor((double) year / 100);
        double B = 2 - A + Math.floor(A / 4);

        return Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + B - 1524.5;
    }

    // Section 2!! (Compute Prayer Time in JS code)
    //

    // get asr shadow factor
    private static double asrFactor() {
        return ASR_METHOD == CalculationMethod.AsrJuristics.Hanafi ? 2d : 1d;
    }

    // return sun angle for sunset/sunrise
    private static MinuteOrAngleDouble riseSetAngle(Coordinate coordinate) {
        // var earthRad = 6371009; // in meters
        // var angle = DMath.arccos(earthRad/(earthRad+ elv));
        double angle = 0.0347 * Math.sqrt(coordinate.getElevation()); // an approximation
        return deg(0.833 + angle);
    }

    // adjust a time for higher latitudes
    private static double adjustHLTime(double time, double bbase, double angle,
                                       double night, boolean ccw) {
        double portion = nightPortion(angle, night);
        double timeDiff = ccw ? timeDiff(time, bbase) : timeDiff(bbase, time);

        if (Double.isNaN(time) || timeDiff > portion)
            time = bbase + (ccw ? -portion : portion);
        return time;
    }

    private static double adjustHLTime(double time, double bbase, double angle,
                                       double night) {
        return adjustHLTime(time, bbase, angle, night, false);
    }

    // the night portion used for adjusting times in higher latitudes
    private static double nightPortion(double angle, double night) {
        double portion = 1d / 2d;
        if (HIGH_LATS_METHOD == CalculationMethod.HighLatMethods.AngleBased) {
            portion = 1d / 60d * angle;
        }
        if (HIGH_LATS_METHOD == CalculationMethod.HighLatMethods.OneSeventh) {
            portion = 1d / 7d;
        }
        return portion * night;
    }

    // get local time zone
    private static double getTimeZone(Date date) {
        return TimeZone.getDefault().getRawOffset() / (60 * 60 * 1000.0);
    }

    //
    // Time Zone Functions
    //
    //

    // get daylight saving for a given date
    private static boolean getDst(Date date) {
        return TimeZone.getDefault().inDaylightTime(date);
    }

    // compute the difference between two times
    private static double timeDiff(double time1, double time2) {
        return fixHour(time2 - time1);
    }

    //
    // Misc Functions
    //
    //

    private static class DeclEqt {
        private final double declination;
        private final double equation;

        DeclEqt(double declination, double equation) {
            super();
            this.declination = declination;
            this.equation = equation;
        }

        double getDeclination() {
            return declination;
        }

        double getEquation() {
            return equation;
        }
    }
}
