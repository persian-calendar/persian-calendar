/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cepmuvakkit.times.posAlgo;

/**
 *
 * @author mgeden
 */
public class EarthPosition {

    private double mLatitude;
    private double mLongitude;
    private double mTimezone;


    private int mAltitude, mTemperature, mPressure;

    public EarthPosition() {
        this(32.85, 39.95, 2, 10, 1010, 0);
    }

    public EarthPosition(double latitude, double longitude) {

        this(latitude,longitude,Math.round(longitude/15.0), 0, 10, 1010);

    }

    public EarthPosition(double latitude, double longitude, double timezone, int altitude, int temperature, int pressure) {
        mLatitude = latitude;
        mLongitude = longitude;
        mTimezone=timezone;
        mTemperature = temperature;
        mPressure = pressure;
        mAltitude = altitude;
    }

    public EarthPosition(float latitude, float longitude,float timezone, int altitude, int temperature, int pressure) {
        mLatitude = (double)latitude;
        mLongitude =(double) longitude;
        mTimezone=(double)timezone;
        mTemperature = temperature;
        mPressure = pressure;
        mAltitude = altitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }
    public double getTimezone() {
        return mTimezone;
    }

    public short getAltitude() {
        return (short) mAltitude;
    }

    public short getPressure() {
        return (short) mPressure;
    }

    public short getTemperature() {
        return (short) mTemperature;
    }

    public EarthHeading toEarthHeading(EarthPosition target) {
        // great circle formula from:
        // http://williams.best.vwh.net/avform.htm
        double radPerDeg = Math.PI / 180;
        double lat1 = Math.toRadians(mLatitude); //7155849931833333333e-19 0.71
        double lat2 = Math.toRadians(target.getLatitude());  //3737913479489224943e-19 0.373
        double lon1 = Math.toRadians(-mLongitude); //-5055637064497558276 e-19 -0.505
        double lon2 = Math.toRadians(-target.getLongitude());//-69493192920839161e-17  -0.69
        double a = Math.sin((lat1 - lat2) / 2);
        double b = Math.sin((lon1 - lon2) / 2);
        double d = 2 * MATH.asin(Math.sqrt(a * a + Math.cos(lat1) * Math.cos(lat2) * b * b));//3774840207564380360e-19
        //d=2*asin(sqrt((sin((lat1-lat2)/2))^2 + cos(lat1)*cos(lat2)*(sin((lon1-lon2)/2))^2))
        double tc1 = 0;
        // double c=a*a+Math.cos(lat1)*Math.cos(lat2))*b*b
        if (d > 0) {
            if ((Math.sin(lon2 - lon1)) < 0) //tc1=acos((sin(lat2)-sin(lat1)*cos(d))/(sin(d)*cos(lat1)))
            {
                tc1 = MATH.acos((Math.sin(lat2) - Math.sin(lat1) * Math.cos(d)) / (Math.sin(d) * Math.cos(lat1)));//2646123918118404228e-18
            } else {
                tc1 = 2 * Math.PI - MATH.acos((Math.sin(lat2) - Math.sin(lat1) * Math.cos(d)) / (Math.sin(d) * Math.cos(lat1)));
            }
        }
        //  tc1=2*pi-acos((sin(lat2)-sin(lat1)*cos(d))/(sin(d)*cos(lat1)))
        return new EarthHeading((tc1 / radPerDeg), (long) (d * 6371000));
    }
}
