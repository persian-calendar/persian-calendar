/*
 * To change this template,choose Tools | Templates
 * and open the template in the editor.
 */
package com.cepmuvakkit.times.posAlgo;

/**
 *
 * @author mehmetrg
 */
public class LunarPosition {

    private final byte cD = 0, cM = 1, cMP = 2, cF = 3;
    private final byte cSin = 0, cCos = 1;
   
    // static final int ΣlΣrTerms[][]={      { 0,0,1,0,6288774,-20905335},{2,0,-1,0,1274027,-3699111},{2,0,0,0,658314,-2955968},{0,0,2,0,213618,-569925},{0,1,0,0,-185116,48888},{0,0,0,2,-114332,-3149},{2,0,-2,0,58793,246158},{2,-1,-1,0,57066,-152138},{2,0,1,0,53322,-170733},{2,-1,0,0,45758,-204586},{0,1,-1,0,-40923,-129620},{1,0,0,0,-34720,108743},{0,1,1,0,-30383,104755},{2,0,0,-2,15327,10321},{0,0,1,2,-12528,0},{0,0,1,-2,10980,79661},{4,0,-1,0,10675,-34782},{0,0,3,0,10034,-23210},{4,0,-2,0,8548,-21636},{2,1,-1,0,-7888,24208},{2,1,0,0,-6766,30824},{1,0,-1,0,-5163,-8379},{1,1,0,0,4987,-16675},{2,-1,1,0,4036,-12831},{2,0,2,0,3994,-10445},{4,0,0,0,3861,-11650},{2,0,-3,0,3665,14403},{0,1,-2,0,-2689,-7003},{2,0,-1,2,-2602,0},{2,-1,-2,0,2390,10056},{1,0,1,0,-2348,6322},{2,-2,0,0,2236,-9884},{0,1,2,0,-2120,5751},{0,2,0,0,-2069,0},{2,-2,-1,0,2048,-4950},{2,0,1,-2,-1773,4130},{2,0,0,2,-1595,0},{4,-1,-1,0,1215,-3958},{0,0,2,2,-1110,0},{3,0,-1,0,-892,3258},{2,1,1,0,-810,2616},{4,-1,-2,0,759,-1897},{0,2,-1,0,-713,-2117},{2,2,-1,0,-700,2354},{2,1,-2,0,691,0},{2,-1,0,-2,596,0},{4,0,1,0,549,-1423},{0,0,4,0,537,-1117},{4,-1,0,0,520,-1571},{1,0,-2,0,-487,-1739},{2,1,0,-2,-399,0},{0,0,2,-2,-381,-4421},{1,1,1,0,351,0},{3,0,-2,0,-340,0},{4,0,-3,0,330,0},{2,-1,2,0,327,0},{0,2,1,0,-323,1165},{1,1,-1,0,299,0},{2,0,3,0,294,0},{2,0,-1,-2,0,8752 }    };
    private static final byte argCoefforΣlΣr[][] = {
        {0, 0, 1, 0}, {2, 0, -1, 0}, {2, 0, 0, 0}, {0, 0, 2, 0}, {0, 1, 0, 0}, {0, 0, 0, 2}, {2, 0, -2, 0}, {2, -1, -1, 0}, {2, 0, 1, 0}, {2, -1, 0, 0}, {0, 1, -1, 0}, {1, 0, 0, 0}, {0, 1, 1, 0}, {2, 0, 0, -2}, {0, 0, 1, 2}, {0, 0, 1, -2}, {4, 0, -1, 0}, {0, 0, 3, 0}, {4, 0, -2, 0}, {2, 1, -1, 0}, {2, 1, 0, 0}, {1, 0, -1, 0}, {1, 1, 0, 0}, {2, -1, 1, 0}, {2, 0, 2, 0}, {4, 0, 0, 0}, {2, 0, -3, 0}, {0, 1, -2, 0}, {2, 0, -1, 2}, {2, -1, -2, 0}, {1, 0, 1, 0}, {2, -2, 0, 0}, {0, 1, 2, 0}, {0, 2, 0, 0}, {2, -2, -1, 0}, {2, 0, 1, -2}, {2, 0, 0, 2}, {4, -1, -1, 0}, {0, 0, 2, 2}, {3, 0, -1, 0}, {2, 1, 1, 0}, {4, -1, -2, 0}, {0, 2, -1, 0}, {2, 2, -1, 0}, {2, 1, -2, 0}, {2, -1, 0, -2}, {4, 0, 1, 0}, {0, 0, 4, 0}, {4, -1, 0, 0}, {1, 0, -2, 0}, {2, 1, 0, -2}, {0, 0, 2, -2}, {1, 1, 1, 0}, {3, 0, -2, 0}, {4, 0, -3, 0}, {2, -1, 2, 0}, {0, 2, 1, 0}, {1, 1, -1, 0}, {2, 0, 3, 0}, {2, 0, -1, -2}
    };
    private static final int coefSinCosΣlΣr[][] = {
        {6288774, -20905355}, {1274027, -3699111}, {658314, -2955968}, {213618, -569925}, {-185116, 48888}, {-114332, -3149}, {58793, 246158}, {57066, -152138}, {53322, -170733}, {45758, -204586}, {-40923, -129620}, {-34720, 108743}, {-30383, 104755}, {15327, 10321}, {-12528, 0}, {10980, 79661}, {10675, -34782}, {10034, -23210}, {8548, -21636}, {-7888, 24208}, {-6766, 30824}, {-5163, -8379}, {4987, -16675}, {4036, -12831}, {3994, -10445}, {3861, -11650}, {3665, 14403}, {-2689, -7003}, {-2602, 0}, {2390, 10056}, {-2348, 6322}, {2236, -9884}, {-2120, 5751}, {-2069, 0}, {2048, -4950}, {-1773, 4130}, {-1595, 0}, {1215, -3958}, {-1110, 0}, {-892, 3258}, {-810, 2616}, {759, -1897}, {-713, -2117}, {-700, 2354}, {691, 0}, {596, 0}, {549, -1423}, {537, -1117}, {520, -1571}, {-487, -1739}, {-399, 0}, {-381, -4421}, {351, 0}, {-340, 0}, {330, 0}, {327, 0}, {-323, 1165}, {299, 0}, {294, 0}, {0, 8752}
    }; 
    //static final int ΣbTerms[][]={{ 0,0,0,1,5128122},{0,0,1,1,280602},{0,0,1,-1,277693},{2,0,0,-1,173237},{2,0,-1,1,55413},{2,0,-1,-1,46271},{2,0,0,1,32573},{0,0,2,1,17198},{2,0,1,-1,9266},{0,0,2,-1,8822},{2,-1,0,-1,8216},{2,0,-2,-1,4324},{2,0,1,1,4200},{2,1,0,-1,-3359},{2,-1,-1,1,2463},{2,-1,0,1,2211},{2,-1,-1,-1,2065},{0,1,-1,-1,-1870},{4,0,-1,-1,1828},{0,1,0,1,-1794},{0,0,0,3,-1749},{0,1,-1,1,-1565},{1,0,0,1,-1491},{0,1,1,1,-1475},{0,1,1,-1,-1410},{0,1,0,-1,-1344},{1,0,0,-1,-1335},{0,0,3,1,1107},{4,0,0,-1,1021},{4,0,-1,1,833},{0,0,1,-3,777},{4,0,-2,1,671},{2,0,0,-3,607},{2,0,2,-1,596},{2,-1,1,-1,491},{2,0,-2,1,-451},{0,0,3,-1,439},{2,0,2,1,422},{2,0,-3,-1,421},{2,1,-1,1,-366},{2,1,0,1,-351},{4,0,0,1,331},{2,-1,1,1,315},{2,-2,0,-1,302},{0,0,1,3,-283},{2,1,1,-1,-229},{1,1,0,-1,223},{1,1,0,1,223},{0,1,-2,-1,-220},{2,1,-1,-1,-220},{1,0,1,1,-185},{2,-1,-2,-1,181},{0,1,2,1,-177},{4,0,-2,-1,176},{4,-1,-1,-1,166},{1,0,1,-1,-164},{4,0,1,-1,132},{1,0,-1,-1,-119},{4,-1,0,-1,115},{2,-2,0,1,107 }    };
    private static final byte argCoefforΣb[][] = {
        {0, 0, 0, 1}, {0, 0, 1, 1}, {0, 0, 1, -1}, {2, 0, 0, -1}, {2, 0, -1, 1}, {2, 0, -1, -1}, {2, 0, 0, 1}, {0, 0, 2, 1}, {2, 0, 1, -1}, {0, 0, 2, -1}, {2, -1, 0, -1}, {2, 0, -2, -1}, {2, 0, 1, 1}, {2, 1, 0, -1}, {2, -1, -1, 1}, {2, -1, 0, 1}, {2, -1, -1, -1}, {0, 1, -1, -1}, {4, 0, -1, -1}, {0, 1, 0, 1}, {0, 0, 0, 3}, {0, 1, -1, 1}, {1, 0, 0, 1}, {0, 1, 1, 1,}, {0, 1, 1, -1}, {0, 1, 0, -1}, {1, 0, 0, -1}, {0, 0, 3, 1}, {4, 0, 0, -1}, {4, 0, -1, 1,}, {0, 0, 1, -3}, {4, 0, -2, 1}, {2, 0, 0, -3}, {2, 0, 2, -1}, {2, -1, 1, -1}, {2, 0, -2, 1}, {0, 0, 3, -1}, {2, 0, 2, 1}, {2, 0, -3, -1}, {2, 1, -1, 1}, {2, 1, 0, 1}, {4, 0, 0, 1}, {2, -1, 1, 1}, {2, -2, 0, -1}, {0, 0, 1, 3}, {2, 1, 1, -1}, {1, 1, 0, -1}, {1, 1, 0, 1}, {0, 1, -2, -1}, {2, 1, -1, -1}, {1, 0, 1, 1}, {2, -1, -2, -1}, {0, 1, 2, 1}, {4, 0, -2, -1}, {4, -1, -1, -1}, {1, 0, 1, -1}, {4, 0, 1, -1}, {1, 0, -1, -1}, {4, -1, 0, -1}, {2, -2, 0, 1}
    };
    private static final int coefSinΣb[] = {5128122, 280602, 277693, 173237, 55413, 46271, 32573, 17198, 9266, 8822, 8216, 4324, 4200, -3359, 2463, 2211, 2065, -1870, 1828, -1794, -1749, -1565, -1491, -1475, -1410, -1344, -1335, 1107, 1021, 833, 777, 671, 607, 596, 491, -451, 439, 422, 421, -366, -351, 331, 315, 302, -283, -229, 223, 223, -220, -220, -185, 181, -177, 176, 166, -164, 132, -119, 115, 107,};

