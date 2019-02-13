package com.byagowi.persiancalendar.calendar;

/**
 * @author Amir
 * @author ebraminio
 */

public class CivilDate extends AbstractDate {

    public CivilDate(int year, int month, int dayOfMonth) {
        super(year, month, dayOfMonth);
    }

    public CivilDate(long jdn) {
        super(jdn);
    }

    public CivilDate(AbstractDate date) {
        super(date);
    }

    // TODO Is it correct to return a CivilDate as a JulianDate?
    private static int[] julianFromJdn(long jdn) {
        long j = jdn + 1402;
        long k = ((j - 1) / 1461);
        long l = j - 1461 * k;
        long n = ((l - 1) / 365) - (l / 1461);
        long i = l - 365 * n + 30;
        j = ((80 * i) / 2447);
        int day = (int) (i - ((2447 * j) / 80));
        i = (j / 11);
        int month = (int) (j + 2 - 12 * i);
        int year = (int) (4 * k + n + i - 4716);

        return new int[]{year, month, day};
    }

    private static long julianToJdn(long lYear, long lMonth, long lDay) {
        return 367 * lYear - ((7 * (lYear + 5001 + ((lMonth - 9) / 7))) / 4)
                + ((275 * lMonth) / 9) + lDay + 1729777;
    }

    // Converters
    @Override
    public long toJdn() {
        long lYear = getYear(), lMonth = getMonth(), lDay = getDayOfMonth();

        if ((lYear > 1582)
                || ((lYear == 1582) && (lMonth > 10))
                || ((lYear == 1582) && (lMonth == 10) && (lDay > 14))) {

            return ((1461 * (lYear + 4800 + ((lMonth - 14) / 12))) / 4)
                    + ((367 * (lMonth - 2 - 12 * (((lMonth - 14) / 12)))) / 12)
                    - ((3 * (((lYear + 4900 + ((lMonth - 14) / 12)) / 100))) / 4)
                    + lDay - 32075;
        } else
            return julianToJdn(lYear, lMonth, lDay);
    }

    @Override
    protected int[] fromJdn(long jdn) {
        if (jdn > 2299160) {
            long l = jdn + 68569;
            long n = ((4 * l) / 146097);
            l = l - ((146097 * n + 3) / 4);
            long i = ((4000 * (l + 1)) / 1461001);
            l = l - ((1461 * i) / 4) + 31;
            long j = ((80 * l) / 2447);
            int day = (int) (l - ((2447 * j) / 80));
            l = (j / 11);
            int month = (int) (j + 2 - 12 * l);
            int year = (int) (100 * (n - 49) + i + l);
            return new int[]{year, month, day};
        } else
            return julianFromJdn(jdn);
    }
}
