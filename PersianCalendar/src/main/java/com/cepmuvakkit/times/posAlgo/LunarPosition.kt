package com.cepmuvakkit.times.posAlgo

import com.cepmuvakkit.times.posAlgo.AstroLib.calculateTimeDifference
import com.cepmuvakkit.times.posAlgo.AstroLib.fourthOrderPolynomial
import com.cepmuvakkit.times.posAlgo.AstroLib.getAltitudeCorrection
import com.cepmuvakkit.times.posAlgo.AstroLib.getApparentAtmosphericRefraction
import com.cepmuvakkit.times.posAlgo.AstroLib.getJulianEphemerisCentury
import com.cepmuvakkit.times.posAlgo.AstroLib.getJulianEphemerisDay
import com.cepmuvakkit.times.posAlgo.AstroLib.getJulianEphemerisMillennium
import com.cepmuvakkit.times.posAlgo.AstroLib.getStringHHMMSSS
import com.cepmuvakkit.times.posAlgo.AstroLib.getWeatherCorrectionCoefficent
import com.cepmuvakkit.times.posAlgo.AstroLib.limitDegrees
import com.cepmuvakkit.times.posAlgo.AstroLib.thirdOrderPolynomial
import com.cepmuvakkit.times.posAlgo.MATH.asin
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * @author mehmetrg
 */
