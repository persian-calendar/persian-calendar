package com.byagowi.persiancalendar.entities;

import com.byagowi.persiancalendar.praytimes.Coordinate;

/**
 * Created by ebraminio on 2/18/16.
 */
public class CityItem {
    final private String key;
    final private String en;
    final private String fa;
    final private String ckb;
    final private String ar;
    final private String countryCode;
    final private String countryEn;
    final private String countryFa;
    final private String countryCkb;
    final private String countryAr;
    final private Coordinate coordinate;

    public CityItem(String key, String en, String fa, String ckb, String ar, String countryCode,
                    String countryEn, String countryFa, String countryCkb, String countryAr,
                    Coordinate coordinate) {
        this.key = key;
        this.en = en;
        this.fa = fa;
        this.ckb = ckb;
        this.ar = ar;
        this.countryCode = countryCode;
        this.countryEn = countryEn;
        this.countryFa = countryFa;
        this.countryCkb = countryCkb;
        this.countryAr = countryAr;
        this.coordinate = coordinate;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCountryEn() {
        return countryEn;
    }

    public String getCountryFa() {
        return countryFa;
    }

    public String getCountryCkb() {
        return countryCkb;
    }

    public String getCountryAr() {
        return countryAr;
    }

    public String getEn() {
        return en;
    }

    public String getFa() {
        return fa;
    }

    public String getCkb() {
        return ckb;
    }

    public String getAr() {
        return ar;
    }

    public String getKey() {
        return key;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }
}