    /**
     * Calculate the mean elongation of the moon from the sun,X0 (in degrees),
     * <ul> D = 297.8501921 + 445267.1114034 * T - 0.0018819 * T2 + T3 / 545868.0 - T4 / 113065000.0;</ul>
     * @param  jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return mean elongation of the moon from the sun,X0 (in degrees).
     */
    private double meanElongationMoonSun(double jce) {
        return AstroLib.limitDegrees(AstroLib.fourthOrderPolynomial(1 / 113065000.0, 1.0 / 545868.0, -0.0018819, 445267.1114034, 297.8501921, jce));
    }

    /**
     * Calculate the mean longitude of the moon (in degrees),
     * <ul>   L1 = 218.3164477 + 481267.88123421 * T - 0.0015786 * T2  + T3 / 538841.0 - T4 / 65194000.0
     * Undoing the built-in 0.7" light-time adjustment
     * +0.0001944;</ul>
     * @param  jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return mean Moon Longitude (in degrees).
     */
    private double meanMoonLongitude(double jce) {
        return AstroLib.limitDegrees(AstroLib.fourthOrderPolynomial(1 / 65194000.0, 1.0 / 538841.0, -0.0015786, 481267.88123421, 218.3164477 + 0.0001944, jce));
        //return AstroLib.limitDegrees(AstroLib.fourthOrderPolynomial(1 / 65194000.0, 1.0 / 538841.0, -0.0015786, 481267.88123421, 218.3164477, jce));

    }

