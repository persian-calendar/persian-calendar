package com.byagowi.persiancalendar.entity;

/**
 * Created by ebraminio on 2/18/16.
 */
public class City {
    public final String key;
    public final String en;
    public final String fa;
    public final String countryCode;
    public final String countryEn;
    public final String countryFa;
    public final double latitude;
    public final double longitude;

    public City(String key, String en, String fa, String countryCode, String countryEn,
                String countryFa, double latitude, double longitude) {
        this.key = key;
        this.en = en;
        this.fa = fa;
        this.countryCode = countryCode;
        this.countryEn = countryEn;
        this.countryFa = countryFa;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