object LunarPosition {
    // static final int ΣlΣrTerms[][]={      { 0,0,1,0,6288774,-20905335},{2,0,-1,0,1274027,-3699111},{2,0,0,0,658314,-2955968},{0,0,2,0,213618,-569925},{0,1,0,0,-185116,48888},{0,0,0,2,-114332,-3149},{2,0,-2,0,58793,246158},{2,-1,-1,0,57066,-152138},{2,0,1,0,53322,-170733},{2,-1,0,0,45758,-204586},{0,1,-1,0,-40923,-129620},{1,0,0,0,-34720,108743},{0,1,1,0,-30383,104755},{2,0,0,-2,15327,10321},{0,0,1,2,-12528,0},{0,0,1,-2,10980,79661},{4,0,-1,0,10675,-34782},{0,0,3,0,10034,-23210},{4,0,-2,0,8548,-21636},{2,1,-1,0,-7888,24208},{2,1,0,0,-6766,30824},{1,0,-1,0,-5163,-8379},{1,1,0,0,4987,-16675},{2,-1,1,0,4036,-12831},{2,0,2,0,3994,-10445},{4,0,0,0,3861,-11650},{2,0,-3,0,3665,14403},{0,1,-2,0,-2689,-7003},{2,0,-1,2,-2602,0},{2,-1,-2,0,2390,10056},{1,0,1,0,-2348,6322},{2,-2,0,0,2236,-9884},{0,1,2,0,-2120,5751},{0,2,0,0,-2069,0},{2,-2,-1,0,2048,-4950},{2,0,1,-2,-1773,4130},{2,0,0,2,-1595,0},{4,-1,-1,0,1215,-3958},{0,0,2,2,-1110,0},{3,0,-1,0,-892,3258},{2,1,1,0,-810,2616},{4,-1,-2,0,759,-1897},{0,2,-1,0,-713,-2117},{2,2,-1,0,-700,2354},{2,1,-2,0,691,0},{2,-1,0,-2,596,0},{4,0,1,0,549,-1423},{0,0,4,0,537,-1117},{4,-1,0,0,520,-1571},{1,0,-2,0,-487,-1739},{2,1,0,-2,-399,0},{0,0,2,-2,-381,-4421},{1,1,1,0,351,0},{3,0,-2,0,-340,0},{4,0,-3,0,330,0},{2,-1,2,0,327,0},{0,2,1,0,-323,1165},{1,1,-1,0,299,0},{2,0,3,0,294,0},{2,0,-1,-2,0,8752 }    };
    private val argCoefforΣlΣr = listOf(
        byteArrayOf(0, 0, 1, 0),
        byteArrayOf(2, 0, -1, 0),
        byteArrayOf(2, 0, 0, 0),
        byteArrayOf(0, 0, 2, 0),
        byteArrayOf(0, 1, 0, 0),
        byteArrayOf(0, 0, 0, 2),
        byteArrayOf(2, 0, -2, 0),
        byteArrayOf(2, -1, -1, 0),
        byteArrayOf(2, 0, 1, 0),
        byteArrayOf(2, -1, 0, 0),
        byteArrayOf(0, 1, -1, 0),
        byteArrayOf(1, 0, 0, 0),
        byteArrayOf(0, 1, 1, 0),
        byteArrayOf(2, 0, 0, -2),
        byteArrayOf(0, 0, 1, 2),
        byteArrayOf(0, 0, 1, -2),
        byteArrayOf(4, 0, -1, 0),
        byteArrayOf(0, 0, 3, 0),
        byteArrayOf(4, 0, -2, 0),
        byteArrayOf(2, 1, -1, 0),
        byteArrayOf(2, 1, 0, 0),
        byteArrayOf(1, 0, -1, 0),
        byteArrayOf(1, 1, 0, 0),
        byteArrayOf(2, -1, 1, 0),
        byteArrayOf(2, 0, 2, 0),
        byteArrayOf(4, 0, 0, 0),
        byteArrayOf(2, 0, -3, 0),
        byteArrayOf(0, 1, -2, 0),
        byteArrayOf(2, 0, -1, 2),
        byteArrayOf(2, -1, -2, 0),
        byteArrayOf(1, 0, 1, 0),
        byteArrayOf(2, -2, 0, 0),
        byteArrayOf(0, 1, 2, 0),
        byteArrayOf(0, 2, 0, 0),
        byteArrayOf(2, -2, -1, 0),
        byteArrayOf(2, 0, 1, -2),
        byteArrayOf(2, 0, 0, 2),
        byteArrayOf(4, -1, -1, 0),
        byteArrayOf(0, 0, 2, 2),
        byteArrayOf(3, 0, -1, 0),
        byteArrayOf(2, 1, 1, 0),
        byteArrayOf(4, -1, -2, 0),
        byteArrayOf(0, 2, -1, 0),
        byteArrayOf(2, 2, -1, 0),
        byteArrayOf(2, 1, -2, 0),
        byteArrayOf(2, -1, 0, -2),
        byteArrayOf(4, 0, 1, 0),
        byteArrayOf(0, 0, 4, 0),
        byteArrayOf(4, -1, 0, 0),
        byteArrayOf(1, 0, -2, 0),
        byteArrayOf(2, 1, 0, -2),
        byteArrayOf(0, 0, 2, -2),
        byteArrayOf(1, 1, 1, 0),
        byteArrayOf(3, 0, -2, 0),
        byteArrayOf(4, 0, -3, 0),
        byteArrayOf(2, -1, 2, 0),
        byteArrayOf(0, 2, 1, 0),
        byteArrayOf(1, 1, -1, 0),
        byteArrayOf(2, 0, 3, 0),
        byteArrayOf(2, 0, -1, -2)
    )
    private val coefSinCosΣlΣr = listOf(
        intArrayOf(6288774, -20905355),
        intArrayOf(1274027, -3699111),
        intArrayOf(658314, -2955968),
        intArrayOf(213618, -569925),
        intArrayOf(-185116, 48888),
        intArrayOf(-114332, -3149),
        intArrayOf(58793, 246158),
        intArrayOf(57066, -152138),
        intArrayOf(53322, -170733),
        intArrayOf(45758, -204586),
        intArrayOf(-40923, -129620),
        intArrayOf(-34720, 108743),
        intArrayOf(-30383, 104755),
        intArrayOf(15327, 10321),
        intArrayOf(-12528, 0),
        intArrayOf(10980, 79661),
        intArrayOf(10675, -34782),
        intArrayOf(10034, -23210),
        intArrayOf(8548, -21636),
        intArrayOf(-7888, 24208),
        intArrayOf(-6766, 30824),
        intArrayOf(-5163, -8379),
        intArrayOf(4987, -16675),
        intArrayOf(4036, -12831),
        intArrayOf(3994, -10445),
        intArrayOf(3861, -11650),
        intArrayOf(3665, 14403),
        intArrayOf(-2689, -7003),
        intArrayOf(-2602, 0),
        intArrayOf(2390, 10056),
        intArrayOf(-2348, 6322),
        intArrayOf(2236, -9884),
        intArrayOf(-2120, 5751),
        intArrayOf(-2069, 0),
        intArrayOf(2048, -4950),
        intArrayOf(-1773, 4130),
        intArrayOf(-1595, 0),
        intArrayOf(1215, -3958),
        intArrayOf(-1110, 0),
        intArrayOf(-892, 3258),
        intArrayOf(-810, 2616),
        intArrayOf(759, -1897),
        intArrayOf(-713, -2117),
        intArrayOf(-700, 2354),
        intArrayOf(691, 0),
        intArrayOf(596, 0),
        intArrayOf(549, -1423),
        intArrayOf(537, -1117),
        intArrayOf(520, -1571),
        intArrayOf(-487, -1739),
        intArrayOf(-399, 0),
        intArrayOf(-381, -4421),
        intArrayOf(351, 0),
        intArrayOf(-340, 0),
        intArrayOf(330, 0),
        intArrayOf(327, 0),
        intArrayOf(-323, 1165),
        intArrayOf(299, 0),
        intArrayOf(294, 0),
        intArrayOf(0, 8752)
    )

