package com.byagowi.persiancalendar.view.sunrisesunset;

import java.math.BigDecimal;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */

public class Location {
    private BigDecimal latitude;
    private BigDecimal longitude;

    public Location(String latitude, String longitude) {
        this.latitude = new BigDecimal(latitude);
        this.longitude = new BigDecimal(longitude);
    }

    public Location(double latitude, double longitude) {
        this.latitude = new BigDecimal(latitude);
        this.longitude = new BigDecimal(longitude);
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

}