    /**
     * Calculate the mean anomaly of the sun (Earth),X1 (in degrees),
     * <ul>M = 357.5291092 + 35999.0502909 * T - 0.0001536 * T2 + T3 / 24490000.0;</ul>
     * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return  mean anomaly of the sun (Earth),X1 (in degrees).
     */
    private double meanAnomalySun(double jce) {
        return AstroLib.limitDegrees(AstroLib.thirdOrderPolynomial(1.0 / 24490000.0, -0.0001536, 35999.0502909, 357.5291092, jce));
    }

    /**
     * Calculate the mean anomaly of the moon,X2 (in degrees),
     * <ul>M1 = 134.9633964 + 477198.8675055 * T + 0.0087414 * T2 + T3 / 69699.0 - T4 / 14712000.0;  </ul>
     * @param  jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return mean anomaly of the moon,X2 (in degrees).
     */
    private double meanAnomalyMoon(double jce) {
        return AstroLib.limitDegrees(AstroLib.fourthOrderPolynomial(1.0 / 14712000.0, 1 / 69699.0, 0.0087414, 477198.8675055, 134.9633964, jce));
    }

    /**
     * Calculate the moon’s argument of latitude,X3 (in degrees),
     * <ul> F = 93.2720950 + 483202.0175233 * T - 0.0036539 * T2- T3 / 3526000.0 + T4 / 863310000.0; </ul>
     * @param  jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return the moon’s argument of latitude,X3 (in degrees).
     */
    private double argumentLatitudeMoon(double jce) {
        return AstroLib.limitDegrees(AstroLib.fourthOrderPolynomial(1 / 863310000.0, 1.0 / 3526000.0, -0.0036539, 483202.0175233, 93.2720950, jce));
    }

