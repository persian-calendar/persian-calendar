package com.byagowi.persiancalendar.view.sunrisesunset;

import android.content.Context;
import java.util.TimeZone;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */

public class Utils {

    //Qom
    private static final String PREF_LAT_KEY = "34.65";
    private static final String PREF_LON_KEY = "50.95";

    public static SunCalculator getSunriseSunsetCalculator(Context context) {
       return new SunCalculator(new Location(PREF_LAT_KEY, PREF_LON_KEY), TimeZone.getDefault());
    }

}
