package com.github.praytimes;

import java.util.*;

import static com.github.praytimes.StaticUtils.*;

public class PrayTimesCalculator {
    private final MinuteOrAngleDouble _imsak = min(10);
    private final MinuteOrAngleDouble _dhuhr = min(0);
    private final CalculationMethod.AsrJuristics _asr = CalculationMethod.AsrJuristics.Standard;
    private final CalculationMethod.HighLatMethods _highLats = CalculationMethod.HighLatMethods.NightMiddle;
    private boolean _dst;
    private double _timeZone;
    private double _jDate;
    private Coordinate _coordinate;
    private final CalculationMethod _method;

    // default times
    private static Map<PrayTime, Double> _defaultTimes;

    static {
        _defaultTimes = new HashMap<PrayTime, Double>();
        _defaultTimes.put(PrayTime.Imsak, 5d / 24);
        _defaultTimes.put(PrayTime.Fajr, 5d / 24);
        _defaultTimes.put(PrayTime.Sunrise, 6d / 24);
        _defaultTimes.put(PrayTime.Dhuhr, 12d / 24);
        _defaultTimes.put(PrayTime.Asr, 13d / 24);
        _defaultTimes.put(PrayTime.Sunset, 18d / 24);
        _defaultTimes.put(PrayTime.Maghrib, 18d / 24);
        _defaultTimes.put(PrayTime.Isha, 18d / 24);
        _defaultTimes = Collections.unmodifiableMap(_defaultTimes); // immutable
    }

    public PrayTimesCalculator() {
        this(CalculationMethod.MWL); // default method
    }

    public PrayTimesCalculator(CalculationMethod method) {
        _method = method; // default method
    }

