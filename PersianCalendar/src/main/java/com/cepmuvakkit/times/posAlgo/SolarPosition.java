/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cepmuvakkit.times.posAlgo;

public class SolarPosition {
    // public enum Salats {};

    private static byte L_COUNT, B_COUNT, R_COUNT, Y_COUNT;
    private final double SUNRADIUS = 0.26667;
    private final byte FAJR = 0, SUNRISE = 1, SUNTRANSIT = 2, ASR_SHAFI = 3, ASR_HANEFI = 4, SUNSET = 5, ISHA = 6, SUN_COUNT = 7;
    private final byte FAJR_ = 0, ISRAK = 1, SUNTRANSIT_ = 2, ASRHANEFI = 3, ISFIRAR = 4, SUNSET_ = 5, KERAHAT_COUNT = 6, DUHA = 7, ISTIVA = 8;
///////////////////////////////////////////////////
///  Earth Periodic Terms
///////////////////////////////////////////////////
    private static final double LTERMS[][][] = {
        {{175347046.0, 0, 0}, {3341656.0, 4.6692568, 6283.07585}, {34894.0, 4.6261, 12566.1517}, {3497.0, 2.7441, 5753.3849}, {3418.0, 2.8289, 3.5231}, {3136.0, 3.6277, 77713.7715}, {2676.0, 4.4181, 7860.4194}, {2343.0, 6.1352, 3930.2097}, {1324.0, 0.7425, 11506.7698}, {1273.0, 2.0371, 529.691}, {1199.0, 1.1096, 1577.3435}, {990, 5.233, 5884.927}, {902, 2.045, 26.298}, {857, 3.508, 398.149}, {780, 1.179, 5223.694}, {753, 2.533, 5507.553}, {505, 4.583, 18849.228}, {492, 4.205, 775.523}, {357, 2.92, 0.067}, {317, 5.849, 11790.629}, {284, 1.899, 796.298}, {271, 0.315, 10977.079}, {243, 0.345, 5486.778}, {206, 4.806, 2544.314}, {205, 1.869, 5573.143}, {202, 2.458, 6069.777}, {156, 0.833, 213.299}, {132, 3.411, 2942.463}, {126, 1.083, 20.775}, {115, 0.645, 0.98}, {103, 0.636, 4694.003}, {102, 0.976, 15720.839}, {102, 4.267, 7.114}, {99, 6.21, 2146.17}, {98, 0.68, 155.42}, {86, 5.98, 161000.69}, {85, 1.3, 6275.96}, {85, 3.67, 71430.7}, {80, 1.81, 17260.15}, {79, 3.04, 12036.46}, {75, 1.76, 5088.63}, {74, 3.5, 3154.69}, {74, 4.68, 801.82}, {70, 0.83, 9437.76}, {62, 3.98, 8827.39}, {61, 1.82, 7084.9}, {57, 2.78, 6286.6}, {56, 4.39, 14143.5}, {56, 3.47, 6279.55}, {52, 0.19, 12139.55}, {52, 1.33, 1748.02}, {51, 0.28, 5856.48}, {49, 0.49, 1194.45}, {41, 5.37, 8429.24}, {41, 2.4, 19651.05}, {39, 6.17, 10447.39}, {37, 6.04, 10213.29}, {37, 2.57, 1059.38}, {36, 1.71, 2352.87}, {36, 1.78, 6812.77}, {33, 0.59, 17789.85}, {30, 0.44, 83996.85}, {30, 2.74, 1349.87}, {25, 3.16, 4690.48}},
        {{628331966747.0, 0, 0}, {206059.0, 2.678235, 6283.07585}, {4303.0, 2.6351, 12566.1517}, {425.0, 1.59, 3.523}, {119.0, 5.796, 26.298}, {109.0, 2.966, 1577.344}, {93, 2.59, 18849.23}, {72, 1.14, 529.69}, {68, 1.87, 398.15}, {67, 4.41, 5507.55}, {59, 2.89, 5223.69}, {56, 2.17, 155.42}, {45, 0.4, 796.3}, {36, 0.47, 775.52}, {29, 2.65, 7.11}, {21, 5.34, 0.98}, {19, 1.85, 5486.78}, {19, 4.97, 213.3}, {17, 2.99, 6275.96}, {16, 0.03, 2544.31}, {16, 1.43, 2146.17}, {15, 1.21, 10977.08}, {12, 2.83, 1748.02}, {12, 3.26, 5088.63}, {12, 5.27, 1194.45}, {12, 2.08, 4694}, {11, 0.77, 553.57}, {10, 1.3, 6286.6}, {10, 4.24, 1349.87}, {9, 2.7, 242.73}, {9, 5.64, 951.72}, {8, 5.3, 2352.87}, {6, 2.65, 9437.76}, {6, 4.67, 4690.48}},
        {{52919.0, 0, 0}, {8720.0, 1.0721, 6283.0758}, {309.0, 0.867, 12566.152}, {27, 0.05, 3.52}, {16, 5.19, 26.3}, {16, 3.68, 155.42}, {10, 0.76, 18849.23}, {9, 2.06, 77713.77}, {7, 0.83, 775.52}, {5, 4.66, 1577.34}, {4, 1.03, 7.11}, {4, 3.44, 5573.14}, {3, 5.14, 796.3}, {3, 6.05, 5507.55}, {3, 1.19, 242.73}, {3, 6.12, 529.69}, {3, 0.31, 398.15}, {3, 2.28, 553.57}, {2, 4.38, 5223.69}, {2, 3.75, 0.98}},
        {{289.0, 5.844, 6283.076}, {35, 0, 0}, {17, 5.49, 12566.15}, {3, 5.2, 155.42}, {1, 4.72, 3.52}, {1, 5.3, 18849.23}, {1, 5.97, 242.73}},
        {{114.0, 3.142, 0}, {8, 4.13, 6283.08}, {1, 3.84, 12566.15}},
        {{1, 3.14, 0}}
    };
    private static final double BTERMS[][][] = {
        {{280.0, 3.199, 84334.662}, {102.0, 5.422, 5507.553}, {80, 3.88, 5223.69}, {44, 3.7, 2352.87}, {32, 4, 1577.34}},
        {{9, 3.9, 5507.55}, {6, 1.73, 5223.69}}
    };
    /*     private static final float BTERMS[][][] = {
    {{280.0f, 3.199f, 84334.662f}, {102.0f, 5.422f, 5507.553f}, {80, 3.88f, 5223.69f}, {44, 3.7f, 2352.87f}, {32, 4, 1577.34f}},
    {{9f, 3.9f, 5507.55f}, {6, 1.73f, 5223.69f}}
    };*/
    private static final double RTERMS[][][] = {
        {{100013989.0, 0, 0}, {1670700.0, 3.0984635, 6283.07585}, {13956.0, 3.05525, 12566.1517}, {3084.0, 5.1985, 77713.7715}, {1628.0, 1.1739, 5753.3849}, {1576.0, 2.8469, 7860.4194}, {925.0, 5.453, 11506.77}, {542.0, 4.564, 3930.21}, {472.0, 3.661, 5884.927}, {346.0, 0.964, 5507.553}, {329.0, 5.9, 5223.694}, {307.0, 0.299, 5573.143}, {243.0, 4.273, 11790.629}, {212.0, 5.847, 1577.344}, {186.0, 5.022, 10977.079}, {175.0, 3.012, 18849.228}, {110.0, 5.055, 5486.778}, {98, 0.89, 6069.78}, {86, 5.69, 15720.84}, {86, 1.27, 161000.69}, {65, 0.27, 17260.15}, {63, 0.92, 529.69}, {57, 2.01, 83996.85}, {56, 5.24, 71430.7}, {49, 3.25, 2544.31}, {47, 2.58, 775.52}, {45, 5.54, 9437.76}, {43, 6.01, 6275.96}, {39, 5.36, 4694}, {38, 2.39, 8827.39}, {37, 0.83, 19651.05}, {37, 4.9, 12139.55}, {36, 1.67, 12036.46}, {35, 1.84, 2942.46}, {33, 0.24, 7084.9}, {32, 0.18, 5088.63}, {32, 1.78, 398.15}, {28, 1.21, 6286.6}, {28, 1.9, 6279.55}, {26, 4.59, 10447.39}},
        {{103019.0, 1.10749, 6283.07585}, {1721.0, 1.0644, 12566.1517}, {702.0, 3.142, 0}, {32, 1.02, 18849.23}, {31, 2.84, 5507.55}, {25, 1.32, 5223.69}, {18, 1.42, 1577.34}, {10, 5.91, 10977.08}, {9, 1.42, 6275.96}, {9, 0.27, 5486.78}},
        {{4359.0, 5.7846, 6283.0758}, {124.0, 5.579, 12566.152}, {12, 3.14, 0}, {9, 3.63, 77713.77}, {6, 1.87, 5573.14}, {3, 5.47, 18849.23}},
        {{145.0, 4.273, 6283.076}, {7, 3.92, 12566.15}},
        {{4, 2.56, 6283.08}}
    };
////////////////////////////////////////////////////////////////
///  Periodic Terms for the nutation in longitude and obliquity
////////////////////////////////////////////////////////////////
    private static final byte YTERMS[][] = {
        {0, 0, 0, 0, 1}, {-2, 0, 0, 2, 2}, {0, 0, 0, 2, 2}, {0, 0, 0, 0, 2}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {-2, 1, 0, 2, 2}, {0, 0, 0, 2, 1}, {0, 0, 1, 2, 2}, {-2, -1, 0, 2, 2}, {-2, 0, 1, 0, 0}, {-2, 0, 0, 2, 1}, {0, 0, -1, 2, 2}, {2, 0, 0, 0, 0}, {0, 0, 1, 0, 1}, {2, 0, -1, 2, 2}, {0, 0, -1, 0, 1}, {0, 0, 1, 2, 1}, {-2, 0, 2, 0, 0}, {0, 0, -2, 2, 1}, {2, 0, 0, 2, 2}, {0, 0, 2, 2, 2}, {0, 0, 2, 0, 0}, {-2, 0, 1, 2, 2}, {0, 0, 0, 2, 0}, {-2, 0, 0, 2, 0}, {0, 0, -1, 2, 1}, {0, 2, 0, 0, 0}, {2, 0, -1, 0, 1}, {-2, 2, 0, 2, 2}, {0, 1, 0, 0, 1}, {-2, 0, 1, 0, 1}, {0, -1, 0, 0, 1}, {0, 0, 2, -2, 0}, {2, 0, -1, 2, 1}, {2, 0, 1, 2, 2}, {0, 1, 0, 2, 2}, {-2, 1, 1, 0, 0}, {0, -1, 0, 2, 2}, {2, 0, 0, 2, 1}, {2, 0, 1, 0, 0}, {-2, 0, 2, 2, 2}, {-2, 0, 1, 2, 1}, {2, 0, -2, 0, 1}, {2, 0, 0, 0, 1}, {0, -1, 1, 0, 0}, {-2, -1, 0, 2, 1}, {-2, 0, 0, 0, 1}, {0, 0, 2, 2, 1}, {-2, 0, 2, 0, 1}, {-2, 1, 0, 2, 1}, {0, 0, 1, -2, 0}, {-1, 0, 1, 0, 0}, {-2, 1, 0, 0, 0}, {1, 0, 0, 0, 0}, {0, 0, 1, 2, 0}, {0, 0, -2, 2, 2}, {-1, -1, 1, 0, 0}, {0, 1, 1, 0, 0}, {0, -1, 1, 2, 2}, {2, -1, -1, 2, 2}, {0, 0, 3, 2, 2}, {2, -1, 0, 2, 2},};
    private static final double PETERMS[][] = {
        {-171996, -174.2, 92025, 8.9}, {-13187, -1.6, 5736, -3.1}, {-2274, -0.2, 977, -0.5}, {2062, 0.2, -895, 0.5}, {1426, -3.4, 54, -0.1}, {712, 0.1, -7, 0}, {-517, 1.2, 224, -0.6}, {-386, -0.4, 200, 0}, {-301, 0, 129, -0.1}, {217, -0.5, -95, 0.3}, {-158, 0, 0, 0}, {129, 0.1, -70, 0}, {123, 0, -53, 0}, {63, 0, 0, 0}, {63, 0.1, -33, 0}, {-59, 0, 26, 0}, {-58, -0.1, 32, 0}, {-51, 0, 27, 0}, {48, 0, 0, 0}, {46, 0, -24, 0}, {-38, 0, 16, 0}, {-31, 0, 13, 0}, {29, 0, 0, 0}, {29, 0, -12, 0}, {26, 0, 0, 0}, {-22, 0, 0, 0}, {21, 0, -10, 0}, {17, -0.1, 0, 0}, {16, 0, -8, 0}, {-16, 0.1, 7, 0}, {-15, 0, 9, 0}, {-13, 0, 7, 0}, {-12, 0, 6, 0}, {11, 0, 0, 0}, {-10, 0, 5, 0}, {-8, 0, 3, 0}, {7, 0, -3, 0}, {-7, 0, 0, 0}, {-7, 0, 3, 0}, {-7, 0, 3, 0}, {6, 0, 0, 0}, {6, 0, -3, 0}, {6, 0, -3, 0}, {-6, 0, 3, 0}, {-6, 0, 3, 0}, {5, 0, 0, 0}, {-5, 0, 3, 0}, {-5, 0, 3, 0}, {-5, 0, 3, 0}, {4, 0, 0, 0}, {4, 0, 0, 0}, {4, 0, 0, 0}, {-4, 0, 0, 0}, {-4, 0, 0, 0}, {-4, 0, 0, 0}, {3, 0, 0, 0}, {-3, 0, 0, 0}, {-3, 0, 0, 0}, {-3, 0, 0, 0}, {-3, 0, 0, 0}, {-3, 0, 0, 0}, {-3, 0, 0, 0}, {-3, 0, 0, 0},};

