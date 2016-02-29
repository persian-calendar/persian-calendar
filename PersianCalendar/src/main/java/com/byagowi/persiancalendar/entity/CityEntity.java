package com.byagowi.persiancalendar.entity;

import com.github.praytimes.Coordinate;

/**
 * Created by ebraminio on 2/18/16.
 */
public class CityEntity {
    private String key;
    private String en;
    private String fa;
    private String countryCode;
    private String countryEn;
    private String countryFa;
    private Coordinate coordinate;

    public CityEntity(String key, String en, String fa, String countryCode, String countryEn,
                      String countryFa, Coordinate coordinate) {
        this.key = key;
        this.en = en;
        this.fa = fa;
        this.countryCode = countryCode;
        this.countryEn = countryEn;
        this.countryFa = countryFa;
        this.coordinate = coordinate;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryEn() {
        return countryEn;
    }

    public void setCountryEn(String countryEn) {
        this.countryEn = countryEn;
    }

    public String getCountryFa() {
        return countryFa;
    }

    public void setCountryFa(String countryFa) {
        this.countryFa = countryFa;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getFa() {
        return fa;
    }

    public void setFa(String fa) {
        this.fa = fa;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setLatitude(Coordinate coordinate) {
        this.coordinate = coordinate;
    }
}