package com.byagowi.persiancalendar.view.sunrisesunset;

import com.github.praytimes.Coordinate;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */

public class SunCalculator {

    private SolarCalculator calculator;

    public SunCalculator(Coordinate coordinate, TimeZone timeZone) {
        this.calculator = new SolarCalculator(coordinate, timeZone);
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