    /**
     * If its absolute value is greater than 20 minutes,
     * by adding or subtracting 1440.
     * @param   minutes
     * @return  limitminutes
     */
    double limitMinutes(double minutes) {
        double limited = minutes;
        if (limited < -20.0) {
            limited += 1440.0;
        } else if (limited > 20.0) {
            limited -= 1440.0;
        }
        return limited;
    }

    static double limitDegrees(double degrees) {
        double limited;
        degrees /= 360.0;
        limited = 360.0 * (degrees - Math.floor(degrees));
        if (limited < 0) {
            limited += 360.0;
        }
        return limited;
    }

    double limitDegrees180pm(double degrees) {
        double limited;

        degrees /= 360.0;
        limited = 360.0 * (degrees - Math.floor(degrees));
        if (limited < -180.0) {
            limited += 360.0;
        } else if (limited > 180.0) {
            limited -= 360.0;
        }

        return limited;
    }

    double limitDegrees180(double degrees) {
        double limited;

        degrees /= 180.0;
        limited = 180.0 * (degrees - Math.floor(degrees));
        if (limited < 0) {
            limited += 180.0;
        }

        return limited;
    }

    double limitZero2one(double value) {
        double limited;

        limited = value - Math.floor(value);
        if (limited < 0) {
            limited += 1.0;
        }

        return limited;
    }

    double dayFracToLocalHour(double dayfrac, double timezone) {
        return 24.0 * limitZero2one(dayfrac + timezone / 24.0);
    }

///////////////////////////////////////////////////////////////////////////////////////////////////
/// Calculate the Earth heliocentric longitude, latitude, and radius vector (L, B, and R): BEGIN///
///////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Calculate the Earth heliocentric longitude, L in degrees,
     * <ul> “Heliocentric” means that the Earth position is calculated
     * with respect to the center of the sun </ul>
     * <ul>  L0i = Ai *cos ( Bi + Ci* JME ) </ul>
     * <ul>  L0=ΣL0i (0,n) </ul>
     * where,- i is the ith row for the term L0, n is the νmber of rows for the term L0.
     * <ul> Ai , Bi , and Ci are the values in the ith row and A, B, and C columns </ul>
     * <ul> L =  L0 + L1* JME + L2* JME^2 + L3* JME^3 + L4* JME ^4+ L5* JME^5/10^8 </ul>
     * <ul> L (in degrees) = L(radians)*180/pi </ul>
     * @param  jme the Julian Ephemeris Millennium (JME) for the 2000 standard epoch.
     * @return the Earth heliocentric longitude, L in degrees.
     */
    private double earthHeliocentricLongitude(double jme) {
        L_COUNT = (byte) LTERMS.length;

        double[] sum = new double[L_COUNT];
        int i;

        for (i = 0; i < L_COUNT; i++) {
            sum[i] = earthPeriodicTermSummation(LTERMS[i], LTERMS[i].length, jme);
        }

        return limitDegrees(Math.toDegrees(earthValues(sum, L_COUNT, jme)));

    }

