package com.byagowi.persiancalendar;

import org.junit.Test;

import calendar.DateConverter;
import calendar.IslamicDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDateCalendar {
    @Test
    public void islamic_converter_test() {
        int[][] tests = {
                {2453767, 1427, 1, 1},
                {2455658, 1432, 5, 2},
                {2458579, 1440, 7, 29},
                // Note the Jdn, this shouldn't be like this
                {2458581, 1440, 8, 1}
        };
        for (int[] test : tests) {
            IslamicDate reference = new IslamicDate(test[1], test[2], test[3]);
            assertEquals(test[0], DateConverter.islamicToJdn(reference));

            assertEquals(test[1], DateConverter.jdnToIslamic(test[0]).getYear());
            assertEquals(test[2], DateConverter.jdnToIslamic(test[0]).getMonth());
            assertEquals(test[3], DateConverter.jdnToIslamic(test[0]).getDayOfMonth());

            assertEquals(test[0], DateConverter.islamicToJdn(DateConverter.jdnToIslamic(test[0])));
            assertTrue(reference.equals(
                    DateConverter.jdnToIslamic(DateConverter.islamicToJdn(reference))));
        }
    }

    @Test
    public void practice_persian_converting_back_and_forth() {
        long startJdn = DateConverter.civilToJdn(1950, 1, 1);
        long endJdn = DateConverter.civilToJdn(2050, 1, 1);
        for (long jdn = startJdn; jdn <= endJdn; ++jdn) {
            long result = DateConverter.civilToJdn(DateConverter.jdnToCivil(jdn));
            assertEquals(jdn, result);
        }
    }

    @Test
    public void practice_islamic_converting_back_and_forth() {
        long startJdn = DateConverter.civilToJdn(1950, 1, 1);
        long endJdn = DateConverter.civilToJdn(2050, 1, 1);
        for (long jdn = startJdn; jdn <= endJdn; ++jdn) {
            long result = DateConverter.islamicToJdn(DateConverter.jdnToIslamic(jdn));
            assertEquals(jdn, result);
        }
    }

    @Test
    public void practice_civil_converting_back_and_forth() {
        long startJdn = DateConverter.civilToJdn(1950, 1, 1);
        long endJdn = DateConverter.civilToJdn(2050, 1, 1);
        for (long jdn = startJdn; jdn <= endJdn; ++jdn) {
            long result = DateConverter.persianToJdn(DateConverter.jdnToPersian(jdn));
            assertEquals(jdn, result);
        }
    }
}