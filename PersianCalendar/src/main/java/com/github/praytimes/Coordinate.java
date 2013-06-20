package com.github.praytimes;

public class Coordinate {
    private final double latitude;
    private final double longitude;
    private final double elevation;

    public Coordinate(double latitude, double longitude) {
        this(latitude, longitude, 0);
    }

    public Coordinate(double latitude, double longitude, double elevation) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getElevation() {
        return elevation;
    }
}