    /**
     * Calculate the Earth radius vector, R (in Astronomical Units, AU),
     * Note that there is no R5, consequently, replace it by zero.
     * “Heliocentric” means that the Earth position is calculated with respect to the center of the sun
     * <ul>      R = (R0 + R1* JME + R2* JME^2 + R3* JME^3 + R4* JME ^4)/10^8 </ul>
     * @param jme the Julian Ephemeris Millennium (JME) for the 2000 standard epoch.
     * @return the Earth radius vector, R (in Astronomical Units, AU),
     */
    private double earthRadiusVector(double jme) {
        R_COUNT = (byte) RTERMS.length;
        double[] sum = new double[R_COUNT];
        int i;

        for (i = 0; i < R_COUNT; i++) {
            sum[i] = earthPeriodicTermSummation(RTERMS[i], RTERMS[i].length, jme);
        }

        return earthValues(sum, R_COUNT, jme);

    }

    /**
     * Calculate the Earth heliocentric latitude, B (in degrees),
     * <ul> “Heliocentric” means that the Earth position is calculated with respect to the center of the sun </ul>
     * <ul> B=(B0 + B1* JME )/10^8 </ul>
     * <ul> B (in degrees) = B(radians)*180/pi </ul>
     * @param  jme the Julian Ephemeris Millennium (JME) for the 2000 standard epoch.
     * @return the Earth heliocentric latitude, B (in degrees).
     */
    private double earthHeliocentricLatitude(double jme) {
        B_COUNT = (byte) BTERMS.length;

        double[] sum = new double[B_COUNT];
        int i;
        for (i = 0; i < B_COUNT; i++) {
            sum[i] = earthPeriodicTermSummation(BTERMS[i], BTERMS[i].length, jme);
        }

        return Math.toDegrees(earthValues(sum, B_COUNT, jme));

    }

    /**
     * Calculate L0i = Ai *cos ( Bi + Ci* JME )
     * <ul> L0=ΣL0i (0,n) </ul>
     * where,- i is the ith row for the term L0 in Table A4.2, n is the νmber of rows for the term L0
     * <ul> Ai , Bi , and Ci are the values in the ith row and A, B, and C columns in Table A4.2, for the term L0 (in radians) </ul>
     * @param  jme  the Julian Ephemeris Millennium (JME) for the 2000 standard epoch.
     * @param terms LTERMS, BTERMS,RTERMS
     * @return L0i = Ai *cos ( Bi + Ci* JME )
     */
    private double earthPeriodicTermSummation(double terms[][], int count, double jme) {
        int i;
        double sum = 0;
        for (i = 0; i < count; i++) {
            sum += terms[i][0] * Math.cos(terms[i][1] + terms[i][2] * jme);
        }
        return sum;
    }

    /**
     * Calculate the Earth Values, L, R and B.
     * replacing all the Ls by Bs  and Rs in all equations.
     * <ul>L=(L0 + L1* JME + L2* JME^2 + L3* JME^3 + L4* JME ^4+ L5* JME^5)/ 10^8 </ul>
     * @param  jme  the Julian Ephemeris Millennium (JME) for the 2000 standard epoch.
     * @return the L,B and R.
     */
    private double earthValues(double termSum[], int count, double jme) {
        int i;
        double sum = 0;

        for (i = 0; i < count; i++) {
            sum += termSum[i] * MATH.pow(jme, i);
        }

        sum /= 1.0e8;

        return sum;
    }

    /**
     * Calculate the geocentric latitude, β (in degrees),
     * <ul> β= − B </ul>
     * @param   b is the  geocentric latitude
     * @return  β the geocentric latitude (in degrees)
     */
    double getGeocentricLatitude(double b) {
        return -b;
    }

    /**
     * Calculate the geocentric longitude, (in degrees),
     * Geocentric” means that the sun position is calculated with respect to the Earth center.
     * <ul>Θ= L + 180</ul>
     * Limit L to the range from 0 to 360. That can be accomplished
     * by dividing L by 360 and recording the decimal fraction of the division as F.
     * <p> If L is positive, then the limited L = 360 * F. If L is negative, then the limited L = 360 - 360 * F. </p>
     * @param  L Earth heliocentric longitude,
     * @return  the geocentric longitude Θ (in degrees)
     */
    double geocentricLongitude(double L) {
        double theta = L + 180.0;

        if (theta >= 360.0) {
            theta -= 360.0;
        }

        return theta;
    }

    /**
     * Calculate the mean elongation of the moon from the sun, X0 (in degrees),
     * <ul> X0 = 297.85036 + 445267.111480 * JCE- 0.0019142*JCE^2 +JCE^3/189474</ul>
     * @param  jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return mean elongation of the moon from the sun, X0 (in degrees).
     */
    private static double meanElongationMoonSun(double jce) {
        return AstroLib.thirdOrderPolynomial(1.0 / 189474.0, -0.0019142, 445267.11148, 297.85036, jce);
    }

    /**
     * Calculate the mean anomaly of the sun (Earth), X1 (in degrees),
     * <ul>X1 = 357.52772 + 35999.050340 * JCE−0.0001603*JCE^2 +JCE^3/300000</ul>
     * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return  mean anomaly of the sun (Earth), X1 (in degrees).
     */
    private static double meanAnomalySun(double jce) {
        return AstroLib.thirdOrderPolynomial(-1.0 / 300000.0, -0.0001603, 35999.05034, 357.52772, jce);
    }

    /**
     * Calculate the mean anomaly of the moon, X2 (in degrees),
     * <ul>X2 = 134.96298 + 477198.867398 * JCE + 0.0086972 *JCE^2 +JCE^3/56250  </ul>
     * @param  jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return mean anomaly of the moon, X2 (in degrees).
     */
    private static double meanAnomalyMoon(double jce) {
        return AstroLib.thirdOrderPolynomial(1.0 / 56250.0, 0.0086972, 477198.867398, 134.96298, jce);
    }

    /**
     * Calculate the moon’s argument of latitude, X3 (in degrees),
     * <ul> X3 = 9327191 + 483202.017538 * JCE −0.0036825 * JCE^2+JCE^3/327270  </ul>
     * @param  jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return the moon’s argument of latitude, X3 (in degrees).
     */
    private static double argumentLatitudeMoon(double jce) {
        return AstroLib.thirdOrderPolynomial(1.0 / 327270.0, -0.0036825, 483202.017538, 93.27191, jce);
    }

    /**
     * Calculate the longitude of the ascending node of the moon’s mean orbit on the ecliptic,
     * measured from the mean equinox of the date, X4 (in degrees),
     * <ul> X4 = 125.04452 − 1934.136261 * JCE+0.0020708*JCE^2 +JCE^3/450000</ul>
     * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return mean elongation of the moon from the sun, X0 (in degrees).
     */
    private static double ascendingLongitudeMoon(double jce) {
        return AstroLib.thirdOrderPolynomial(1.0 / 450000.0, 0.0020708, -1934.136261, 125.04452, jce);
    }

    /**
     * Calculate ΣXj*Yij Term Summation Xj is the jth X calculated by
     * <p> using  meanElongationMoonSun through ascendingLongitudeMoon fuctions </p>
     * <p> Yi, j is the value listed in ith row and jth Y column YTERMS matrix</p>
     * @param  i νmber
     * @param  x xj
     * @return ΣXj*Yij  Term Summation
     */
    private static double xyTermSummation(int i, double x[]) {
        int j;
        double sum = 0;
        int TERM_Y_COUNT = x.length;
        for (j = 0; j < TERM_Y_COUNT; j++) {
            sum += x[j] * YTERMS[i][j];
        }

        return sum;
    }

