package calendar.islamic;

import java.util.HashMap;
import java.util.Map;

/**
 * Credits of this work goes to Saeed Rasooli and his
 * Kudos to his creative work!
 * I've tried to optimize its runtime performance so it is different to the way used on starcal.
 */
public class IranianIslamicDateConverter {
    private static Map<Integer, long[]> yearsMonthsInJd = new HashMap<>();

    private static int supportedYearsStart;
    private static long[] yearsStartJd;
    private static long jdSupportEnd;
    private static long jdSupportStart = 2453766;

    static {
        // https://github.com/ilius/starcal/blob/master/scal3/cal_types/hijri-monthes.json
        int[] hijriMonths = {
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
                1440, 30, 29, 30, 30, 30, 29, 29, 30, 29, 30, 29, 29,
                1441, 30, 29, 30, 29, 30, 30, 29, 30, 30, 29, 30, 29,
                1442, 29, 30, 29, 30, 29, 30, 29, 30, 30, 29, 30, 29,
                1443, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 30,
                1444, 29, 30, 29, 30, 30, 29, 29, 30, 29, 30, 29, 30,
                1445, 29, 30, 30, 30, 29, 30, 29, 29, 30, 29, 29, 30,
                1446, 29, 30, 30, 30, 29, 30, 30, 29, 29, 30, 29, 29,
                1447, 30, 29, 30, 30, 30, 29, 30, 29, 30, 29, 30, 29,
                1448, 29, 30, 29, 30, 30, 29, 30, 30, 29, 30, 29, 30,
                1449, 29, 29, 30, 29, 30, 29, 30, 30, 29, 30, 30, 29,
                1450, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 29,
                1451, 30, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 29,
                1452, 30, 30, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30,
                1453, 29, 30, 30, 30, 29, 29, 30, 29, 30, 29, 30, 29,
                1454, 29, 30, 30, 30, 29, 30, 29, 30, 29, 30, 29, 30,
                1455, 29, 29, 30, 30, 29, 30, 29, 30, 30, 29, 30, 29,
                1456, 30, 29, 29, 30, 29, 30, 29, 30, 30, 30, 29, 30,
                1457, 29, 30, 29, 29, 30, 29, 29, 30, 30, 29, 30, 30,
                1458, 30, 29, 30, 29, 29, 30, 29, 29, 30, 30, 29, 30,
                1459, 30, 30, 29, 30, 29, 29, 30, 29, 29, 30, 30, 29,
                1460, 30, 30, 29, 30, 29, 30, 29, 30, 29, 29, 30, 30,
                1461, 29, 30, 29, 30, 30, 29, 30, 29, 30, 29, 30, 29,
                1462, 30, 29, 30, 29, 30, 29, 30, 29, 30, 30, 29, 30,
                1463, 29, 30, 29, 29, 30, 29, 30, 30, 29, 30, 30, 29,
                1464, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 30,
                1465, 29, 30, 29, 30, 29, 29, 30, 29, 29, 30, 30, 30,
                1466, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30,
                1467, 30, 29, 30, 30, 29, 30, 29, 29, 30, 29, 30, 29,
                1468, 30, 29, 30, 30, 29, 30, 29, 30, 29, 30, 29, 30,
                1469, 29, 29, 30, 30, 29, 30, 30, 29, 30, 30, 29, 29,
                1470, 30, 29, 29, 30, 30, 29, 30, 29, 30, 30, 30, 29,
                1471, 29, 30, 29, 29, 30, 29, 30, 30, 29, 30, 30, 29,
                1472, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 30, 29,
                1473, 30, 29, 30, 30, 29, 30, 29, 29, 30, 29, 30, 29,
                1474, 30, 30, 29, 30, 30, 29, 30, 29, 29, 30, 29, 30,
                1475, 29, 30, 29, 30, 30, 30, 29, 30, 29, 29, 30, 29,
                1476, 29, 30, 29, 30, 30, 30, 29, 30, 30, 29, 29, 30,
                1477, 29, 29, 30, 29, 30, 30, 29, 30, 30, 30, 29, 29,
                1478, 30, 29, 29, 30, 29, 30, 30, 29, 30, 30, 29, 30,
                1479, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 29, 30,
                1480, 29, 30, 30, 29, 29, 30, 29, 30, 29, 30, 29, 30,
                1481, 29, 30, 30, 29, 30, 30, 29, 30, 29, 29, 30, 29,
                1482, 30, 29, 30, 30, 29, 30, 30, 29, 30, 29, 29, 30,
                1483, 29, 29, 30, 30, 29, 30, 30, 30, 29, 30, 29, 29,
                1484, 30, 29, 29, 30, 30, 29, 30, 30, 29, 30, 30, 29,
                1485, 29, 30, 29, 29, 30, 30, 29, 30, 29, 30, 30, 30,
                1486, 29, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 30,
                1487, 29, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 30,
                1488, 29, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30,
                1489, 29, 30, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29,
                1490, 30, 29, 30, 30, 29, 30, 30, 29, 30, 29, 29, 30,
                1491, 29, 30, 29, 30, 29, 30, 30, 29, 30, 29, 30, 30
        };

        int years = (int) Math.ceil(((float) hijriMonths.length) / 13);
        yearsStartJd = new long[years];
        supportedYearsStart = hijriMonths[0];
        long jd = jdSupportStart;
        for (int y = 0; y < years; ++y) {
            int year = hijriMonths[y * 13];

            yearsStartJd[y] = jd;
            long[] months = new long[12];
            for (int m = 1; m < 13 && y * 13 + m < hijriMonths.length; ++m) {
                months[m - 1] = jd;
                jd += hijriMonths[y * 13 + m];
            }
            yearsMonthsInJd.put(year, months);
        }
        jdSupportEnd = jd;
    }

    public static long toJdn(int year, int month, int day) {
        if (yearsMonthsInJd == null || yearsMonthsInJd.get(year) == null)
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

    public static int[] fromJdn(long jd) {
        if (jd < jdSupportStart || jd >= jdSupportEnd || yearsStartJd == null)
            return null;

        int yearIndex = search(yearsStartJd, jd);
        int year = yearIndex + supportedYearsStart - 1;
        long[] yearMonths = yearsMonthsInJd.get(year);
        if (yearMonths == null) {
            return null;
        }
        int month = search(yearMonths, jd);
        if (yearMonths[month - 1] == 0) {
            return null;
        }
        int day = (int) (jd - yearMonths[month - 1]);
        return new int[]{year, month, day};
    }
}