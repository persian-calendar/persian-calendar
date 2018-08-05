package com.byagowi.persiancalendar.view.sunrisesunset;

import androidx.annotation.NonNull;

public interface SunriseSunsetLabelFormatter {

    String formatSunriseLabel(@NonNull Time sunrise);

    String formatSunsetLabel(@NonNull Time sunset);
}