    //static final int ΣbTerms[][]={{ 0,0,0,1,5128122},{0,0,1,1,280602},{0,0,1,-1,277693},{2,0,0,-1,173237},{2,0,-1,1,55413},{2,0,-1,-1,46271},{2,0,0,1,32573},{0,0,2,1,17198},{2,0,1,-1,9266},{0,0,2,-1,8822},{2,-1,0,-1,8216},{2,0,-2,-1,4324},{2,0,1,1,4200},{2,1,0,-1,-3359},{2,-1,-1,1,2463},{2,-1,0,1,2211},{2,-1,-1,-1,2065},{0,1,-1,-1,-1870},{4,0,-1,-1,1828},{0,1,0,1,-1794},{0,0,0,3,-1749},{0,1,-1,1,-1565},{1,0,0,1,-1491},{0,1,1,1,-1475},{0,1,1,-1,-1410},{0,1,0,-1,-1344},{1,0,0,-1,-1335},{0,0,3,1,1107},{4,0,0,-1,1021},{4,0,-1,1,833},{0,0,1,-3,777},{4,0,-2,1,671},{2,0,0,-3,607},{2,0,2,-1,596},{2,-1,1,-1,491},{2,0,-2,1,-451},{0,0,3,-1,439},{2,0,2,1,422},{2,0,-3,-1,421},{2,1,-1,1,-366},{2,1,0,1,-351},{4,0,0,1,331},{2,-1,1,1,315},{2,-2,0,-1,302},{0,0,1,3,-283},{2,1,1,-1,-229},{1,1,0,-1,223},{1,1,0,1,223},{0,1,-2,-1,-220},{2,1,-1,-1,-220},{1,0,1,1,-185},{2,-1,-2,-1,181},{0,1,2,1,-177},{4,0,-2,-1,176},{4,-1,-1,-1,166},{1,0,1,-1,-164},{4,0,1,-1,132},{1,0,-1,-1,-119},{4,-1,0,-1,115},{2,-2,0,1,107 }    };
    private val argCoefforΣb = listOf(
        byteArrayOf(0, 0, 0, 1),
        byteArrayOf(0, 0, 1, 1),
        byteArrayOf(0, 0, 1, -1),
        byteArrayOf(2, 0, 0, -1),
        byteArrayOf(2, 0, -1, 1),
        byteArrayOf(2, 0, -1, -1),
        byteArrayOf(2, 0, 0, 1),
        byteArrayOf(0, 0, 2, 1),
        byteArrayOf(2, 0, 1, -1),
        byteArrayOf(0, 0, 2, -1),
        byteArrayOf(2, -1, 0, -1),
        byteArrayOf(2, 0, -2, -1),
        byteArrayOf(2, 0, 1, 1),
        byteArrayOf(2, 1, 0, -1),
        byteArrayOf(2, -1, -1, 1),
        byteArrayOf(2, -1, 0, 1),
        byteArrayOf(2, -1, -1, -1),
        byteArrayOf(0, 1, -1, -1),
        byteArrayOf(4, 0, -1, -1),
        byteArrayOf(0, 1, 0, 1),
        byteArrayOf(0, 0, 0, 3),
        byteArrayOf(0, 1, -1, 1),
        byteArrayOf(1, 0, 0, 1),
        byteArrayOf(0, 1, 1, 1),
        byteArrayOf(0, 1, 1, -1),
        byteArrayOf(0, 1, 0, -1),
        byteArrayOf(1, 0, 0, -1),
        byteArrayOf(0, 0, 3, 1),
        byteArrayOf(4, 0, 0, -1),
        byteArrayOf(4, 0, -1, 1),
        byteArrayOf(0, 0, 1, -3),
        byteArrayOf(4, 0, -2, 1),
        byteArrayOf(2, 0, 0, -3),
        byteArrayOf(2, 0, 2, -1),
        byteArrayOf(2, -1, 1, -1),
        byteArrayOf(2, 0, -2, 1),
        byteArrayOf(0, 0, 3, -1),
        byteArrayOf(2, 0, 2, 1),
        byteArrayOf(2, 0, -3, -1),
        byteArrayOf(2, 1, -1, 1),
        byteArrayOf(2, 1, 0, 1),
        byteArrayOf(4, 0, 0, 1),
        byteArrayOf(2, -1, 1, 1),
        byteArrayOf(2, -2, 0, -1),
        byteArrayOf(0, 0, 1, 3),
        byteArrayOf(2, 1, 1, -1),
        byteArrayOf(1, 1, 0, -1),
        byteArrayOf(1, 1, 0, 1),
        byteArrayOf(0, 1, -2, -1),
        byteArrayOf(2, 1, -1, -1),
        byteArrayOf(1, 0, 1, 1),
        byteArrayOf(2, -1, -2, -1),
        byteArrayOf(0, 1, 2, 1),
        byteArrayOf(4, 0, -2, -1),
        byteArrayOf(4, -1, -1, -1),
        byteArrayOf(1, 0, 1, -1),
        byteArrayOf(4, 0, 1, -1),
        byteArrayOf(1, 0, -1, -1),
        byteArrayOf(4, -1, 0, -1),
        byteArrayOf(2, -2, 0, 1)
    )
    private val coefSinΣb = listOf(
        5128122,
        280602,
        277693,
        173237,
        55413,
        46271,
        32573,
        17198,
        9266,
        8822,
        8216,
        4324,
        4200,
        -3359,
        2463,
        2211,
        2065,
        -1870,
        1828,
        -1794,
        -1749,
        -1565,
        -1491,
        -1475,
        -1410,
        -1344,
        -1335,
        1107,
        1021,
        833,
        777,
        671,
        607,
        596,
        491,
        -451,
        439,
        422,
        421,
        -366,
        -351,
        331,
        315,
        302,
        -283,
        -229,
        223,
        223,
        -220,
        -220,
        -185,
        181,
        -177,
        176,
        166,
        -164,
        132,
        -119,
        115,
        107
    )
    private const val cD: Byte = 0
    private const val cM: Byte = 1
    private const val cMP: Byte = 2
    private const val cF: Byte = 3
    private const val cSin: Byte = 0
    private const val cCos: Byte = 1

