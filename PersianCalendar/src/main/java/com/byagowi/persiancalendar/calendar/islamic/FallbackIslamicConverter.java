package com.byagowi.persiancalendar.calendar.islamic;

import com.byagowi.persiancalendar.calendar.CivilDate;

public class FallbackIslamicConverter {

    private static int NMONTHS = (1405 * 12 + 1);

    private static long floor(double d) {
        return (long) Math.floor(d);
    }

    public static long toJdn(int year, int month, int day) {
        // NMONTH is the number of months between julian day number 1 and
        // the year 1405 A.H. which started immediatly after lunar
        // conjunction number 1048 which occured on September 1984 25d
        // 3h 10m UT.

        if (year < 0)
            year++;

        long k = month + year * 12 - NMONTHS; // nunber of months since 1/1/1405

        return floor(visibility(k + 1048) + day + 0.5);
    }

    private static double tmoonphase(long n, int nph) {

        final double RPD = (1.74532925199433E-02); // radians per degree
        // (pi/180)

        double xtra;

        double k = n + nph / 4d;
        double T = k / 1236.85;
        double t2 = T * T;
        double t3 = t2 * T;
        double jd = 2415020.75933 + 29.53058868 * k - 0.0001178 * t2
                - 0.000000155 * t3 + 0.00033
                * Math.sin(RPD * (166.56 + 132.87 * T - 0.009173 * t2));

        // Sun's mean anomaly
        double sa = RPD
                * (359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3);

        // Moon's mean anomaly
        double ma = RPD
                * (306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3);

        // Moon's argument of latitude
        double tf = RPD
                * 2d
                * (21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3);

        // should reduce to interval 0-1.0 before calculating further
        switch (nph) {
            case 0:
            case 2:
                xtra = (0.1734 - 0.000393 * T) * Math.sin(sa) + 0.0021
                        * Math.sin(sa * 2) - 0.4068 * Math.sin(ma) + 0.0161
                        * Math.sin(2 * ma) - 0.0004 * Math.sin(3 * ma) + 0.0104
                        * Math.sin(tf) - 0.0051 * Math.sin(sa + ma) - 0.0074
                        * Math.sin(sa - ma) + 0.0004 * Math.sin(tf + sa) - 0.0004
                        * Math.sin(tf - sa) - 0.0006 * Math.sin(tf + ma) + 0.001
                        * Math.sin(tf - ma) + 0.0005 * Math.sin(sa + 2 * ma);
                break;
            case 1:
            case 3:
                xtra = (0.1721 - 0.0004 * T) * Math.sin(sa) + 0.0021
                        * Math.sin(sa * 2) - 0.628 * Math.sin(ma) + 0.0089
                        * Math.sin(2 * ma) - 0.0004 * Math.sin(3 * ma) + 0.0079
                        * Math.sin(tf) - 0.0119 * Math.sin(sa + ma) - 0.0047
                        * Math.sin(sa - ma) + 0.0003 * Math.sin(tf + sa) - 0.0004
                        * Math.sin(tf - sa) - 0.0006 * Math.sin(tf + ma) + 0.0021
                        * Math.sin(tf - ma) + 0.0003 * Math.sin(sa + 2 * ma)
                        + 0.0004 * Math.sin(sa - 2 * ma) - 0.0003
                        * Math.sin(2 * sa + ma);
                if (nph == 1)
                    xtra = xtra + 0.0028 - 0.0004 * Math.cos(sa) + 0.0003
                            * Math.cos(ma);
                else
                    xtra = xtra - 0.0028 + 0.0004 * Math.cos(sa) - 0.0003
                            * Math.cos(ma);

                break;
            default:
                return 0;
        }
        // convert from Ephemeris Time (ET) to (approximate)Universal Time (UT)
        return jd + xtra - (0.41 + 1.2053 * T + 0.4992 * t2) / 1440;
    }

    private static double visibility(long n) {

        // parameters for Makkah: for a new moon to be visible after sunset on
        // a the same day in which it started, it has to have started before
        // (SUNSET-MINAGE)-TIMZ=3 A.M. local time.
        final float TIMZ = 3f, MINAGE = 13.5f, SUNSET = 19.5f, // approximate
                TIMDIF = (SUNSET - MINAGE);

        double jd = tmoonphase(n, 0);
        long d = floor(jd);

        double tf = (jd - d);

        if (tf <= 0.5) // new moon starts in the afternoon
            return (jd + 1f);
        else { // new moon starts before noon
            tf = (tf - 0.5) * 24 + TIMZ; // local time
            if (tf > TIMDIF)
                return (jd + 1d); // age at sunset < min for visiblity
            else
                return jd;
        }
    }

    public static int[] fromJdn(long jd) {
        CivilDate civil = new CivilDate(jd);
        int year = civil.getYear();
        int month = civil.getMonth();
        int day = civil.getDayOfMonth();

        long k = floor(0.6 + (year + (month % 2 == 0 ? month : month - 1) / 12d
                + day / 365f - 1900) * 12.3685);

        double mjd;
        do {
            mjd = visibility(k);
            k = k - 1;
        } while (mjd > (jd - 0.5));

        k = k + 1;
        long hm = k - 1048;

        year = 1405 + (int) (hm / 12);
        month = (int) (hm % 12) + 1;

        if (hm != 0 && month <= 0) {
            month = month + 12;
            year = year - 1;
        }

        if (year <= 0)
            year = year - 1;

        day = (int) floor(jd - mjd + 0.5);

        return new int[]{year, month, day};
    }
}