    //
    // Calculation Logic
    //
    //
    Map<PrayTime, Clock> calculate(Date date, Coordinate coordinate,
                                   Double timeZone, Boolean dst) {
        _coordinate = coordinate;
        _timeZone = timeZone != null ? timeZone : getTimeZone(date);
        _dst = dst != null ? dst : getDst(date);
        if (_dst) {
            _timeZone++;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        _jDate = julian(year, month, day) - _coordinate.getLongitude() / (15d * 24d);

        // compute prayer times at given julian date
        Map<PrayTime, Double> times = new HashMap<PrayTime, Double>();
        times.put(PrayTime.Imsak, sunAngleTime(_imsak, _defaultTimes.get(PrayTime.Imsak), true));
        times.put(PrayTime.Fajr, sunAngleTime(_method.getFajr(), _defaultTimes.get(PrayTime.Fajr), true));
        times.put(PrayTime.Sunrise, sunAngleTime(riseSetAngle(), _defaultTimes.get(PrayTime.Sunrise), true));
        times.put(PrayTime.Dhuhr, midDay(_defaultTimes.get(PrayTime.Dhuhr)));
        times.put(PrayTime.Asr, asrTime(asrFactor(), _defaultTimes.get(PrayTime.Asr)));
        times.put(PrayTime.Sunset, sunAngleTime(riseSetAngle(), _defaultTimes.get(PrayTime.Sunset)));
        times.put(PrayTime.Maghrib, sunAngleTime(_method.getMaghrib(), _defaultTimes.get(PrayTime.Maghrib)));
        times.put(PrayTime.Isha, sunAngleTime(_method.getIsha(), _defaultTimes.get(PrayTime.Isha)));

        times = adjustTimes(times);

        // add midnight time
        times.put(PrayTime.Midnight, (_method.getMidnight() == CalculationMethod.MidnightType.Jafari) ?
                times.get(PrayTime.Sunset) + timeDiff(times.get(PrayTime.Sunset), times.get(PrayTime.Fajr)) / 2 :
                times.get(PrayTime.Sunset) + timeDiff(times.get(PrayTime.Sunset), times.get(PrayTime.Sunrise)) / 2);

        Map<PrayTime, Clock> result = new HashMap<PrayTime, Clock>();
        for (Map.Entry<PrayTime, Double> i : times.entrySet()) {
            result.put(i.getKey(), Clock.fromDouble(i.getValue()));
        }
        return result;
    }

    Map<PrayTime, Clock> calculate(Date date, Coordinate coordinate, Double timeZone) {
        return calculate(date, coordinate, timeZone, null);
    }

    public Map<PrayTime, Clock> calculate(Date date, Coordinate coordinate) {
        return calculate(date, coordinate, null);
    }

    // compute mid-day time
    private double midDay(double time) {
        double eqt = sunPosition(_jDate + time).getEquation();
        double noon = fixHour(12 - eqt);
        return noon;
    }

    // compute the time at which sun reaches a specific angle below horizon
    private double sunAngleTime(MinuteOrAngleDouble angle, double time, boolean ccw) {
        // TODO: I must enable below line!
        // if (angle.isMin()) throw new IllegalArgumentException("angle argument must be degree, not minute!");
        double decl = sunPosition(_jDate + time).getDeclination();
        double noon = dtr(midDay(time));
        double t = Math.acos((-Math.sin(dtr(angle.getValue())) - Math.sin(decl) * Math.sin(dtr(_coordinate.getLatitude()))) /
                (Math.cos(decl) * Math.cos(dtr(_coordinate.getLatitude())))) / 15d;
        return rtd(noon + (ccw ? -t : t));
    }

    private double sunAngleTime(MinuteOrAngleDouble angle, double time) {
        return sunAngleTime(angle, time, false);
    }

    // compute asr time
    private double asrTime(double factor, double time) {
        double decl = sunPosition(_jDate + time).getDeclination();
        double angle = -Math.atan(1 / (factor + Math.tan(dtr(_coordinate.getLatitude()) - decl)));
        return sunAngleTime(deg(rtd(angle)), time);
    }

    private class DeclEqt {
        private final double declination;
        private final double equation;

        public DeclEqt(double declination, double equation) {
            super();
            this.declination = declination;
            this.equation = equation;
        }

        public double getDeclination() {
            return declination;
        }

        public double getEquation() {
            return equation;
        }
    }

    // compute declination angle of sun and equation of time
    // Ref: http://aa.usno.navy.mil/faq/docs/SunApprox.php
    private DeclEqt sunPosition(double jd) {
        double D = jd - 2451545d;
        double g = (357.529 + 0.98560028 * D) % 360;
        double q = (280.459 + 0.98564736 * D) % 360;
        double L = (q + 1.915 * Math.sin(dtr(g)) + 0.020 * Math.sin(dtr(2d * g))) % 360;

        // weird!
        // double R = 1.00014 - 0.01671 * Math.cos(dtr(g)) - 0.00014 * Math.cos(dtr(2d * g));

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
        double B = 2 - A + Math.floor((double) A / 4);

        double JD = Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + B - 1524.5;
        return JD;
    }

    // Section 2!! (Compute Prayer Time in JS code)
    //

    // adjust times
    private Map<PrayTime, Double> adjustTimes(Map<PrayTime, Double> times) {
        Map<PrayTime, Double> result = new HashMap<PrayTime, Double>();
        for (Map.Entry<PrayTime, Double> i : times.entrySet()) {
            result.put(i.getKey(), i.getValue() + _timeZone - _coordinate.getLongitude() / 15d);
        }

        if (_highLats != CalculationMethod.HighLatMethods.None) {
            result = adjustHighLats(result);
        }

        if (_imsak.isMin()) {
            result.put(PrayTime.Imsak, result.get(PrayTime.Fajr) - _imsak.getValue() / 60);
        }
        if (_method.getMaghrib().isMin()) {
            result.put(PrayTime.Maghrib, result.get(PrayTime.Sunset) + _method.getMaghrib().getValue() / 60d);
        }
        if (_method.getIsha().isMin()) {
            result.put(PrayTime.Isha, result.get(PrayTime.Maghrib) + _method.getIsha().getValue() / 60d);
        }
        result.put(PrayTime.Dhuhr, result.get(PrayTime.Dhuhr) + _dhuhr.getValue() / 60d);

        return result;
    }

    // get asr shadow factor
    private double asrFactor() {
        return _asr == CalculationMethod.AsrJuristics.Hanafi ? 2d : 1d;
    }

    // return sun angle for sunset/sunrise
    private MinuteOrAngleDouble riseSetAngle() {
        //var earthRad = 6371009; // in meters
        //var angle = DMath.arccos(earthRad/(earthRad+ elv));
        double angle = 0.0347 * Math.sqrt(_coordinate.getElevation()); // an approximation
        return deg(0.833 + angle);
    }

    // adjust times for locations in higher latitudes
    private Map<PrayTime, Double> adjustHighLats(Map<PrayTime, Double> times) {
        double nightTime = timeDiff(times.get(PrayTime.Sunset), times.get(PrayTime.Sunrise));

        times.put(PrayTime.Imsak,
                adjustHLTime(times.get(PrayTime.Imsak), times.get(PrayTime.Sunrise), _imsak.getValue(), nightTime, true));

        times.put(PrayTime.Fajr,
                adjustHLTime(times.get(PrayTime.Fajr), times.get(PrayTime.Sunrise), _method.getFajr().getValue(), nightTime, true));

        times.put(PrayTime.Isha,
                adjustHLTime(times.get(PrayTime.Isha), times.get(PrayTime.Sunset), _method.getIsha().getValue(), nightTime));

        times.put(PrayTime.Maghrib,
                adjustHLTime(times.get(PrayTime.Maghrib), times.get(PrayTime.Sunset), _method.getMaghrib().getValue(), nightTime));

        return times;
    }

    // adjust a time for higher latitudes
    private double adjustHLTime(double time, double bbase, double angle, double night, boolean ccw) {
        double portion = nightPortion(angle, night);
        double timeDiff = ccw ? timeDiff(time, bbase) : timeDiff(bbase, time);

        if (Double.isNaN(time) || timeDiff > portion)
            time = bbase + (ccw ? -portion : portion);
        return time;
    }

    private double adjustHLTime(double time, double bbase, double angle, double night) {
        return adjustHLTime(time, bbase, angle, night, false);
    }

    // the night portion used for adjusting times in higher latitudes
    private double nightPortion(double angle, double night) {
        double portion = 1d / 2d;
        if (_highLats == CalculationMethod.HighLatMethods.AngleBased) {
            portion = 1d / 60d * angle;
        }
        if (_highLats == CalculationMethod.HighLatMethods.OneSeventh) {
            portion = 1 / 7;
        }
        return portion * night;
    }

    //
    // Time Zone Functions
    //
    //

    // get local time zone
    private double getTimeZone(Date date) {
        return TimeZone.getDefault().getRawOffset() / (60 * 60 * 1000.0);
    }

    // get daylight saving for a given date
    private boolean getDst(Date date) {
        return TimeZone.getDefault().inDaylightTime(date);
    }

    //
    // Misc Functions
    //
    //

    // compute the difference between two times
    private double timeDiff(double time1, double time2) {
        return fixHour(time2 - time1);
    }
}
