package calendar;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Credits of this work goes to Saeed Rasooli and his
 * Kudos to his creative work!
 * I've tried to optimize its runtime perforamance so it is different to the way used on starcal.
 */
class IslamicDateConverter {
    private static HashMap<Integer, long[]> yearsMonthsInJd = new HashMap<>();

    private static int supportedYearsStart;
    private static long[] yearsStartJd;
    private static long jdSupportEnd;
    private static long jdSupportStart = 2453766;
    private static void init() {
        // https://github.com/ilius/starcal/blob/master/scal3/cal_types/hijri-monthes.json
        int[] hirjiMonths = {
                1427, 30, 29, 29, 30, 29, 30, 30, 30, 30, 29, 29, 30,
                1428, 29, 30, 29, 29, 29, 30, 30, 29, 30, 30, 30, 29,
                1429, 30, 29, 30, 29, 29, 29, 30, 30, 29, 30, 30, 29,
                1430, 30, 30, 29, 29, 30, 29, 30, 29, 29, 30, 30, 29,
                1431, 30, 30, 29, 30, 29, 30, 29, 30, 29, 29, 30, 29,
                1432, 30, 30, 29, 30, 30, 30, 29, 29, 30, 29, 30, 29,
                1433, 29, 30, 29, 30, 30, 30, 29, 30, 29, 30, 29, 30,
                1434, 29, 29, 30, 29, 30, 30, 29, 30, 30, 29, 30, 29,
                1435, 29, 30, 29, 30, 29, 30, 29, 30, 30, 30, 29, 30,
                1436, 29, 30, 29, 29, 30, 29, 30, 29, 30, 29, 30, 30,
                1437, 29, 30, 30, 29, 30, 29, 29, 30, 29, 29, 30, 30,
                1438, 29, 30, 30, 30, 29, 30, 29, 29, 30, 29, 29, 30,
                1439, 29, 30, 30, 30, 30, 29, 30, 29, 29, 30, 29, 29,
                1440, 30, 29, 30, 30, 30, 29, 29
        };

        int years = (int) Math.ceil(((float) hirjiMonths.length) / 13);
        yearsStartJd = new long[years];
        supportedYearsStart = hirjiMonths[0];
        long jd = jdSupportStart;
        for (int y = 0; y < years; ++y) {
            int year = hirjiMonths[y * 13];

            yearsStartJd[y] = jd;
            long[] months = new long[12];
            for (int m = 1; m < 13 && y * 13 + m < hirjiMonths.length; ++m) {
                months[m - 1] = jd;
                jd += hirjiMonths[y * 13 + m];
            }
            yearsMonthsInJd.put(year, months);
        }
        jdSupportEnd = jd;
    }
    {
        init();
    }

    static long hijriToJd(int year, int month, int day) {
        if (jdSupportEnd == 0)
            init();

        if (!yearsMonthsInJd.containsKey(year))
            return -1;

        long calculatedDay = yearsMonthsInJd.get(year)[month - 1];

        if (calculatedDay == 0)
            return -1;

        return calculatedDay + day;
    }

    private static int search(long[] array, long r) {
        int i = 0;
        while (i < array.length && array[i] < r) ++i;
        return i;
    }

    static int[] jdToHirji(long jd) {
        if (jdSupportEnd == 0)
            init();

        if (jd < jdSupportStart || jd >= jdSupportEnd)
            return null;

        int yearIndex = search(yearsStartJd, jd);
        int year = yearIndex + supportedYearsStart - 1;
        long[] yearMonths = yearsMonthsInJd.get(year);
        int month = search(yearMonths, jd);
        if (yearMonths[month - 1] == 0)
            return null;
        int day = (int)(jd - yearMonths[month - 1]);
        return new int[] { year, month, day };
    }
}