    /**
     * Calculate the eccentrity of Earth orbit arround the sun ,
     * <ul>E = 1.0 - 0.002516 * T - 0.0000074 * T2; </ul>
     * @param  jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return the eccentrity of Earth orbit arround the sun.
     */
    private double eccentrityOfEarthOrbit(double jce) {
        return 1.0 - 0.002516 * jce - 0.0000074 * jce * jce;
    }

    /**
     * Calculate effect of Venus to the Moon ,
     * <ul>A1 = 119.75 + 131.849 * T </ul>
     * @param  jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return A1.
     */
    private double effectVenus(double jce) {
        return AstroLib.limitDegrees(119.75 + 131.849 * jce);
    }

    /**
     * Calculate effect of Jupiter to the Moon ,
     * <ul>A2 = 53.09 + 479264.290 * T; </ul>
     * @param  jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return A2.
     */
    private double effectJupiter(double jce) {
        return AstroLib.limitDegrees(53.09 + 479264.290 * jce);
    }

    /**
     * Calculate effect of Jupiter to the Moon ,
     * <ul> A3 = 313.45 + 481266.484 * T; </ul>
     * @param  jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return A3.
     */
    private double effectFlatting(double jce) {
        return AstroLib.limitDegrees(313.45 + 481266.484 * jce);
    }

