package com.byagowi.persiancalendar;

import org.junit.Test;

import calendar.DateConverter;

import static org.junit.Assert.assertEquals;

public class TestDateCalendar {
    @Test
    public void iterate_over_all_years() {
        long startJdn = DateConverter.islamicToJdn(1426, 1, 1);
        long endJdn = DateConverter.islamicToJdn(1441, 1, 1);
        for (long jdn = startJdn; jdn <= endJdn; ++jdn) {
            long result = DateConverter.islamicToJdn(DateConverter.jdnToIslamic(jdn));
            assertEquals(jdn, result);
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