package com.byagowi.persiancalendar;

import com.byagowi.persiancalendar.util.CalendarType;
import com.byagowi.persiancalendar.util.CalendarUtils;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

// Should be merged with MainLogicTests Kotlin tests
public class LeapYearTest {
    @Test
    public void test_leap_years() {
        // Doesn't match with https://calendar.ut.ac.ir/Fa/News/Data/Doc/KabiseShamsi1206-1498.pdf
        List<Integer> leapYears = Arrays.asList(
                1209/*1210*/, 1214, 1218, 1222, 1226, 1230, 1234, 1238, 1242/*1243*/, 1247, 1251, 1255, 1259, 1263,
                1267, 1271, 1276, 1280, 1284, 1288, 1292, 1296, 1300, 1304, 1309, 1313, 1317, 1321,
                1325, 1329, 1333, 1337, 1342, 1346, 1350, 1354, 1358, 1362, 1366, 1370, 1375, 1379,
                1383, 1387, 1391, 1395, 1399, /*1403*/1404, 1408, 1412, 1416, 1420, 1424, 1428, 1432, /*1436*/1437,
                1441, 1445, 1449, 1453, 1457, 1461, 1465, /*1469*/1470, 1474, 1478, 1482, 1486, 1490, 1494,
                1498);
        for (int year = 1206; year <= 1498; ++year) {
            assertEquals(CalendarUtils.getMonthLength(CalendarType.SHAMSI, year, 12),
                    leapYears.contains(year) ? 30 : 29);
        }
    }
}
