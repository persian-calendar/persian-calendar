package com.byagowi.persiancalendar.entity;

/**
 * Created by ebraminio on 2/18/16.
 */
public class City {
    private String key;
    private String en;
    private String fa;
    private String countryCode;
    private String countryEn;
    private String countryFa;
    private double latitude;
    private double longitude;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}