    public Ecliptic calculateMoonEclipticCoordinates(double jd, double ΔT) {
        double λ, β, Δ; //λ the ecliptic longitude,latitude and distance from earth in geocentric coordinates
        double Σl, Σb, Σr, L1, A1, A2, A3, M, F, M1, E, D;
        double jde, jce;
        double[] sum;

        //jc=AstroLib.getJulianCentury(jd);
        // double ΔT =AstroLib.calculateTimeDifference(jd);
        jde = AstroLib.getJulianEphemerisDay(jd, ΔT);
        jce = AstroLib.getJulianEphemerisCentury(jde);

        L1 = meanMoonLongitude(jce);
        D = meanElongationMoonSun(jce);
        M = meanAnomalySun(jce);
        M1 = meanAnomalyMoon(jce);
        F = argumentLatitudeMoon(jce);
        A1 = effectVenus(jce);
        A2 = effectJupiter(jce);
        A3 = effectFlatting(jce);
        E = eccentrityOfEarthOrbit(jce);
        sum = summationΣlΣr(L1, D, M, M1, F, E);
        Σl = sum[0];
        Σr = sum[1];
        Σb = summationΣb(L1, D, M, M1, F, E);
        Σl += 3958.0 * Math.sin(Math.toRadians(A1)) + 1962.0 * Math.sin(Math.toRadians(L1 - F)) + 318.0 * Math.sin(Math.toRadians(A2));
        Σb += -2235.0 * Math.sin(Math.toRadians(L1)) + 382.0 * Math.sin(Math.toRadians(A3)) + 175.0 * Math.sin(Math.toRadians(A1 - F)) +
                175.0 * Math.sin(Math.toRadians(A1 + F)) + 127.0 * Math.sin(Math.toRadians(L1 - M1)) - 115.0 * Math.sin(Math.toRadians(L1 + M1));
        double[] x = SolarPosition.calculateXArray(jd, ΔT);
        double Δψ = SolarPosition.nutationLongitude(jce, x);//
        λ = L1 + Σl / 1000000.0 + Δψ;//133.16265468515076//133.162849085666605652
        β = Σb / 1000000.0;
        Δ = 385000.56 + Σr / 1000.0;
        return new Ecliptic(λ, β, Δ);
    }
     //Ecliptic coordinates without  earth nutation  factor
     public Ecliptic calculateMoonEclipticCoordinatesAstronomic(double jd, double ΔT) {
        double λ, β;
        double Σl, Σb, L1, A1, A2, A3, M, F, M1, E, D;
        double jde, jce;
        double[] sum;
        jde = AstroLib.getJulianEphemerisDay(jd, ΔT);
        jce = AstroLib.getJulianEphemerisCentury(jde);
        L1 = meanMoonLongitude(jce);
        D = meanElongationMoonSun(jce);
        M = meanAnomalySun(jce);
        M1 = meanAnomalyMoon(jce);
        F = argumentLatitudeMoon(jce);
        A1 = effectVenus(jce);
        A2 = effectJupiter(jce);
        A3 = effectFlatting(jce);
        E = eccentrityOfEarthOrbit(jce);
        sum = summationΣlΣr(L1, D, M, M1, F, E);
        Σl = sum[0];
        Σb = summationΣb(L1, D, M, M1, F, E);
        Σl += 3958.0 * Math.sin(Math.toRadians(A1)) + 1962.0 * Math.sin(Math.toRadians(L1 - F)) + 318.0 * Math.sin(Math.toRadians(A2));
        Σb += -2235.0 * Math.sin(Math.toRadians(L1)) + 382.0 * Math.sin(Math.toRadians(A3)) + 175.0 * Math.sin(Math.toRadians(A1 - F)) +
                175.0 * Math.sin(Math.toRadians(A1 + F)) + 127.0 * Math.sin(Math.toRadians(L1 - M1)) - 115.0 * Math.sin(Math.toRadians(L1 + M1));
        λ = L1 + Σl / 1000000.0 ;
        β = Σb / 1000000.0;
        return new Ecliptic(λ, β);
    }

