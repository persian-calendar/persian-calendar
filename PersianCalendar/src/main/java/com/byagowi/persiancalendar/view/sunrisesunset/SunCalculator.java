package com.byagowi.persiancalendar.view.sunrisesunset;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */

public class SunCalculator {

    private SolarCalculator calculator;

    SunCalculator(Location location, TimeZone timeZone) {
        this.calculator = new SolarCalculator(location, timeZone);
    }

    public String getOfficialSunriseForDate(Calendar date) {
        return calculator.computeSunriseTime(Zen.OFFICIAL, date);
    }

    public Calendar getOfficialSunriseCalendarForDate(Calendar date) {
        return calculator.computeSunriseCalendar(Zen.OFFICIAL, date);
    }

    public String getOfficialSunsetForDate(Calendar date) {
        return calculator.computeSunsetTime(Zen.OFFICIAL, date);
    }

    public Calendar getOfficialSunsetCalendarForDate(Calendar date) {
        return calculator.computeSunsetCalendar(Zen.OFFICIAL, date);
    }

}
