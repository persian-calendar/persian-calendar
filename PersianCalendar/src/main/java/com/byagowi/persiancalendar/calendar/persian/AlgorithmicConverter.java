package com.byagowi.persiancalendar.calendar.persian;

/**
 * @author Amir
 * @author ebraminio
 */
public class AlgorithmicConverter {

    private static long floor(double d) {
        return (long) Math.floor(d);
    }

    public static long toJdn(int year, int month, int day) {
        final long PERSIAN_EPOCH = 1948321; // The JDN of 1 Farvardin 1

        long epbase;
        if (year >= 0)
            epbase = year - 474;
        else
            epbase = year - 473;

        long epyear = 474 + (epbase % 2820);

        long mdays;
        if (month <= 7)
            mdays = (month - 1) * 31;
        else
            mdays = (month - 1) * 30 + 6;

        return day + mdays + ((epyear * 682) - 110) / 2816 + (epyear - 1) * 365
                + epbase / 2820 * 1029983 + (PERSIAN_EPOCH - 1);
    }

    public static int[] fromJdn(long jdn) {
        long depoch = jdn - 2121446; // or toJdn(475, 1, 1);
        long cycle = depoch / 1029983;
        long cyear = depoch % 1029983;
        long ycycle;
        long aux1, aux2;

        if (cyear == 1029982)
            ycycle = 2820;
        else {
            aux1 = cyear / 366;
            aux2 = cyear % 366;
            ycycle = floor(((2134 * aux1) + (2816 * aux2) + 2815) / 1028522d)
                    + aux1 + 1;
        }

        int year, month, day;
        year = (int) (ycycle + (2820 * cycle) + 474);
        if (year <= 0)
            year = year - 1;

        long yday = (jdn - toJdn(year, 1, 1)) + 1;
        if (yday <= 186)
            month = (int) Math.ceil(yday / 31d);
        else
            month = (int) Math.ceil((yday - 6) / 30d);

        day = (int) (jdn - toJdn(year, month, 1)) + 1;
        return new int[]{year, month, day};
    }
}