   public  Equatorial calculateMoonEqutarialCoordinates(double jd, double ΔT) {
        Ecliptic moonPos;
        double α, δ, ε0, Δε, ε;
        double jce, jme, jde;
        moonPos = calculateMoonEclipticCoordinates(jd, ΔT);
        double[] x = SolarPosition.calculateXArray(jd, ΔT);
        jde = AstroLib.getJulianEphemerisDay(jd, ΔT);
        jce = AstroLib.getJulianEphemerisCentury(jde);
        jme = AstroLib.getJulianEphemerisMillennium(jce);
        jme = AstroLib.getJulianEphemerisMillennium(jce);
        ε0 = SolarPosition.eclipticMeanObliquity(jme);//
        Δε = SolarPosition.nutationObliquity(jce, x);//
        // Δψ=SolarPosition.nutationLongitude(jce,x);//
        ε = SolarPosition.eclipticTrueObliquity(Δε, ε0);//
        α = SolarPosition.geocentricRightAscension(moonPos.λ, ε, moonPos.β);
        δ = SolarPosition.geocentricDeclination(moonPos.λ, ε, moonPos.β);
        return new Equatorial(α, δ, moonPos.Δ);

    }

     Equatorial calculateMoonEqutarialCoordinates(Ecliptic moonPos, double jd, double ΔT) {
        double α, δ, ε0, Δε, ε;
        double jce, jme, jde;
        double[] x = SolarPosition.calculateXArray(jd, ΔT);
        jde = AstroLib.getJulianEphemerisDay(jd, ΔT);
        jce = AstroLib.getJulianEphemerisCentury(jde);
        jme = AstroLib.getJulianEphemerisMillennium(jce);
        jme = AstroLib.getJulianEphemerisMillennium(jce);
        ε0 = SolarPosition.eclipticMeanObliquity(jme);//
        Δε = SolarPosition.nutationObliquity(jce, x);//
        // Δψ=SolarPosition.nutationLongitude(jce,x);//
        ε = SolarPosition.eclipticTrueObliquity(Δε, ε0);//
        α = SolarPosition.geocentricRightAscension(moonPos.λ, ε, moonPos.β);
        δ = SolarPosition.geocentricDeclination(moonPos.λ, ε, moonPos.β);
        return new Equatorial(α, δ, moonPos.Δ);

    }

    double[] summationΣlΣr(double L1, double D, double M, double M1, double F, double E) {
        double arg, L, R, E2 = E * E;
        L = 0;
        R = 0;
        for (int i = 0; i < argCoefforΣlΣr.length; ++i) {
            arg = Math.toRadians(argCoefforΣlΣr[i][cD] * D + argCoefforΣlΣr[i][cM] * M + argCoefforΣlΣr[i][cMP] * M1 + argCoefforΣlΣr[i][cF] * F);
            if (argCoefforΣlΣr[i][cM] == -2.0 || argCoefforΣlΣr[i][cM] == 2.0) {
                L += coefSinCosΣlΣr[i][cSin] * E2 * Math.sin(arg);
                R += coefSinCosΣlΣr[i][cCos] * E2 * Math.cos(arg);
            } else if (argCoefforΣlΣr[i][cM] == -1.0 || argCoefforΣlΣr[i][cM] == 1.0) {
                L += coefSinCosΣlΣr[i][cSin] * E * Math.sin(arg);
                R += coefSinCosΣlΣr[i][cCos] * E * Math.cos(arg);
            } else {
                L += coefSinCosΣlΣr[i][cSin] * Math.sin(arg);
                R += coefSinCosΣlΣr[i][cCos] * Math.cos(arg);
            }
        }
        double[] ΣlΣr = {L, R};
        return ΣlΣr;
    }