    /**
     * Calculate the mean elongation of the moon from the sun,X0 (in degrees),
     *  D = 297.8501921 + 445267.1114034 * T - 0.0018819 * T2 + T3 / 545868.0 - T4 / 113065000.0;
     *
     * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return mean elongation of the moon from the sun,X0 (in degrees).
     */
    private fun meanElongationMoonSun(jce: Double): Double {
        return limitDegrees(
            fourthOrderPolynomial(
                1 / 113065000.0,
                1.0 / 545868.0,
                -0.0018819,
                445267.1114034,
                297.8501921,
                jce
            )
        )
    }

    /**
     * Calculate the mean longitude of the moon (in degrees),
     *    L1 = 218.3164477 + 481267.88123421 * T - 0.0015786 * T2  + T3 / 538841.0 - T4 / 65194000.0
     * Undoing the built-in 0.7" light-time adjustment
     * +0.0001944;
     *
     * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return mean Moon Longitude (in degrees).
     */
    private fun meanMoonLongitude(jce: Double): Double {
        return limitDegrees(
            fourthOrderPolynomial(
                1 / 65194000.0,
                1.0 / 538841.0,
                -0.0015786,
                481267.88123421,
                218.3164477 + 0.0001944,
                jce
            )
        )
        //return AstroLib.limitDegrees(AstroLib.fourthOrderPolynomial(1 / 65194000.0, 1.0 / 538841.0, -0.0015786, 481267.88123421, 218.3164477, jce));
    }

    /**
     * Calculate the mean anomaly of the sun (Earth),X1 (in degrees),
     * M = 357.5291092 + 35999.0502909 * T - 0.0001536 * T2 + T3 / 24490000.0;
     *
     * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return mean anomaly of the sun (Earth),X1 (in degrees).
     */
    private fun meanAnomalySun(jce: Double): Double {
        return limitDegrees(
            thirdOrderPolynomial(
                1.0 / 24490000.0,
                -0.0001536,
                35999.0502909,
                357.5291092,
                jce
            )
        )
    }

    /**
     * Calculate the mean anomaly of the moon,X2 (in degrees),
     * M1 = 134.9633964 + 477198.8675055 * T + 0.0087414 * T2 + T3 / 69699.0 - T4 / 14712000.0;
     *
     * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return mean anomaly of the moon,X2 (in degrees).
     */
    private fun meanAnomalyMoon(jce: Double): Double {
        return limitDegrees(
            fourthOrderPolynomial(
                1.0 / 14712000.0,
                1 / 69699.0,
                0.0087414,
                477198.8675055,
                134.9633964,
                jce
            )
        )
    }

    /**
     * Calculate the moon’s argument of latitude,X3 (in degrees),
     *  F = 93.2720950 + 483202.0175233 * T - 0.0036539 * T2- T3 / 3526000.0 + T4 / 863310000.0;
     *
     * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return the moon’s argument of latitude,X3 (in degrees).
     */
    private fun argumentLatitudeMoon(jce: Double): Double {
        return limitDegrees(
            fourthOrderPolynomial(
                1 / 863310000.0,
                1.0 / 3526000.0,
                -0.0036539,
                483202.0175233,
                93.2720950,
                jce
            )
        )
    }

