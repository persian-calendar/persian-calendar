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
    private static final PrayTimes DEFAULT_TIMES = new PrayTimes(
            5d / 24, 5d / 24, 6d / 24,
            12d / 24, 13d / 24, 18d / 24,
            18d / 24, 18d / 24, 24d / 24);
    private static final MinuteOrAngleDouble DEFAULT_IMSAK = min(10);
    private static final MinuteOrAngleDouble DEFAULT_DHUHR = min(0);
    private static final CalculationMethod.AsrJuristics ASR_METHOD = CalculationMethod.AsrJuristics.Standard;
    private static final CalculationMethod.HighLatMethods HIGH_LATS_METHOD = CalculationMethod.HighLatMethods.NightMiddle;
    private final CalculationMethod mMethod;
    private double mTimeZone;
    private double mJDate;
    private Coordinate mCoordinate;

    public PrayTimesCalculator() {
        this(CalculationMethod.MWL); // default method
    }

    public PrayTimesCalculator(CalculationMethod method) {
        mMethod = method; // default method
    }

    //
    // Calculation Logic
    //
    //
    public PrayTimes calculate(Date date, Coordinate coordinate,
                               Double timeZone, Boolean dst) {
        mCoordinate = coordinate;
        this.mTimeZone = timeZone != null ? timeZone : getTimeZone(date);
        boolean _dst = dst != null ? dst : getDst(date);
        if (_dst) {
            this.mTimeZone++;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        mJDate = julian(year, month, day) - mCoordinate.getLongitude() / (15d * 24d);

        // compute prayer times at given julian date
        PrayTimes times = new PrayTimes(
                sunAngleTime(DEFAULT_IMSAK, DEFAULT_TIMES.getImsak(), true),
                sunAngleTime(mMethod.getFajr(), DEFAULT_TIMES.getFajr(), true),
                sunAngleTime(riseSetAngle(), DEFAULT_TIMES.getSunrise(), true),
                midDay(DEFAULT_TIMES.getDhuhr()),
                asrTime(asrFactor(), DEFAULT_TIMES.getAsr()),
                sunAngleTime(riseSetAngle(), DEFAULT_TIMES.getSunset()),
                sunAngleTime(mMethod.getMaghrib(), DEFAULT_TIMES.getMaghrib()),
                sunAngleTime(mMethod.getIsha(), DEFAULT_TIMES.getIsha()), 0);

        times = adjustTimes(times);

        // add midnight time
        times.setMidnight(mMethod.getMidnight() == CalculationMethod.MidnightType.Jafari
                ? times.getSunset() + timeDiff(times.getSunset(), times.getFajr()) / 2
                : times.getSunset() + timeDiff(times.getSunset(), times.getSunrise()) / 2);

        return times;
    }

    PrayTimes calculate(Date date, Coordinate coordinate, Double timeZone) {
        return calculate(date, coordinate, timeZone, null);
    }

    public PrayTimes calculate(Date date, Coordinate coordinate) {
        return calculate(date, coordinate, null);
    }

    // compute mid-day time
    private double midDay(double time) {
        double eqt = sunPosition(mJDate + time).getEquation();
        return fixHour(12 - eqt);
    }

    // compute the time at which sun reaches a specific angle below horizon
    private double sunAngleTime(MinuteOrAngleDouble angle, double time,
                                boolean ccw) {
        // TODO: I must enable below line!
        // if (angle.isMinute()) throw new IllegalArgumentException("angle argument must be degree, not minute!");
        double decl = sunPosition(mJDate + time).getDeclination();
        double noon = dtr(midDay(time));
        double t = Math.acos((-Math.sin(dtr(angle.getValue())) - Math.sin(decl)
                * Math.sin(dtr(mCoordinate.getLatitude())))
                / (Math.cos(decl) * Math.cos(dtr(mCoordinate.getLatitude())))) / 15d;
        return rtd(noon + (ccw ? -t : t));
    }

    private double sunAngleTime(MinuteOrAngleDouble angle, double time) {
        return sunAngleTime(angle, time, false);
    }

    // compute asr time
    private double asrTime(double factor, double time) {
        double decl = sunPosition(mJDate + time).getDeclination();
        double angle = -Math.atan(1 / (factor + Math.tan(dtr(mCoordinate.getLatitude()) - decl)));
        return sunAngleTime(deg(rtd(angle)), time);
    }

    // compute declination angle of sun and equation of time
    // Ref: http://aa.usno.navy.mil/faq/docs/SunApprox.php
    private DeclEqt sunPosition(double jd) {
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
    private double julian(int year, int month, int day) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        double A = Math.floor((double) year / 100);
        double B = 2 - A + Math.floor(A / 4);

        return Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + B - 1524.5;
    }

    // adjust times
    private PrayTimes adjustTimes(PrayTimes times) {
        times.addToAll(mTimeZone - mCoordinate.getLongitude() / 15d);

        if (HIGH_LATS_METHOD != CalculationMethod.HighLatMethods.None) {
            times = adjustHighLats(times);
        }

        if (DEFAULT_IMSAK.isMinute()) {
            times.setImsak(times.getFajr() - DEFAULT_IMSAK.getValue() / 60);
        }
        if (mMethod.getMaghrib().isMinute()) {
            times.setMaghrib(times.getSunset() + mMethod.getMaghrib().getValue() / 60d);
        }
        if (mMethod.getIsha().isMinute()) {
            times.setIsha(times.getMaghrib() + mMethod.getIsha().getValue() / 60d);
        }
        times.setDhuhr(times.getDhuhr() + DEFAULT_DHUHR.getValue() / 60d);

        return times;
    }

    // Section 2!! (Compute Prayer Time in JS code)
    //

    // get asr shadow factor
    private double asrFactor() {
        return ASR_METHOD == CalculationMethod.AsrJuristics.Hanafi ? 2d : 1d;
    }

    // return sun angle for sunset/sunrise
    private MinuteOrAngleDouble riseSetAngle() {
        // var earthRad = 6371009; // in meters
        // var angle = DMath.arccos(earthRad/(earthRad+ elv));
        double angle = 0.0347 * Math.sqrt(mCoordinate.getElevation()); // an approximation
        return deg(0.833 + angle);
    }

    // adjust times for locations in higher latitudes
    private PrayTimes adjustHighLats(PrayTimes times) {
        double nightTime = timeDiff(times.getSunset(),
                times.getSunrise());

        times.setImsak(adjustHLTime(times.getImsak(),
                times.getSunrise(), DEFAULT_IMSAK.getValue(), nightTime, true));

        times.setFajr(adjustHLTime(times.getFajr(), times.getSunrise(),
                mMethod.getFajr().getValue(), nightTime, true));

        times.setIsha(adjustHLTime(times.getIsha(), times.getSunset(),
                mMethod.getIsha().getValue(), nightTime));

        times.setMaghrib(adjustHLTime(times.getMaghrib(),
                times.getSunset(), mMethod.getMaghrib().getValue(), nightTime));

        return times;
    }

    // adjust a time for higher latitudes
    private double adjustHLTime(double time, double bbase, double angle,
                                double night, boolean ccw) {
        double portion = nightPortion(angle, night);
        double timeDiff = ccw ? timeDiff(time, bbase) : timeDiff(bbase, time);

        if (Double.isNaN(time) || timeDiff > portion)
            time = bbase + (ccw ? -portion : portion);
        return time;
    }

    private double adjustHLTime(double time, double bbase, double angle,
                                double night) {
        return adjustHLTime(time, bbase, angle, night, false);
    }

    // the night portion used for adjusting times in higher latitudes
    private double nightPortion(double angle, double night) {
        double portion = 1d / 2d;
        if (HIGH_LATS_METHOD == CalculationMethod.HighLatMethods.AngleBased) {
            portion = 1d / 60d * angle;
        }
        if (HIGH_LATS_METHOD == CalculationMethod.HighLatMethods.OneSeventh) {
            portion = 1 / 7;
        }
        return portion * night;
    }

    // get local time zone
    private double getTimeZone(Date date) {
        return TimeZone.getDefault().getRawOffset() / (60 * 60 * 1000.0);
    }

    //
    // Time Zone Functions
    //
    //

    // get daylight saving for a given date
    private boolean getDst(Date date) {
        return TimeZone.getDefault().inDaylightTime(date);
    }

    // compute the difference between two times
    private double timeDiff(double time1, double time2) {
        return fixHour(time2 - time1);
    }

//
// Misc Functions
//
//

    private class DeclEqt {
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
