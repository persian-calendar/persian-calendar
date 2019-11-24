package io.github.persiancalendar;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

public class MainLogicTests {
    @Test
    public final void test_equinox_time() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"));
        int[][] elements = new int[][]{
                // https://calendar.ut.ac.ir/Fa/News/Data/Doc/Calendar%201399-Full.pdf
                {2020, 3, 20, 7, 20/*should be 19*/, 43/* should be 37*/},
                // https://calendar.ut.ac.ir/Fa/News/Data/Doc/Calendar%201398-Full.pdf
                {2019, 3, 21, 1, 28, 13/*should be 27*/},
                // https://calendar.ut.ac.ir/Fa/News/Data/Doc/Calendar%201397-Full.pdf
                {2018, 3, 20, 19, 45, 53/*should be 28*/},
                // https://calendar.ut.ac.ir/Fa/News/Data/Doc/Calendar%201396-Full.pdf
                {2017, 3, 20, 13, 59/*should be 58*/, 38/*should be 40*/},
                // https://calendar.ut.ac.ir/Fa/News/Data/Doc/Calendar%201395-Full.pdf
                {2016, 3, 20, 8, 0, 55/*should be 12*/},
                // http://vetmed.uk.ac.ir/documents/203998/204600/calendar-1394.pdf
                {2015, 3, 21, 2, 16/*should be 15*/, 0/*should be 11*/},
                // https://raw.githubusercontent.com/ilius/starcal/master/plugins/iran-jalali-data.txt
                {2014, 3, 20, 20, 27, 41/*should be 7*/},
                {2013, 3, 20, 14, 32/*should be 31*/, 41/*should be 56*/},
                {2012, 3, 20, 8, 44, 19/*should be 27*/},
                {2011, 3, 21, 2, 51/*should be 50*/, 38/*should be 25*/},
                {2010, 3, 20, 21, 2, 49/*should be 13*/},
                {2009, 3, 20, 15, 14/*should be 13*/, 50/*should be 39*/},
                {2008, 3, 20, 9, 18, 17/*should be 19*/}
        };

        for (int[] item : elements) {
            calendar.setTime(Equinox.northwardEquinox(item[0]));
            Assert.assertEquals(String.valueOf(item[0]), item[0], calendar.get(Calendar.YEAR));
            Assert.assertEquals(String.valueOf(item[0]), item[1], calendar.get(Calendar.MONTH) + 1);
            Assert.assertEquals(String.valueOf(item[0]), item[2], calendar.get(Calendar.DAY_OF_MONTH));
            Assert.assertEquals(String.valueOf(item[0]), item[3], calendar.get(Calendar.HOUR_OF_DAY));
            Assert.assertEquals(String.valueOf(item[0]), item[4], calendar.get(Calendar.MINUTE));
            Assert.assertEquals(String.valueOf(item[0]), item[5], calendar.get(Calendar.SECOND));
        }

        // And not having random crashes
        for (int i = -2000; i <= 10000; i++) {
            Equinox.northwardEquinox(i);
        }
    }
}