    /**
     * Calculate the eccentrity of Earth orbit arround the sun ,
     * E = 1.0 - 0.002516 * T - 0.0000074 * T2;
     *
     * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return the eccentrity of Earth orbit arround the sun.
     */
    private fun eccentrityOfEarthOrbit(jce: Double): Double {
        return 1.0 - 0.002516 * jce - 0.0000074 * jce * jce
    }

    /**
     * Calculate effect of Venus to the Moon ,
     * A1 = 119.75 + 131.849 * T
     *
     * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return A1.
     */
    private fun effectVenus(jce: Double): Double {
        return limitDegrees(119.75 + 131.849 * jce)
    }

    /**
     * Calculate effect of Jupiter to the Moon ,
     * A2 = 53.09 + 479264.290 * T;
     *
     * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return A2.
     */
    private fun effectJupiter(jce: Double): Double {
        return limitDegrees(53.09 + 479264.290 * jce)
    }

    /**
     * Calculate effect of Jupiter to the Moon ,
     *  A3 = 313.45 + 481266.484 * T;
     *
     * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
     * @return A3.
     */
    private fun effectFlatting(jce: Double): Double {
        return limitDegrees(313.45 + 481266.484 * jce)
    }

    fun calculateMoonEclipticCoordinates(jd: Double, ΔT: Double): Ecliptic {
        val λ: Double
        val β: Double
        val Δ: Double //λ the ecliptic longitude,latitude and distance from earth in geocentric coordinates
        var Σl: Double
        var Σb: Double
        val Σr: Double
        val L1: Double
        val A1: Double
        val A2: Double
        val A3: Double
        val M: Double
        val F: Double
        val M1: Double
        val E: Double
        val D: Double

        //jc=AstroLib.getJulianCentury(jd);
        // double ΔT =AstroLib.calculateTimeDifference(jd);
        val jde: Double = getJulianEphemerisDay(jd, ΔT)
        val jce: Double = getJulianEphemerisCentury(jde)
        L1 = meanMoonLongitude(jce)
        D = meanElongationMoonSun(jce)
        M = meanAnomalySun(jce)
        M1 = meanAnomalyMoon(jce)
        F = argumentLatitudeMoon(jce)
        A1 = effectVenus(jce)
        A2 = effectJupiter(jce)
        A3 = effectFlatting(jce)
        E = eccentrityOfEarthOrbit(jce)
        val sum: DoubleArray = summationΣlΣr(L1, D, M, M1, F, E)
        Σl = sum[0]
        Σr = sum[1]
        Σb = summationΣb(L1, D, M, M1, F, E)
        Σl += 3958.0 * sin(Math.toRadians(A1)) + 1962.0 * sin(Math.toRadians(L1 - F)) + 318.0 * sin(
            Math.toRadians(A2)
        )
        Σb += -2235.0 * sin(Math.toRadians(L1)) + 382.0 * sin(Math.toRadians(A3)) + 175.0 * sin(
            Math.toRadians(A1 - F)
        ) + 175.0 * sin(Math.toRadians(A1 + F)) + 127.0 * sin(
            Math.toRadians(L1 - M1)
        ) - 115.0 * sin(Math.toRadians(L1 + M1))
        val x = SolarPosition.calculateXArray(jd, ΔT)
        val Δψ = SolarPosition.nutationLongitude(jce, x) //
        λ = L1 + Σl / 1000000.0 + Δψ //133.16265468515076//133.162849085666605652
        β = Σb / 1000000.0
        Δ = 385000.56 + Σr / 1000.0
        return Ecliptic(λ, β, Δ)
    }

