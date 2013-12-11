/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cepmuvakkit.times.posAlgo;

/**
 *
 * @author mgeden
 */
public class MoonRiseSetTest {
     static SolarPosition spa;
    LunarPosition lunar;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        double longitude = 32.85, latitude = 39.95,timezone=2; int temperature=10, pressure=1010;//ANKARA position
        LunarPosition lunar = new LunarPosition();
        Ecliptic moonPosEc;Equatorial moonPosEq;
        double jd= AstroLib.calculateJulianDay(2010,2,20, 0, 0, 0, 0);
        double ΔT = AstroLib.calculateTimeDifference(jd);
        System.out.println("---LUNAR POSITIONS------------");
        moonPosEc = lunar.calculateMoonEclipticCoordinates(jd, ΔT);
        System.out.println("Lunar Apperant Longitude λ :" + moonPosEc.λ);
        System.out.println("Lunar Latitude β :" + moonPosEc.β);
        System.out.println("Lunar Distance  :" + moonPosEc.Δ);
        moonPosEq = lunar.calculateMoonEqutarialCoordinates(jd, ΔT);
        System.out.println("Lunar Right Ascension α :" + moonPosEq.α);
        System.out.println("Lunar Declination δ :" + moonPosEq.δ);
        Horizontal horizontalMoon=moonPosEq.Equ2Topocentric(longitude, latitude,0,jd,ΔT);
        System.out.println("Lunar Topocentric Pos Azimuth :" +horizontalMoon.Az );
        System.out.println("Lunar Topocentric Pos elevation:" +horizontalMoon.h);
        double elevationCorrected=horizontalMoon.h+AstroLib.getAtmosphericRefraction(horizontalMoon.h)*AstroLib.getWeatherCorrectionCoefficent(temperature, pressure);
        System.out.println("Lunar Topocentric Pos elevation with Atmospheric correction:" +elevationCorrected);
        System.out.println("---MOONRISESET------------");
        for (int i= 0; i <= 30;i++) {
        System.out.print("Date" + AstroLib.fromJulianToCalendarStr(jd+i)+"  ");
        lunar.calculateMoonRiseTransitSet(jd+i, latitude, longitude, timezone,10, 1010, 0);
        }
        }
    }