    double summationΣb(double L1, double D, double M, double M1, double F, double E) {
        double arg, Σb, E2 = E * E;
        Σb = 0;
        for (int i = 0; i < argCoefforΣb.length; ++i) {
            arg = Math.toRadians(argCoefforΣb[i][cD] * D + argCoefforΣb[i][cM] * M + argCoefforΣb[i][cMP] * M1 + argCoefforΣb[i][cF] * F);
            if (argCoefforΣb[i][cM] == -2.0 || argCoefforΣb[i][cM] == 2.0) {
                Σb += coefSinΣb[i] * E2 * Math.sin(arg);
            } else if (argCoefforΣb[i][cM] == -1.0 || argCoefforΣb[i][cM] == 1.0) {
                Σb += coefSinΣb[i] * E * Math.sin(arg);
            } else {
                Σb += coefSinΣb[i] * Math.sin(arg);
            }
        }
        return Σb;
    }

  
    void calculateMoonRiseTransitSetStr(double jd, double latitude, double longitude, double timezone, int temperature, int pressure, int altitude)
    {
       double[] m_trs, h_rts, νRts, αPrime, δPrime,HPrime;
        m_trs = new double[3];
        h_rts = new double[3];
        νRts = new double[3];
        αPrime = new double[3];
        δPrime = new double[3];
        HPrime = new double[3];
        
        double ν, H0, n;
        SolarPosition solar=new SolarPosition ();
        Equatorial dayBefore, dayOfInterest, dayAfter;
        jd = Math.floor(jd + 0.5) - 0.5;

        double ΔT =AstroLib.calculateTimeDifference(jd);
        dayBefore = calculateMoonEqutarialCoordinates(jd - 1, ΔT);
        dayOfInterest = calculateMoonEqutarialCoordinates(jd, ΔT);
        dayAfter = calculateMoonEqutarialCoordinates(jd + 1, ΔT);
        // private final int MOONRADIUS=15;
        double h0Prime =getLunarRiseSetAltitude(dayOfInterest.Δ,temperature,pressure,altitude);


        ν = SolarPosition.calculateGreenwichSiderealTime(jd, ΔT);
        m_trs[0] = solar.approxSunTransitTime(dayOfInterest.α, longitude, ν);
        H0 = solar.getHourAngleAtRiseSet(latitude, dayOfInterest.δ, h0Prime);
      
        double[] δ = {dayBefore.δ, dayOfInterest.δ, dayAfter.δ};
        double  moontransit, moonrise, moonset;

        if ((dayOfInterest.α - dayBefore.α) > 180.0) {
            dayBefore.α+= 360;
        } else if ((dayOfInterest.α - dayBefore.α) < -180.0) {
            dayOfInterest.α += 360;
        }
        if ((dayAfter.α - dayOfInterest.α) > 180.0) {
            dayOfInterest.α+= 360;
        } else if ((dayAfter.α - dayOfInterest.α) < -180.0) {
            dayAfter.α += 360;
        }

        double[] α = {dayBefore.α, dayOfInterest.α, dayAfter.α};
        if (H0 >= 0) {

            solar.approxSunRiseAndSet(m_trs, H0);

            for (int i = 0; i < 3; i++) {

                νRts[i] = ν + 360.985647 * m_trs[i];
                n = m_trs[i] + ΔT / 86400.0;
                αPrime[i] = solar.Interpolate(n,α);
                δPrime[i] = solar.Interpolate(n,δ);
                HPrime[i] = solar.limitDegrees180pm(νRts[i] + longitude - αPrime[i]);

                h_rts[i] = solar.rtsSunAltitude(latitude, δPrime[i], HPrime[i]);
            }
            double T=m_trs[0] - HPrime[0] / 360.0;
            double R=solar.sunRiseAndSet(m_trs, h_rts, δPrime, latitude, HPrime, h0Prime, 1);
            double S=solar.sunRiseAndSet(m_trs, h_rts, δPrime, latitude, HPrime, h0Prime, 2);
            moontransit = solar.dayFracToLocalHour(T, timezone);
            moonrise = solar.dayFracToLocalHour(R, timezone);
            moonset = solar.dayFracToLocalHour(S, timezone);
          /*  System.out.println("MoonRise       :" + AstroLib.SecTime(moonrise));
            System.out.println("MoonTransit    :" + AstroLib.SecTime(moontransit));
            System.out.println("MoonSet        :" + AstroLib.SecTime(moonset));*/
             System.out.println(AstroLib.getStringHHMMSSS(moonrise)+"  "+AstroLib.getStringHHMMSSS(moontransit)+" "+AstroLib.getStringHHMMSSS(moonset));
           }

     }
    