    //Ecliptic coordinates without  earth nutation  factor
    fun calculateMoonEclipticCoordinatesAstronomic(jd: Double, ΔT: Double): Ecliptic {
        val λ: Double
        val β: Double
        var Σl: Double
        var Σb: Double
        val L1: Double
        val A1: Double
        val A2: Double
        val A3: Double
        val M: Double
        val F: Double
        val M1: Double
        val E: Double
        val D: Double
        val jde: Double = getJulianEphemerisDay(jd, ΔT)
        val jce: Double = getJulianEphemerisCentury(jde)
        L1 = meanMoonLongitude(jce)
        D = meanElongationMoonSun(jce)
        M = meanAnomalySun(jce)
        M1 = meanAnomalyMoon(jce)
        F = argumentLatitudeMoon(jce)
        A1 = effectVenus(jce)
        A2 = effectJupiter(jce)
        A3 = effectFlatting(jce)
        E = eccentrityOfEarthOrbit(jce)
        val sum: DoubleArray = summationΣlΣr(L1, D, M, M1, F, E)
        Σl = sum[0]
        Σb = summationΣb(L1, D, M, M1, F, E)
        Σl += 3958.0 * sin(Math.toRadians(A1)) + 1962.0 * sin(Math.toRadians(L1 - F)) + 318.0 * sin(
            Math.toRadians(A2)
        )
        Σb += -2235.0 * sin(Math.toRadians(L1)) + 382.0 * sin(Math.toRadians(A3)) + 175.0 * sin(
            Math.toRadians(A1 - F)
        ) + 175.0 * sin(Math.toRadians(A1 + F)) + 127.0 * sin(
            Math.toRadians(L1 - M1)
        ) - 115.0 * sin(Math.toRadians(L1 + M1))
        λ = L1 + Σl / 1000000.0
        β = Σb / 1000000.0
        return Ecliptic(λ, β)
    }

    fun calculateMoonEqutarialCoordinates(jd: Double, ΔT: Double): Equatorial {
        val α: Double
        val δ: Double
        val ε0: Double
        val Δε: Double
        val jce: Double
        val moonPos: Ecliptic = calculateMoonEclipticCoordinates(jd, ΔT)
        val x = SolarPosition.calculateXArray(jd, ΔT)
        val jde: Double = getJulianEphemerisDay(jd, ΔT)
        jce = getJulianEphemerisCentury(jde)
        val jme: Double = getJulianEphemerisMillennium(jce)
        ε0 = SolarPosition.eclipticMeanObliquity(jme) //
        Δε = SolarPosition.nutationObliquity(jce, x) //
        // Δψ=SolarPosition.nutationLongitude(jce,x);//
        val ε: Double = SolarPosition.eclipticTrueObliquity(Δε, ε0) //
        α = SolarPosition.geocentricRightAscension(moonPos.λ, ε, moonPos.β)
        δ = SolarPosition.geocentricDeclination(moonPos.λ, ε, moonPos.β)
        return Equatorial(α, δ, moonPos.Δ)
    }

    fun calculateMoonEqutarialCoordinates(moonPos: Ecliptic, jd: Double, ΔT: Double): Equatorial {
        val α: Double
        val δ: Double
        val ε0: Double
        val Δε: Double
        val jce: Double
        val x = SolarPosition.calculateXArray(jd, ΔT)
        val jde: Double = getJulianEphemerisDay(jd, ΔT)
        jce = getJulianEphemerisCentury(jde)
        val jme: Double = getJulianEphemerisMillennium(jce)
        ε0 = SolarPosition.eclipticMeanObliquity(jme) //
        Δε = SolarPosition.nutationObliquity(jce, x) //
        // Δψ=SolarPosition.nutationLongitude(jce,x);//
        val ε: Double = SolarPosition.eclipticTrueObliquity(Δε, ε0) //
        α = SolarPosition.geocentricRightAscension(moonPos.λ, ε, moonPos.β)
        δ = SolarPosition.geocentricDeclination(moonPos.λ, ε, moonPos.β)
        return Equatorial(α, δ, moonPos.Δ)
    }

    fun summationΣlΣr(
        L1: Double,
        D: Double,
        M: Double,
        M1: Double,
        F: Double,
        E: Double
    ): DoubleArray {
        var arg: Double
        val E2 = E * E
        var L: Double = 0.0
        var R: Double = 0.0
        for (i in argCoefforΣlΣr.indices) {
            arg =
                Math.toRadians(argCoefforΣlΣr[i][cD.toInt()] * D + argCoefforΣlΣr[i][cM.toInt()] * M + argCoefforΣlΣr[i][cMP.toInt()] * M1 + argCoefforΣlΣr[i][cF.toInt()] * F)
            if (argCoefforΣlΣr[i][cM.toInt()].toDouble() == -2.0 || argCoefforΣlΣr[i][cM.toInt()].toDouble() == 2.0) {
                L += coefSinCosΣlΣr[i][cSin.toInt()] * E2 * sin(arg)
                R += coefSinCosΣlΣr[i][cCos.toInt()] * E2 * cos(arg)
            } else if (argCoefforΣlΣr[i][cM.toInt()].toDouble() == -1.0 || argCoefforΣlΣr[i][cM.toInt()].toDouble() == 1.0) {
                L += coefSinCosΣlΣr[i][cSin.toInt()] * E * sin(arg)
                R += coefSinCosΣlΣr[i][cCos.toInt()] * E * cos(arg)
            } else {
                L += coefSinCosΣlΣr[i][cSin.toInt()] * sin(arg)
                R += coefSinCosΣlΣr[i][cCos.toInt()] * cos(arg)
            }
        }
        return doubleArrayOf(L, R)
    }