    /**
     * Calculate the nutation in obliquity,Δε  in degrees
     * <ul> Δε= ΣΔεi(i=0..n) / 36000000 </ul>
     * <ul> Δεi = (c + d * JCE )*cos ( Σ X *Y )</ul>
     * @param  Δεi the mean obliquity of the ecliptic  Δεi (in arc seconds)
     * @param  jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return  the nutation in obliquity,Δε  in degrees
     */
    static double nutationObliquity(double jce, double Δεi[]) {
        int i;
        double xyTermSum, sumε = 0;
        Y_COUNT = (byte) YTERMS.length;
        for (i = 0; i < Y_COUNT; i++) {
            xyTermSum = Math.toRadians(xyTermSummation(i, Δεi));
            sumε += (PETERMS[i][2] + jce * PETERMS[i][3]) * Math.cos(xyTermSum);
        }

        return sumε / 36000000.0;
    }

    /**
     * Calculate the nutation in longitude, Δψ (in degrees),
     * <ul>Δψ = ΣΔψi(i=0..n)/36000000 </ul>
     * <ul>Δψi = (ai + bi * JCE )*sin ( Σ Xj *Yij )</ul>
     * @param  Δψi the mean obliquity of the ecliptic  Δψi  (in arc seconds)
     * @param  jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return the nutation in longitude,  Δψ  (in degrees),
     */
    static double nutationLongitude(double jce, double X[]) {
        int i;
        double xyTermSum, sumPsi = 0;
        Y_COUNT = (byte) YTERMS.length;
        for (i = 0; i < Y_COUNT; i++) {
            xyTermSum = Math.toRadians(xyTermSummation(i, X));
            sumPsi += (PETERMS[i][0] + jce * PETERMS[i][1]) * Math.sin(xyTermSum);
        }

        return sumPsi / 36000000.0;
    }

    /**
     * Calculate the true obliquity of the ecliptic, ε(in degrees),
     * <ul>ε=ε0+Δε/3600 </ul>
     * @param ε0 the nutation in obliquity Δε in degrees.
     * @param Δε the mean obliquity of the ecliptic  ε0 (in arc seconds)
     * @return the true obliquity of the ecliptic, ε(in degrees),
     */
    static double eclipticTrueObliquity(double Δε, double ε0) {
        return Δε + ε0 / 3600.0;
    }

    /** Calculate the apparent sun longitude, λ (in degrees):
     *  <ul> λ=Θ+Δψ+Δτ </ul>
     * @param Δτ the aberration correction Δτ (in degrees).
     * @param Δψ the nutation in longitude Δψ (in degrees).
     * @param Θ  the geocentric longitude Θ (in degrees).
     * @return the apparent sun longitude, λ (in degrees).
     */
    private double apparentSunLongitude(double Θ, double Δψ, double Δτ) {
        return Θ + Δψ + Δτ;
    }

    /**
     * Calculate the aberration correction Δτ (in degrees):
     * <ul> Δτ=-20.4898/3600R </ul>
     * @param  r the Earth radius vector, R (in Astronomical Units, AU).
     * @return the aberration correction Δτ (in degrees).
     */
    private double aberrationCorrection(double r) {
        return -20.4898 / (3600.0 * r);
    }

