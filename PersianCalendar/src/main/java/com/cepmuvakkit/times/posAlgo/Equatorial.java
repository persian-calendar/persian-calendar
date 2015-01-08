/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cepmuvakkit.times.posAlgo;

public class Equatorial {

    public double α; //right ascension (α) -also RA-, or hour angle (H) -also HA-
    public double δ; //declination (δ)
    public double Δ; //distance to the earth(Δ) in km

    Equatorial() {
    }

    Equatorial(double sunRightAscension, double sunDeclination) {
        this.α = sunRightAscension;
        this.δ = sunDeclination;
    }

    Equatorial(double sunRightAscension, double sunDeclination, double radius) {
        this.α = sunRightAscension;
        this.δ = sunDeclination;
        this.Δ = radius;
    }

   public Horizontal Equ2Topocentric (double longitude, double latitude, double Height, double jd, double ΔT)
          {

           double ϕ=Math.toRadians(latitude);
           double ρsinϕPr = ρsinϕPrime(ϕ, Height);
           double ρCosϕPr = ρCosϕPrime(ϕ, Height);

          //Calculate the Sidereal time

          //double ΔT = AstroLib.calculateTimeDifference(jd);
          double theta = SolarPosition.calculateGreenwichSiderealTime(jd, ΔT);

          //Convert to radians
          double δrad = Math.toRadians(δ);
          
          double cosδ = Math.cos(δrad);
          //  4.26345151167726E-5
          //Calculate the Parallax
          double π= getHorizontalParallax(Δ);
          double sinπ = Math.sin(π);

          //Calculate the hour angle
          double H = Math.toRadians(AstroLib.limitDegrees(theta +longitude - α));
          double cosH =Math.cos(H);
          double sinH =Math.sin(H);

          //Calculate the adjustment in right ascension
          double  Δα = MATH.atan2(-ρCosϕPr*sinπ*sinH,cosδ - ρCosϕPr*sinπ*cosH);

          Horizontal horizontal;
          horizontal= new Horizontal ();
        //  CAA2DCoordinate Topocentric;
      //    double αPrime =Math.toRadians(α)+Δα;
          double δPrime= MATH.atan2((Math.sin(δrad) - ρsinϕPr*sinπ) * Math.cos(Δα), cosδ - ρCosϕPr*sinπ*cosH);
          double HPrime=H-Δα;
       
         horizontal.Az= Math.toDegrees(MATH.atan2(Math.sin(HPrime), Math.cos(HPrime)*Math.sin(ϕ)-Math.tan(δPrime)* Math.cos(ϕ))+Math.PI);
         horizontal.h=Math.toDegrees( MATH.asin (Math.sin(ϕ)*Math.sin(δPrime)+ Math.cos(ϕ) *Math.cos(δPrime)*Math.cos (HPrime)));
         return horizontal;

        }

    double ρsinϕPrime(double ϕ, double Height) {
        
        double U = MATH.atan(0.99664719 * Math.tan(ϕ));
        return 0.99664719 * Math.sin(U) + (Height / 6378149 * Math.sin(ϕ));
    }

    double ρCosϕPrime(double ϕ, double Height) {
        //Convert from degress to radians
        double U = MATH.atan(0.99664719 * Math.tan(ϕ));
        return Math.cos(U) + (Height / 6378149 *  Math.cos(ϕ));


    }

     double getHorizontalParallax(double RadiusVector) {
        return MATH.asin(6378.14 / RadiusVector);
    }
   
}
