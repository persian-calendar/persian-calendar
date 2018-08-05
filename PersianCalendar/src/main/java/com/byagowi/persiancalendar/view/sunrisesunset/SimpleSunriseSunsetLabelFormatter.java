package com.byagowi.persiancalendar.view.sunrisesunset;


import java.util.Locale;

import androidx.annotation.NonNull;

public class SimpleSunriseSunsetLabelFormatter implements SunriseSunsetLabelFormatter {
    @Override
    public String formatSunriseLabel(@NonNull Time sunrise) {
        return formatTime(sunrise);
    }

    @Override
    public String formatSunsetLabel(@NonNull Time sunset) {
        return formatTime(sunset);
    }

    public String formatTime(Time time) {
        return String.format(Locale.getDefault(), "%d:%d", time.hour, time.minute);
    }

}