    fun summationΣb(L1: Double, D: Double, M: Double, M1: Double, F: Double, E: Double): Double {
        var arg: Double
        val E2 = E * E
        var Σb: Double = 0.0
        for (i in argCoefforΣb.indices) {
            arg =
                Math.toRadians(argCoefforΣb[i][cD.toInt()] * D + argCoefforΣb[i][cM.toInt()] * M + argCoefforΣb[i][cMP.toInt()] * M1 + argCoefforΣb[i][cF.toInt()] * F)
            Σb += if (argCoefforΣb[i][cM.toInt()].toDouble() == -2.0 || argCoefforΣb[i][cM.toInt()].toDouble() == 2.0) {
                coefSinΣb[i] * E2 * sin(arg)
            } else if (argCoefforΣb[i][cM.toInt()].toDouble() == -1.0 || argCoefforΣb[i][cM.toInt()].toDouble() == 1.0) {
                coefSinΣb[i] * E * sin(arg)
            } else {
                coefSinΣb[i] * sin(arg)
            }
        }
        return Σb
    }

    fun calculateMoonRiseTransitSetStr(
        jd: Double,
        latitude: Double,
        longitude: Double,
        timezone: Double,
        temperature: Int,
        pressure: Int,
        altitude: Int
    ) {
        var jd = jd
        val m_trs = DoubleArray(3)
        val h_rts = DoubleArray(3)
        val νRts = DoubleArray(3)
        val αPrime = DoubleArray(3)
        val δPrime = DoubleArray(3)
        val HPrime = DoubleArray(3)
        val ν: Double
        val H0: Double
        var n: Double
        val dayBefore: Equatorial
        val dayOfInterest: Equatorial
        val dayAfter: Equatorial
        jd = floor(jd + 0.5) - 0.5
        val ΔT = calculateTimeDifference(jd)
        dayBefore = calculateMoonEqutarialCoordinates(jd - 1, ΔT)
        dayOfInterest = calculateMoonEqutarialCoordinates(jd, ΔT)
        dayAfter = calculateMoonEqutarialCoordinates(jd + 1, ΔT)
        // private final int MOONRADIUS=15;
        val h0Prime = getLunarRiseSetAltitude(dayOfInterest.Δ, temperature, pressure, altitude)
        ν = SolarPosition.calculateGreenwichSiderealTime(jd, ΔT)
        m_trs[0] = SolarPosition.approxSunTransitTime(dayOfInterest.α, longitude, ν)
        H0 = SolarPosition.getHourAngleAtRiseSet(latitude, dayOfInterest.δ, h0Prime)
        val δ = doubleArrayOf(dayBefore.δ, dayOfInterest.δ, dayAfter.δ)
        val moontransit: Double
        val moonrise: Double
        val moonset: Double
        if (dayOfInterest.α - dayBefore.α > 180.0) {
            dayBefore.α += 360
        } else if (dayOfInterest.α - dayBefore.α < -180.0) {
            dayOfInterest.α += 360
        }
        if (dayAfter.α - dayOfInterest.α > 180.0) {
            dayOfInterest.α += 360
        } else if (dayAfter.α - dayOfInterest.α < -180.0) {
            dayAfter.α += 360
        }
        val α = doubleArrayOf(dayBefore.α, dayOfInterest.α, dayAfter.α)
        if (H0 >= 0) {
            SolarPosition.approxSunRiseAndSet(m_trs, H0)
            for (i in 0..2) {
                νRts[i] = ν + 360.985647 * m_trs[i]
                n = m_trs[i] + ΔT / 86400.0
                αPrime[i] = SolarPosition.Interpolate(n, α)
                δPrime[i] = SolarPosition.Interpolate(n, δ)
                HPrime[i] = SolarPosition.limitDegrees180pm(νRts[i] + longitude - αPrime[i])
                h_rts[i] = SolarPosition.rtsSunAltitude(latitude, δPrime[i], HPrime[i])
            }
            val T = m_trs[0] - HPrime[0] / 360.0
            val R = SolarPosition.sunRiseAndSet(m_trs, h_rts, δPrime, latitude, HPrime, h0Prime, 1)
            val S = SolarPosition.sunRiseAndSet(m_trs, h_rts, δPrime, latitude, HPrime, h0Prime, 2)
            moontransit = SolarPosition.dayFracToLocalHour(T, timezone)
            moonrise = SolarPosition.dayFracToLocalHour(R, timezone)
            moonset = SolarPosition.dayFracToLocalHour(S, timezone)
            /*  System.out.println("MoonRise       :" + AstroLib.SecTime(moonrise));
            System.out.println("MoonTransit    :" + AstroLib.SecTime(moontransit));
            System.out.println("MoonSet        :" + AstroLib.SecTime(moonset));*/println(
                getStringHHMMSSS(moonrise) + "  " + getStringHHMMSSS(moontransit) + " " + getStringHHMMSSS(
                    moonset
                )
            )
        }
    }