    /**
     * Calculate the mean obliquity of the ecliptic, ε0 (in arc seconds)
     * <ul>ε0 = 84381.448−4680.93U −155 U^2 + 1999.25U^3</ul>
     * <ul>...−51.38U^4− -249.67U^5 − 39.05U^6 + 7.12 U^7 + 27.87 U^8 + 5.79U^9 + 2.45U^10</ul>
     * where <ul> U = JME/10.</ul>
     * @param jme the Julian Ephemeris Millennium (JME) for the 2000 standard epoch.
     * @return the mean obliquity of the ecliptic  ε0 (in arc seconds)
     */
    static double eclipticMeanObliquity(double jme) {
        double u = jme / 10.0;

        return 84381.448 + u * (-4680.93 + u * (-1.55 + u * (1999.25 + u * (-51.38 + u * (-249.67
                + u * (-39.05 + u * (7.12 + u * (27.87 + u * (5.79 + u * 2.45)))))))));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    /// Calculate the Earth heliocentric longitude, latitude, and radius vector (L, B, and R): END  ///
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Calculate the mean sidereal time at Greenwich, ν0 (in degrees),:
     * <ul> ν0=280.46061837+ 360.98564736629 *(JD −2451545)+0.000387933*JC^2−JC^3/38710000 </ul>
     * @param jd  is the Julian Day
     * @param jc the Julian  Century
     * @return the mean  sidereal time at Greenwich, ν0 (in degrees)
     */
    double greenwichMeanSiderealTime(double jd, double jc) {


        return limitDegrees(280.46061837 + 360.98564736629 * (jd - 2451545.0)
                + jc * jc * (0.000387933 - jc / 38710000.0));
    }

    /**
     * Calculate the mean sidereal time at Greenwich, ν0 (in degrees),:
     * <ul> ν0=280.46061837+ 360.98564736629 *(JD −2451545)+0.000387933*JC^2−JC^3/38710000 </ul>
     * @param jd  is the Julian Day
     * @return the mean  sidereal time at Greenwich, ν0 (in degrees)
     */
    static double greenwichMeanSiderealTime(double jd) {

        double jc = (jd - 2451545.0) / 36525.0;// jc the Julian  Century
        return limitDegrees(280.46061837 + 360.98564736629 * (jd - 2451545.0)
                + jc * jc * (0.000387933 - jc / 38710000.0));
    }

    /**
     * Calculate the apparent sidereal time at Greenwich, ν (in degrees):
     * <ul> ν=ν+Δψcos(ε) </ul>
     * @param   ε   JME is the Julian Ephemeris Millennium.
     * @return  the apparent sidereal time at Greenwich, ν (in degrees)
     */
    static double greenwichSiderealTime(double ν0, double Δψ, double ε) {
        return ν0 + Δψ * Math.cos(Math.toRadians(ε));
    }

    /**
     * Calculate the Geometric Mean Longitude of the Sun.
     * This value is close to 0? at the spring equinox,
     * 90? at the summer solstice, 180? at the automne equinox
     * and 270? at the winter solstice.
     *
     * @param   jme   JME is the Julian Ephemeris Millennium.
     * @return  the  Sun mean longitude.
     */
    double sunMeanLongitude(double jme) {
        return limitDegrees(280.4664567 + jme * (360007.6982779 + jme * (0.03032028
                + jme * (1 / 49931.0 + jme * (-1 / 15300.0 + jme * (-1 / 2000000.0))))));
    }

    /**
     * Calculate the geocentric sun right ascension, α (in degrees):
     * <ul> α=  arctan( (sin λ*cos ε−tan β*sin ε)/cos λ ) </ul>
     * @param   λ  apparent sun longitude (in degrees)
     * @param   ε  the true obliquity of the ecliptic (in degrees)
     * @param   β  the geocentric latitude (in degrees)
     * @return  α is the geocentric right ascention (in degrees).
     */
    static double geocentricRightAscension(double λ, double ε, double β) {
        double λRad = Math.toRadians(λ);
        double εRad = Math.toRadians(ε);
        return limitDegrees(Math.toDegrees(MATH.atan2(Math.sin(λRad) * Math.cos(εRad) - Math.tan(Math.toRadians(β) * Math.sin(εRad)), Math.cos(λRad))));

    }

    /**
     * Calculate the geocentric sun declination δ (in degrees):
     * <ul>     δ= asin(sin β*cos ε+cos β*sin ε*sin λ) </ul>
     * @param   λ  apparent sun longitude (in degrees)
     * @param   ε  the true obliquity of the ecliptic (in degrees)
     * @param   β  the geocentric latitude (in degrees)
     * @return  the geocentric sun declination, δ (in degrees):
     */
    static double geocentricDeclination(double λ, double ε, double β) {
        double βRad = Math.toRadians(β);
        double εRad = Math.toRadians(ε);

        return Math.toDegrees(MATH.asin(Math.sin(βRad) * Math.cos(εRad)
                + Math.cos(βRad) * Math.sin(εRad) * Math.sin(Math.toRadians(λ))));
    }

    double observerHourAngle(double ν, double longitude, double αDeg) {
        return limitDegrees(ν + longitude - αDeg);
    }

    double sunEquatorialHorizontalParallax(double r) {
        return 8.794 / (3600.0 * r);
    }

    void sunRightAscensionParallaxAndTopocentricDec(double latitude, double elevation,
            double xi, double h, double δ, double δα, double δPrime) {
        double δαRad;
        double latRad = Math.toRadians(latitude);
        double xiRad = Math.toRadians(xi);
        double hRad = Math.toRadians(h);
        double δRad = Math.toRadians(δ);
        double u = MATH.atan(0.99664719 * Math.tan(latRad));
        double y = 0.99664719 * Math.sin(u) + elevation * Math.sin(latRad) / 6378140.0;
        double x = Math.cos(u) + elevation * Math.cos(latRad) / 6378140.0;

        δαRad = MATH.atan2(-x * Math.sin(xiRad) * Math.sin(hRad),
                Math.cos(δRad) - x * Math.sin(xiRad) * Math.cos(hRad));

        δPrime = Math.toDegrees(MATH.atan2((Math.sin(δRad) - y * Math.sin(xiRad)) * Math.cos(δαRad),
                Math.cos(δRad) - x * Math.sin(xiRad) * Math.cos(hRad)));

        δα = Math.toDegrees(δαRad);
    }

    double topocentricSunRightAscension(double αDeg, double δα) {
        return αDeg + δα;
    }

    double topocentricLocalHourAngle(double h, double δα) {
        return h - δα;
    }

    double topocentricElevationAngle(double latitude, double δPrime, double hPrime) {
        double latRad = Math.toRadians(latitude);
        double δPrimeRad = Math.toRadians(δPrime);

        return Math.toDegrees(MATH.asin(Math.sin(latRad) * Math.sin(δPrimeRad)
                + Math.cos(latRad) * Math.cos(δPrimeRad) * Math.cos(Math.toRadians(hPrime))));
    }

    double atmosphericRefractionCorrection(double pressure, double temperature,
            double atmosRefract, double e0) {
        double Δe = 0;

        if (e0 >= -1 * (SUNRADIUS + atmosRefract)) {
            Δe = (pressure / 1010.0) * (283.0 / (273.0 + temperature))
                    * 1.02 / (60.0 * Math.tan(Math.toRadians(e0 + 10.3 / (e0 + 5.11))));
        }

        return Δe;
    }

    double topocentricElevationAngleCorrected(double e0, double ΔE) {
        return e0 + ΔE;
    }

    double topocentricZenithAngle(double e) {
        return 90.0 - e;
    }

    double topocentricAzimuthAngleNeg180180(double hPrime, double latitude, double δPrime) {
        double hPrimeRad = Math.toRadians(hPrime);
        double latRad = Math.toRadians(latitude);

        return Math.toDegrees(MATH.atan2(Math.sin(hPrimeRad),
                Math.cos(hPrimeRad) * Math.sin(latRad) - Math.tan(Math.toRadians(δPrime)) * Math.cos(latRad)));
    }

    double topocentricAzimuthAngleZero360(double azimuth180) {
        return azimuth180 + 180.0;
    }

    double surfaceIncidenceAngle(double zenith, double azimuth180, double azmRotation,
            double slope) {
        double zenithRad = Math.toRadians(zenith);
        double slopeRad = Math.toRadians(slope);

        return Math.toDegrees(MATH.acos(Math.cos(zenithRad) * Math.cos(slopeRad)
                + Math.sin(slopeRad) * Math.sin(zenithRad) * Math.cos(Math.toRadians(azimuth180 - azmRotation))));
    }

    double approxSunTransitTime(double αo, double longitude, double ν) {
        return (αo - longitude - ν) / 360.0;
    }

    double getHourAngleAtRiseSet(double latitude, double δo, double h0Prime) {
        double h0 = -99999;
        double latitudeRad = Math.toRadians(latitude);
        double δoRad = Math.toRadians(δo);
        double argument = (Math.sin(Math.toRadians(h0Prime)) - Math.sin(latitudeRad) * Math.sin(δoRad))
                / (Math.cos(latitudeRad) * Math.cos(δoRad));

        if (Math.abs(argument) <= 1) {
            h0 = limitDegrees180(Math.toDegrees(MATH.acos(argument)));
        }

        return h0;
    }

    void approxSunRiseAndSet(double[] mRts, double h0) {
        double h0Dfrac = h0 / 360.0;

        mRts[1] = limitZero2one(mRts[0] - h0Dfrac);
        mRts[2] = limitZero2one(mRts[0] + h0Dfrac);
        mRts[0] = limitZero2one(mRts[0]);

    }

    void approxSalatTimes(double[] mRts, double[] h0) {
        mRts[SUNRISE] = limitZero2one(mRts[SUNTRANSIT] - h0[SUNRISE] / 360.0);
        mRts[SUNSET] = limitZero2one(mRts[SUNTRANSIT] + h0[SUNSET] / 360.0);
        mRts[ASR_SHAFI] = limitZero2one(mRts[SUNTRANSIT] + h0[ASR_SHAFI] / 360.0);
        mRts[ASR_HANEFI] = limitZero2one(mRts[SUNTRANSIT] + h0[ASR_HANEFI] / 360.0);
        mRts[FAJR] = limitZero2one(mRts[SUNTRANSIT] - h0[FAJR] / 360.0);
        mRts[ISHA] = limitZero2one(mRts[SUNTRANSIT] + h0[ISHA] / 360.0);
        mRts[SUNTRANSIT] = limitZero2one(mRts[SUNTRANSIT]);

    }

    void approxKerahatTimes(double[] mRts, double[] h0) {
        mRts[FAJR_] = limitZero2one(mRts[SUNTRANSIT_] - h0[FAJR_] / 360.0);
        mRts[ISRAK] = limitZero2one(mRts[SUNTRANSIT_] - h0[ISRAK] / 360.0);
        mRts[ASRHANEFI] = limitZero2one(mRts[SUNTRANSIT_] + h0[ASRHANEFI] / 360.0);
        mRts[ISFIRAR] = limitZero2one(mRts[SUNTRANSIT_] + h0[ISFIRAR] / 360.0);
        mRts[SUNSET] = limitZero2one(mRts[SUNTRANSIT_] + h0[SUNSET] / 360.0);
        mRts[SUNTRANSIT_] = limitZero2one(mRts[SUNTRANSIT_]);

    }

    double rtsAlphaDeltaPrime(double[] ad, double n) {
        double a = ad[1] - ad[0];
        double b = ad[2] - ad[1];

        if (Math.abs(a) >= 2.0) {
            a = limitZero2one(a);
        }
        if (Math.abs(b) >= 2.0) {
            b = limitZero2one(b);
        }

        return ad[1] + n * (a + b + (b - a) * n) / 2.0;
    }

    double Interpolate(double n, double[] Y) {
        double a = Y[1] - Y[0];
        double b = Y[2] - Y[1];
        double c = Y[0] + Y[2] - 2 * Y[1];

        return Y[1] + n / 2 * (a + b + n * c);
    }

    double rtsSunAltitude(double latitude, double δPrime, double hPrime) {
        double latitudeRad = Math.toRadians(latitude);
        double δPrimeRad = Math.toRadians(δPrime);

        return Math.toDegrees(MATH.asin(Math.sin(latitudeRad) * Math.sin(δPrimeRad)
                + Math.cos(latitudeRad) * Math.cos(δPrimeRad) * Math.cos(Math.toRadians(hPrime))));
    }

    double sunRiseAndSet(double[] mRts, double[] hRts, double[] δPrime, double latitude,
            double[] hPrime, double h0Prime, int sun) {
        return mRts[sun] + (hRts[sun] - h0Prime)
                / (360.0 * Math.cos(Math.toRadians(δPrime[sun])) * Math.cos(Math.toRadians(latitude)) * Math.sin(Math.toRadians(hPrime[sun])));
    }

    public void calculateSunRiseTransitSet(double[] spa, double jd) {

        double[] mRts, hRts, νRts, αPrime, δPrime, hPrime;
        mRts = new double[3];
        hRts = new double[3];
        νRts = new double[3];
        αPrime = new double[3];
        δPrime = new double[3];
        hPrime = new double[3];
        double atmosRefract = 0.5667;
        double h0Prime = -1 * (SUNRADIUS + atmosRefract);
        double ν, h0, n;
        Equatorial dayBefore, dayOfInterest, dayAfter;
        jd = Math.floor(jd + 0.5) - 0.5;
        double ΔT = AstroLib.calculateTimeDifference(jd);
        double longitude = 32.85, latitude = 39.95, timezone = 2;//ANKARA position
        //double longitude =-116.8625, latitude =33.356111111111112, timezone = 0;
        dayBefore = calculateSunEquatorialCoordinates(jd - 1, ΔT);
        dayOfInterest = calculateSunEquatorialCoordinates(jd, ΔT);
        dayAfter = calculateSunEquatorialCoordinates(jd + 1, ΔT);

        ν = calculateGreenwichSiderealTime(jd, ΔT);
        mRts[0] = approxSunTransitTime(dayOfInterest.α, longitude, ν);
        h0 = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, h0Prime);
        double[] α = {dayBefore.α, dayOfInterest.α, dayAfter.α};
        double[] δ = {dayBefore.δ, dayOfInterest.δ, dayAfter.δ};
        double suntransit, sunrise, sunset;

        if (h0 >= 0) {
            approxSunRiseAndSet(mRts, h0);
            for (int i = 0; i < 3; i++) {
                νRts[i] = ν + 360.985647 * mRts[i];
                n = mRts[i] + ΔT / 86400.0;
                αPrime[i] = rtsAlphaDeltaPrime(α, n);
                δPrime[i] = rtsAlphaDeltaPrime(δ, n);
                hPrime[i] = limitDegrees180pm(νRts[i] + longitude - αPrime[i]);
                hRts[i] = rtsSunAltitude(latitude, δPrime[i], hPrime[i]);
            }
            suntransit = dayFracToLocalHour(mRts[0] - hPrime[0] / 360.0, timezone);
            sunrise = dayFracToLocalHour(sunRiseAndSet(mRts, hRts, δPrime, latitude, hPrime, h0Prime, 1), timezone);
            sunset = dayFracToLocalHour(sunRiseAndSet(mRts, hRts, δPrime, latitude, hPrime, h0Prime, 2), timezone);
            spa[0] = suntransit;
            spa[1] = sunrise;
            spa[2] = sunset;
    

        }



    }
    
    
    public double[] calculateSunRiseTransitSet(double jd, double latitude, double longitude, double  timezone,  double ΔT) {

        double[] mRts, hRts, νRts, αPrime, δPrime, hPrime, spa;
        mRts = new double[3];
        hRts = new double[3];
        νRts = new double[3];
        αPrime = new double[3];
        δPrime = new double[3];
        hPrime = new double[3];
        spa = new double[3];
        double atmosRefract = 0.5667;
        double h0Prime = -1 * (SUNRADIUS + atmosRefract);
        double ν, h0, n;
        Equatorial dayBefore, dayOfInterest, dayAfter;
        jd = Math.floor(jd + 0.5) - 0.5;
        //double longitude = 32.85, latitude = 39.95, timezone = 2;//ANKARA position
        //double longitude =-116.8625, latitude =33.356111111111112, timezone = 0;
        dayBefore = calculateSunEquatorialCoordinates(jd - 1, ΔT);
        dayOfInterest = calculateSunEquatorialCoordinates(jd, ΔT);
        dayAfter = calculateSunEquatorialCoordinates(jd + 1, ΔT);

        ν = calculateGreenwichSiderealTime(jd, ΔT);
        mRts[0] = approxSunTransitTime(dayOfInterest.α, longitude, ν);
        h0 = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, h0Prime);
        double[] α = {dayBefore.α, dayOfInterest.α, dayAfter.α};
        double[] δ = {dayBefore.δ, dayOfInterest.δ, dayAfter.δ};
        double suntransit, sunrise, sunset;