 	public double[] calculateMoonRiseTransitSet(double jd, double latitude, double longitude, double timezone, int temperature, int pressure, int altitude)
    {
       double[] m_trs, h_rts, νRts, αPrime, δPrime,HPrime,moonRiseSet;
        m_trs = new double[3];
        h_rts = new double[3];
        νRts = new double[3];
        αPrime = new double[3];
        δPrime = new double[3];
        HPrime = new double[3];
        moonRiseSet= new double[3];
        double ν, H0, n;
        SolarPosition solar=new SolarPosition ();
        Equatorial dayBefore, dayOfInterest, dayAfter;
        jd = Math.floor(jd + 0.5) - 0.5;

        double ΔT =AstroLib.calculateTimeDifference(jd);
        dayBefore = calculateMoonEqutarialCoordinates(jd - 1, ΔT);
        dayOfInterest = calculateMoonEqutarialCoordinates(jd, ΔT);
        dayAfter = calculateMoonEqutarialCoordinates(jd + 1, ΔT);
        // private final int MOONRADIUS=15;
        double h0Prime =getLunarRiseSetAltitude(dayOfInterest.Δ,temperature,pressure,altitude);


        ν = SolarPosition.calculateGreenwichSiderealTime(jd, ΔT);
        m_trs[0] = solar.approxSunTransitTime(dayOfInterest.α, longitude, ν);
        H0 = solar.getHourAngleAtRiseSet(latitude, dayOfInterest.δ, h0Prime);
      
        double[] δ = {dayBefore.δ, dayOfInterest.δ, dayAfter.δ};
    
  
        if ((dayOfInterest.α - dayBefore.α) > 180.0) {
            dayBefore.α+= 360;
        } else if ((dayOfInterest.α - dayBefore.α) < -180.0) {
            dayOfInterest.α += 360;
        }
        if ((dayAfter.α - dayOfInterest.α) > 180.0) {
            dayOfInterest.α+= 360;
        } else if ((dayAfter.α - dayOfInterest.α) < -180.0) {
            dayAfter.α += 360;
        }

        double[] α = {dayBefore.α, dayOfInterest.α, dayAfter.α};
        if (H0 >= 0) {

            solar.approxSunRiseAndSet(m_trs, H0);

            for (int i = 0; i < 3; i++) {

                νRts[i] = ν + 360.985647 * m_trs[i];
                n = m_trs[i] + ΔT / 86400.0;
                αPrime[i] = solar.Interpolate(n,α);
                δPrime[i] = solar.Interpolate(n,δ);
                HPrime[i] = solar.limitDegrees180pm(νRts[i] + longitude - αPrime[i]);

                h_rts[i] = solar.rtsSunAltitude(latitude, δPrime[i], HPrime[i]);
            }
            double T=m_trs[0] - HPrime[0] / 360.0;
            double R=solar.sunRiseAndSet(m_trs, h_rts, δPrime, latitude, HPrime, h0Prime, 1);
            double S=solar.sunRiseAndSet(m_trs, h_rts, δPrime, latitude, HPrime, h0Prime, 2);
            moonRiseSet[1] = solar.dayFracToLocalHour(T, timezone);
            moonRiseSet[0]= solar.dayFracToLocalHour(R, timezone);
            moonRiseSet[2]= solar.dayFracToLocalHour(S, timezone);

        }

    	
		return  moonRiseSet;

     }
    double getHorizontalParallax(double RadiusVector) {
        return Math.toDegrees(MATH.asin(6378.14 / RadiusVector));
    }

    double getLunarRiseSetAltitude (double Δ,int temperature, int pressure, int altitude)
    {
        //double Δinkm=149598000.0*Δ;
        double π=getHorizontalParallax(Δ);
        double ho=0.7275*π
                -AstroLib.getApparentAtmosphericRefraction(0) * AstroLib.getWeatherCorrectionCoefficent(temperature, pressure)
                -AstroLib.getAltitudeCorrection(altitude);
        return ho;
    }

}



