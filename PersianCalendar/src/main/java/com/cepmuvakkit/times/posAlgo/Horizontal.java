/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cepmuvakkit.times.posAlgo;

import android.graphics.Canvas;





public class Horizontal {

    public double h; //h         Altitude
    public double Az; //Az       Azimuth

    public Horizontal() {
    }

    public Horizontal(double Azimuth, double Altitude) {
        this.h = Altitude;
        this.Az = Azimuth;
    }

    public double getElevation() {
        return h;
    }

    public double getAzimuth() {
        return Az;
    }

    public void setAzimuth(double azimuth) {
        Az = azimuth;
    }

    public void setElevation(double elevation) {
        h = elevation;
    }

    public ScreenPosition toScreenPosition(Canvas canvas, int offset, boolean flipX) {
        int midX = canvas.getWidth() / 2;
        int midY = canvas.getHeight() / 2;
        int maxR = Math.min(midX, midY);
        ScreenPosition screenPosition = new ScreenPosition();
        double r = ((90 - h) / 90) * maxR;
        double azimuth = Math.toRadians(Az - offset);
        screenPosition.x = (int) (Math.sin(azimuth) * r) * (flipX ? -1 : 1) + midX;
        screenPosition.y = (int) (Math.cos(azimuth) * (-r)) + midY;
        return screenPosition;

    }
    public ScreenPosition toScreenPosition(int midX,int midY) {
        
    	int maxR = Math.min(midX,midY);
        ScreenPosition screenPosition = new ScreenPosition();
        double r = ((90 - h) / 90) * maxR;
        double azimuth = Math.toRadians(Az);
        screenPosition.x = (int) (Math.sin(azimuth) * r)+ midX;
        screenPosition.y = (int) (Math.cos(azimuth) * (-r)) + midY;
        return screenPosition;

    }
}