        if (h0 >= 0) {
            approxSunRiseAndSet(mRts, h0);
            for (int i = 0; i < 3; i++) {
                νRts[i] = ν + 360.985647 * mRts[i];
                n = mRts[i] + ΔT / 86400.0;
                αPrime[i] = rtsAlphaDeltaPrime(α, n);
                δPrime[i] = rtsAlphaDeltaPrime(δ, n);
                hPrime[i] = limitDegrees180pm(νRts[i] + longitude - αPrime[i]);
                hRts[i] = rtsSunAltitude(latitude, δPrime[i], hPrime[i]);
            }
            suntransit = dayFracToLocalHour(mRts[0] - hPrime[0] / 360.0, timezone);
            sunrise = dayFracToLocalHour(sunRiseAndSet(mRts, hRts, δPrime, latitude, hPrime, h0Prime, 1), timezone);
            sunset = dayFracToLocalHour(sunRiseAndSet(mRts, hRts, δPrime, latitude, hPrime, h0Prime, 2), timezone);
            spa[0] = suntransit;
            spa[1] = sunrise;
            spa[2] = sunset;
    

        }
        return spa;


    }

   public double[] calculateSalatTimes(double jd, double latitude, double longitude, double timezone, int temperature, int pressure, int altitude, double fajrAngle, double ishaAngle) {
        double[] mRts, hRts, νRts, αPrime, δPrime, HPrime, salatTimes, H0;
        mRts = new double[SUN_COUNT];
        hRts = new double[SUN_COUNT];
        νRts = new double[SUN_COUNT];
        H0 = new double[SUN_COUNT];
        αPrime = new double[SUN_COUNT];
        δPrime = new double[SUN_COUNT];
        salatTimes = new double[SUN_COUNT];
        HPrime = new double[SUN_COUNT];//Calculate the local hour angle for the sun transit, sunrise, and sunset, H’i (in degrees),
        //double atmosRefract = 0.5667;
        double h0Prime = -SUNRADIUS - AstroLib.getApparentAtmosphericRefraction(0) * AstroLib.getWeatherCorrectionCoefficent(temperature, pressure) - AstroLib.getAltitudeCorrection(altitude);
        double ν, n, ΔT;
        Equatorial dayBefore, dayOfInterest, dayAfter;
        jd = Math.floor(jd + 0.5) - 0.5;
        ΔT = AstroLib.calculateTimeDifference(jd);
        dayBefore = calculateSunEquatorialCoordinates(jd - 1, ΔT);
        dayOfInterest = calculateSunEquatorialCoordinates(jd, ΔT);
        dayAfter = calculateSunEquatorialCoordinates(jd + 1, ΔT);
        //ASR ANGLE CALCULATION******************************///
        double δnoon = (dayOfInterest.δ + dayAfter.δ) / 2.0; //Arithmetic average for sun declination for midday
        double zenithTransit = Math.toRadians((latitude - δnoon));
        double asrShafiAngle = Math.toDegrees(MATH.atan(1 / (1 + Math.tan(zenithTransit))));
        double asrHanafiAngle = Math.toDegrees(MATH.atan(1 / (2 + Math.tan(zenithTransit))));
        //**************************************************///
        ν = calculateGreenwichSiderealTime(jd, ΔT);
        mRts[SUNTRANSIT] = approxSunTransitTime(dayOfInterest.α, longitude, ν);
        //Calculate the local hour angle corresponding to the sun elevation equals 0.8333/,H0
        H0[SUNTRANSIT] = 0;
        H0[SUNRISE] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, h0Prime);
        H0[SUNSET] = H0[SUNRISE];
        H0[ASR_SHAFI] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, asrShafiAngle);
        H0[ASR_HANEFI] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, asrHanafiAngle);
        H0[FAJR] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, fajrAngle);
        H0[ISHA] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, ishaAngle);
        double[] α = {dayBefore.α, dayOfInterest.α, dayAfter.α};
        double[] δ = {dayBefore.δ, dayOfInterest.δ, dayAfter.δ};


        approxSalatTimes(mRts, H0);
        for (int i = 0; i < SUN_COUNT; i++) {
            if (H0[i] >= 0) {
                νRts[i] = ν + 360.985647 * mRts[i];
                n = mRts[i] + ΔT / 86400.0;
                αPrime[i] = Interpolate(n, α);
                δPrime[i] = Interpolate(n, δ);
                HPrime[i] = limitDegrees180pm(νRts[i] + longitude - αPrime[i]);
                hRts[i] = rtsSunAltitude(latitude, δPrime[i], HPrime[i]);
            }
        }

        if (H0[SUNTRANSIT] >= 0) {
            salatTimes[SUNTRANSIT] = dayFracToLocalHour(mRts[SUNTRANSIT] - HPrime[SUNTRANSIT] / 360.0, timezone);
        }
        if (H0[SUNRISE] >= 0) {
            salatTimes[SUNRISE] = dayFracToLocalHour(sunRiseAndSet(mRts, hRts, δPrime, latitude, HPrime, h0Prime, SUNRISE), timezone);
        }
        if (H0[SUNSET] >= 0) {
            salatTimes[SUNSET] = dayFracToLocalHour(sunRiseAndSet(mRts, hRts, δPrime, latitude, HPrime, h0Prime, SUNSET), timezone);
        }
        if (H0[ASR_SHAFI] >= 0) {
            salatTimes[ASR_SHAFI] = dayFracToLocalHour(sunRiseAndSet(mRts, hRts, δPrime, latitude, HPrime, asrShafiAngle, ASR_SHAFI), timezone);
        }
        if (H0[ASR_HANEFI] >= 0) {
            salatTimes[ASR_HANEFI] = dayFracToLocalHour(sunRiseAndSet(mRts, hRts, δPrime, latitude, HPrime, asrHanafiAngle, ASR_HANEFI), timezone);
        }
        if (H0[FAJR] >= 0) {
            salatTimes[FAJR] = dayFracToLocalHour(sunRiseAndSet(mRts, hRts, δPrime, latitude, HPrime, fajrAngle, FAJR), timezone);
        }
        if (H0[ISHA] >= 0) {
            salatTimes[ISHA] = dayFracToLocalHour(sunRiseAndSet(mRts, hRts, δPrime, latitude, HPrime, ishaAngle, ISHA), timezone);
        }



        return salatTimes;

    }

   public double[] calculateKerahetTimes(double jd, double latitude, double longitude, double timezone, int temperature, int pressure, int altitude, double fajrAngle, double israkIsfirarAngle) {     //final int FAJR_=0,ISRAK=1,SUNTRANSIT_=2,ASRHANEFI=3,ISFIRAR=4,SUNSET_=5,KERAHAT_COUNT=6,DUHA=7,ISTIVA=8;
        double[] mRts, hRts, νRts, αPrime, δPrime, HPrime, kerahatTimes, H0;
        mRts = new double[KERAHAT_COUNT];
        hRts = new double[KERAHAT_COUNT];
        νRts = new double[KERAHAT_COUNT];
        H0 = new double[KERAHAT_COUNT];
        αPrime = new double[KERAHAT_COUNT];
        δPrime = new double[KERAHAT_COUNT];
        kerahatTimes = new double[KERAHAT_COUNT + 3];
        HPrime = new double[KERAHAT_COUNT];//Calculate the local hour angle for the sun transit, sunrise, and sunset, H’i (in degrees),
        //double atmosRefract = 0.5667;
        double h0Prime = -SUNRADIUS - AstroLib.getApparentAtmosphericRefraction(0) * AstroLib.getWeatherCorrectionCoefficent(temperature, pressure) - AstroLib.getAltitudeCorrection(altitude);
        double ν, n, ΔT;
        Equatorial dayBefore, dayOfInterest, dayAfter;
        jd = Math.floor(jd + 0.5) - 0.5;
        ΔT = AstroLib.calculateTimeDifference(jd);
        dayBefore = calculateSunEquatorialCoordinates(jd - 1, ΔT);
        dayOfInterest = calculateSunEquatorialCoordinates(jd, ΔT);
        dayAfter = calculateSunEquatorialCoordinates(jd + 1, ΔT);
        //ASR ANGLE CALCULATION----------------------------------///
        double δnoon = (dayOfInterest.δ + dayAfter.δ) / 2.0; //Arithmetic average for sun declination for midday
        double zenithTransit = Math.toRadians((latitude - δnoon));
        double asrHanafiAngle = Math.toDegrees(MATH.atan(1 / (2 + Math.tan(zenithTransit))));
        //-----------------------------------------------------///
        ν = calculateGreenwichSiderealTime(jd, ΔT);
        mRts[SUNTRANSIT_] = approxSunTransitTime(dayOfInterest.α, longitude, ν);
        //Calculate the local hour angle corresponding to the sun elevation equals 0.8333/,H0
        H0[SUNTRANSIT_] = 0;
        H0[SUNSET] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, h0Prime);
        H0[ISRAK] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, AstroLib.getApparentAtmosphericRefraction(israkIsfirarAngle));
        H0[ISFIRAR] = H0[ISRAK];
        H0[ASRHANEFI] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, asrHanafiAngle);
        H0[FAJR_] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, fajrAngle);
        //H0[ISHA]= getHourAngleAtRiseSet(latitude,dayOfInterest.δ,ishaAngle);
        double[] α = {dayBefore.α, dayOfInterest.α, dayAfter.α};
        double[] δ = {dayBefore.δ, dayOfInterest.δ, dayAfter.δ};
        approxKerahatTimes(mRts, H0);
        for (int i = 0; i < KERAHAT_COUNT; i++) {
            if (H0[i] >= 0) {
                νRts[i] = ν + 360.985647 * mRts[i];
                n = mRts[i] + ΔT / 86400.0;
                αPrime[i] = rtsAlphaDeltaPrime(α, n);
                δPrime[i] = rtsAlphaDeltaPrime(δ, n);
                HPrime[i] = limitDegrees180pm(νRts[i] + longitude - αPrime[i]);
                hRts[i] = rtsSunAltitude(latitude, δPrime[i], HPrime[i]);
            }
            if (H0[SUNTRANSIT_] >= 0) {
                kerahatTimes[SUNTRANSIT_] = dayFracToLocalHour(mRts[SUNTRANSIT_] - HPrime[SUNTRANSIT_] / 360.0, timezone);
            }
            if (H0[ISRAK] >= 0) {
                kerahatTimes[ISRAK] = dayFracToLocalHour(sunRiseAndSet(mRts, hRts, δPrime, latitude, HPrime, israkIsfirarAngle, ISRAK), timezone);
            }
            if (H0[SUNSET_] >= 0) {
                kerahatTimes[SUNSET_] = dayFracToLocalHour(sunRiseAndSet(mRts, hRts, δPrime, latitude, HPrime, h0Prime, SUNSET_), timezone);
            }
            if (H0[ASRHANEFI] >= 0) {
                kerahatTimes[ASRHANEFI] = dayFracToLocalHour(sunRiseAndSet(mRts, hRts, δPrime, latitude, HPrime, asrHanafiAngle, ASRHANEFI), timezone);
            }
            if (H0[ISFIRAR] >= 0) {
                kerahatTimes[ISFIRAR] = dayFracToLocalHour(sunRiseAndSet(mRts, hRts, δPrime, latitude, HPrime, israkIsfirarAngle, ISFIRAR), timezone);
            }
            if (H0[FAJR_] >= 0) {
                kerahatTimes[FAJR_] = dayFracToLocalHour(sunRiseAndSet(mRts, hRts, δPrime, latitude, HPrime, fajrAngle, FAJR_), timezone);
            }
                kerahatTimes[DUHA] = (3 * kerahatTimes[FAJR_] + kerahatTimes[SUNSET]) / 4;      
                kerahatTimes[ISTIVA] = (kerahatTimes[SUNSET] + kerahatTimes[FAJR_]) / 2;
          
        }
        return kerahatTimes;
    }
   public  Equatorial calculateSunEquatorialCoordinates(double jd, double ΔT) {
        double jce, jme, jde, Δψ, ε, r, l, β, theta, Δτ, λ, b, Δε, ε0;
        double α, δ;
        double[] x = new double[5];
        //jc=getJulianCentury(jd);
        jde = AstroLib.getJulianEphemerisDay(jd, ΔT);
        jce = AstroLib.getJulianEphemerisCentury(jde);
        jme = AstroLib.getJulianEphemerisMillennium(jce);
        // jde=getJulianEphemerisDay(jd,ΔT);
        x[0] = meanElongationMoonSun(jce);
        x[1] = meanAnomalySun(jce);
        x[2] = meanAnomalyMoon(jce);
        x[3] = argumentLatitudeMoon(jce);
        x[4] = ascendingLongitudeMoon(jce);
        ε0 = eclipticMeanObliquity(jme);
        Δε = nutationObliquity(jce, x);
        Δψ = nutationLongitude(jce, x);
        ε = eclipticTrueObliquity(Δε, ε0);
        r = earthRadiusVector(jme);
        l = earthHeliocentricLongitude(jme);
        theta = geocentricLongitude(l);
        Δτ = aberrationCorrection(r);
        λ = apparentSunLongitude(theta, Δψ, Δτ);
        // ν0= greenwichMeanSiderealTime (jd,jc);
        //ν= greenwichSiderealTime (ν0,Δψ,ε);
        b = earthHeliocentricLatitude(jme);
        β = getGeocentricLatitude(b);
        α = geocentricRightAscension(λ, ε, β);
        δ = geocentricDeclination(λ, ε, β);
        double Δ = 149597887.5;
        return new Equatorial(α, δ, Δ);
        //m=sunMeanLongitude(jme);
        //eot=calculateEquationOfTime(m,α,Δψ,ε);

    }

    Equatorial calculateSunEquatorialCoordinates(Ecliptic sunPosEc, double jd, double ΔT) {
        double jce, jme, jde, ε, Δε, ε0;
        double α, δ;
        double[] x = new double[5];

        jde = AstroLib.getJulianEphemerisDay(jd, ΔT);
        jce = AstroLib.getJulianEphemerisCentury(jde);
        jme = AstroLib.getJulianEphemerisMillennium(jce);

        x[0] = meanElongationMoonSun(jce);
        x[1] = meanAnomalySun(jce);
        x[2] = meanAnomalyMoon(jce);
        x[3] = argumentLatitudeMoon(jce);
        x[4] = ascendingLongitudeMoon(jce);
        ε0 = eclipticMeanObliquity(jme);
        Δε = nutationObliquity(jce, x);
        ε = eclipticTrueObliquity(Δε, ε0);

        α = geocentricRightAscension(sunPosEc.λ, ε, sunPosEc.β);
        δ = geocentricDeclination(sunPosEc.λ, ε, sunPosEc.β);
        double Δ = 149597887.5;
        return new Equatorial(α, δ, Δ);


    }

    public Ecliptic calculateSunEclipticCoordinatesAstronomic(double jd, double ΔT) {
        double jce, jme, jde, l, β, theta, b;
        jde = AstroLib.getJulianEphemerisDay(jd, ΔT);
        jce = AstroLib.getJulianEphemerisCentury(jde);
        jme = AstroLib.getJulianEphemerisMillennium(jce);
        l = earthHeliocentricLongitude(jme);
        theta = geocentricLongitude(l);
        b = earthHeliocentricLatitude(jme);
        β = getGeocentricLatitude(b);
        return new Ecliptic(theta, β);

    }

    ///////////////////////////////////////////////
    static double calculateGreenwichSiderealTime(double jd, double ΔT) {
        double jce, jme, jde, Δψ, ε, ν0, Δε, ε0;
        double ν;
        double[] x = new double[5];
        jde = AstroLib.getJulianEphemerisDay(jd, ΔT);
        jce = AstroLib.getJulianEphemerisCentury(jde);
        jme = AstroLib.getJulianEphemerisMillennium(jce);
        x[0] = meanElongationMoonSun(jce);
        x[1] = meanAnomalySun(jce);
        x[2] = meanAnomalyMoon(jce);
        x[3] = argumentLatitudeMoon(jce);
        x[4] = ascendingLongitudeMoon(jce);
        ε0 = eclipticMeanObliquity(jme);//
        Δε = nutationObliquity(jce, x);//
        Δψ = nutationLongitude(jce, x);//
        ε = eclipticTrueObliquity(Δε, ε0);//
        ν0 = greenwichMeanSiderealTime(jd);
        ν = greenwichSiderealTime(ν0, Δψ, ε);
        return ν;
    }


    /*   void calculateNutationAndObliquity(double jd,double ΔT, double[] Δψ, double[] ε)
    {
    double jc,jce,jme,jde,Δε,ε0;
    double ν;
    double[] x=new double[5];
    //jc=AstroLib.getJulianCentury(jd);
    jde=AstroLib.getJulianEphemerisDay(jd,ΔT);
    jce=AstroLib.getJulianEphemerisCentury(jde);
    jme=AstroLib.getJulianEphemerisMillennium(jce);
    x[0] = meanElongationMoonSun(jce);
    x[1] = meanAnomalySun(jce);
    x[2] = meanAnomalyMoon(jce);
    x[3] = argumentLatitudeMoon(jce);
    x[4] = ascendingLongitudeMoon(jce);
    ε0=eclipticMeanObliquity(jme);//
    Δε=nutationObliquity(jce, x);//
    Δψ[0]=nutationLongitude(jce,x);//
    ε[0]=eclipticTrueObliquity(Δε, ε0);//

    }*/
    static double[] calculateXArray(double jd, double ΔT) {
        double jce, jde;
        double[] x = new double[5];
        jde = AstroLib.getJulianEphemerisDay(jd, ΔT);
        jce = AstroLib.getJulianEphemerisCentury(jde);
        x[0] = meanElongationMoonSun(jce);
        x[1] = meanAnomalySun(jce);
        x[2] = meanAnomalyMoon(jce);
        x[3] = argumentLatitudeMoon(jce);
        x[4] = ascendingLongitudeMoon(jce);
        return x;
    }

    double calculateEclipticTrueObliquity(double jd, double ΔT) {
        double jce, jme, jde, ε, Δε, ε0;

        double[] x = new double[5];
        jde = AstroLib.getJulianEphemerisDay(jd, ΔT);
        jce = AstroLib.getJulianEphemerisCentury(jde);
        jme = AstroLib.getJulianEphemerisMillennium(jce);
        x[0] = meanElongationMoonSun(jce);
        x[1] = meanAnomalySun(jce);
        x[2] = meanAnomalyMoon(jce);
        x[3] = argumentLatitudeMoon(jce);
        x[4] = ascendingLongitudeMoon(jce);
        ε0 = eclipticMeanObliquity(jme);//
        Δε = nutationObliquity(jce, x);//
        ε = eclipticTrueObliquity(Δε, ε0);//
        return ε;
    }

    /**
     * TA'DIL-I ZAMAN
     * Tadil-i zaman degiskenini doner.
     * Gunesin saat 12 de iken tepe noktasinda olmasi gerekirken olamayipda  aradaki farki dakika cinsinden
     * veren fonksiyon.
     * Calculate the difference between true solar time and mean. The "equation
     * of time" is a term accounting for changes in the time of solar noon for
     * a given location over the course of a year. Earth's elliptical orbit and
     * Kepler's law of equal areas in equal times are the culprits behind this
     * phenomenon. See the
     * <A HREF="http://www.analemma.com/Pages/framesPage.html">Analemma page</A>.
     * Below is a plot of the equation of time versus the day of the year.
     *
     * <P align="center"><img src="doc-files/EquationOfTime.png"></P>
     *
     * @param  t νmber of Julian centuries since J2000.
     * @return Equation of time in minutes of time.
     * TA'DIL-I ZEMAN
     */
    double calculateEquationOfTime(double M, double α, double Δψ, double ε) {
        return limitMinutes(4.0 * (M - 0.0057183 - α + Δψ * Math.cos(Math.toRadians(ε))));
    }
}