    fun calculateMoonRiseTransitSet(
        jd: Double,
        latitude: Double,
        longitude: Double,
        timezone: Double,
        temperature: Int,
        pressure: Int,
        altitude: Int
    ): DoubleArray {
        var jd = jd
        val m_trs: DoubleArray = DoubleArray(3)
        val h_rts: DoubleArray = DoubleArray(3)
        val νRts: DoubleArray = DoubleArray(3)
        val αPrime: DoubleArray = DoubleArray(3)
        val δPrime: DoubleArray = DoubleArray(3)
        val HPrime: DoubleArray = DoubleArray(3)
        val moonRiseSet: DoubleArray = DoubleArray(3)
        val ν: Double
        val H0: Double
        var n: Double
        val dayBefore: Equatorial
        val dayOfInterest: Equatorial
        val dayAfter: Equatorial
        jd = floor(jd + 0.5) - 0.5
        val ΔT = calculateTimeDifference(jd)
        dayBefore = calculateMoonEqutarialCoordinates(jd - 1, ΔT)
        dayOfInterest = calculateMoonEqutarialCoordinates(jd, ΔT)
        dayAfter = calculateMoonEqutarialCoordinates(jd + 1, ΔT)
        // private final int MOONRADIUS=15;
        val h0Prime = getLunarRiseSetAltitude(dayOfInterest.Δ, temperature, pressure, altitude)
        ν = SolarPosition.calculateGreenwichSiderealTime(jd, ΔT)
        m_trs[0] = SolarPosition.approxSunTransitTime(dayOfInterest.α, longitude, ν)
        H0 = SolarPosition.getHourAngleAtRiseSet(latitude, dayOfInterest.δ, h0Prime)
        val δ = doubleArrayOf(dayBefore.δ, dayOfInterest.δ, dayAfter.δ)
        if (dayOfInterest.α - dayBefore.α > 180.0) {
            dayBefore.α += 360
        } else if (dayOfInterest.α - dayBefore.α < -180.0) {
            dayOfInterest.α += 360
        }
        if (dayAfter.α - dayOfInterest.α > 180.0) {
            dayOfInterest.α += 360
        } else if (dayAfter.α - dayOfInterest.α < -180.0) {
            dayAfter.α += 360
        }
        val α = doubleArrayOf(dayBefore.α, dayOfInterest.α, dayAfter.α)
        if (H0 >= 0) {
            SolarPosition.approxSunRiseAndSet(m_trs, H0)
            for (i in 0..2) {
                νRts[i] = ν + 360.985647 * m_trs[i]
                n = m_trs[i] + ΔT / 86400.0
                αPrime[i] = SolarPosition.Interpolate(n, α)
                δPrime[i] = SolarPosition.Interpolate(n, δ)
                HPrime[i] = SolarPosition.limitDegrees180pm(νRts[i] + longitude - αPrime[i])
                h_rts[i] = SolarPosition.rtsSunAltitude(latitude, δPrime[i], HPrime[i])
            }
            val T = m_trs[0] - HPrime[0] / 360.0
            val R = SolarPosition.sunRiseAndSet(m_trs, h_rts, δPrime, latitude, HPrime, h0Prime, 1)
            val S = SolarPosition.sunRiseAndSet(m_trs, h_rts, δPrime, latitude, HPrime, h0Prime, 2)
            moonRiseSet[1] = SolarPosition.dayFracToLocalHour(T, timezone)
            moonRiseSet[0] = SolarPosition.dayFracToLocalHour(R, timezone)
            moonRiseSet[2] = SolarPosition.dayFracToLocalHour(S, timezone)
        }
        return moonRiseSet
    }

    fun getHorizontalParallax(RadiusVector: Double): Double {
        return Math.toDegrees(asin(6378.14 / RadiusVector))
    }

    fun getLunarRiseSetAltitude(Δ: Double, temperature: Int, pressure: Int, altitude: Int): Double {
        //double Δinkm=149598000.0*Δ;
        val π = getHorizontalParallax(Δ)
        return 0.7275 * π - getApparentAtmosphericRefraction(0.0) * getWeatherCorrectionCoefficent(
            temperature,
            pressure
        ) - getAltitudeCorrection(altitude)
    }
}
