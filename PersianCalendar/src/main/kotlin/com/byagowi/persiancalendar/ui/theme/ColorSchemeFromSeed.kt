// The following is original from https://github.com/material-components/material-components-android/tree/ba465a2/lib/java/com/google/android/material/color/utilities
// But only the relevant parts is turned to Kotlin
/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.byagowi.persiancalendar.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.ln1p
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

fun lightColorSchemeFromSeed(argb: Int) = lightFromCorePalette(CorePalette.of(argb))

fun darkColorSchemeFromSeed(argb: Int) = darkFromCorePalette(CorePalette.of(argb))

//fun lightContent(argb: Int): ColorScheme {
//    return lightFromCorePalette(CorePalette.contentOf(argb))
//}
//
//fun darkContent(argb: Int): ColorScheme {
//    return darkFromCorePalette(CorePalette.contentOf(argb))
//}

private fun lightFromCorePalette(core: CorePalette): ColorScheme {
    return lightColorScheme(
        primary = Color(core.a1.tone(40)),
        onPrimary = Color(core.a1.tone(100)),
        primaryContainer = Color(core.a1.tone(90)),
        onPrimaryContainer = Color(core.a1.tone(10)),
        secondary = Color(core.a2.tone(40)),
        onSecondary = Color(core.a2.tone(100)),
        secondaryContainer = Color(core.a2.tone(90)),
        onSecondaryContainer = Color(core.a2.tone(10)),
        tertiary = Color(core.a3.tone(40)),
        onTertiary = Color(core.a3.tone(100)),
        tertiaryContainer = Color(core.a3.tone(90)),
        onTertiaryContainer = Color(core.a3.tone(10)),
        error = Color(core.error.tone(40)),
        onError = Color(core.error.tone(100)),
        errorContainer = Color(core.error.tone(90)),
        onErrorContainer = Color(core.error.tone(10)),
        background = Color(core.n1.tone(99)),
        onBackground = Color(core.n1.tone(10)),
        surface = Color(core.n1.tone(99)),
        onSurface = Color(core.n1.tone(10)),
        surfaceVariant = Color(core.n2.tone(90)),
        onSurfaceVariant = Color(core.n2.tone(30)),
        outline = Color(core.n2.tone(50)),
        outlineVariant = Color(core.n2.tone(80)),
        //shadow = Color(core.n1.tone(0)),
        scrim = Color(core.n1.tone(0)),
        inverseSurface = Color(core.n1.tone(20)),
        inverseOnSurface = Color(core.n1.tone(95)),
        inversePrimary = Color(core.a1.tone(80)),
    )
}

private fun darkFromCorePalette(core: CorePalette): ColorScheme {
    return darkColorScheme(
        primary = Color(core.a1.tone(80)),
        onPrimary = Color(core.a1.tone(20)),
        primaryContainer = Color(core.a1.tone(30)),
        onPrimaryContainer = Color(core.a1.tone(90)),
        secondary = Color(core.a2.tone(80)),
        onSecondary = Color(core.a2.tone(20)),
        secondaryContainer = Color(core.a2.tone(30)),
        onSecondaryContainer = Color(core.a2.tone(90)),
        tertiary = Color(core.a3.tone(80)),
        onTertiary = Color(core.a3.tone(20)),
        tertiaryContainer = Color(core.a3.tone(30)),
        onTertiaryContainer = Color(core.a3.tone(90)),
        error = Color(core.error.tone(80)),
        onError = Color(core.error.tone(20)),
        errorContainer = Color(core.error.tone(30)),
        onErrorContainer = Color(core.error.tone(80)),
        background = Color(core.n1.tone(10)),
        onBackground = Color(core.n1.tone(90)),
        surface = Color(core.n1.tone(10)),
        onSurface = Color(core.n1.tone(90)),
        surfaceVariant = Color(core.n2.tone(30)),
        onSurfaceVariant = Color(core.n2.tone(80)),
        outline = Color(core.n2.tone(60)),
        outlineVariant = Color(core.n2.tone(30)),
        //shadow = Color(core.n1.tone(0)),
        scrim = Color(core.n1.tone(0)),
        inverseSurface = Color(core.n1.tone(90)),
        inverseOnSurface = Color(core.n1.tone(20)),
        inversePrimary = Color(core.a1.tone(40)),
    )
}

private class CorePalette private constructor(argb: Int, isContent: Boolean) {
    val a1: TonalPalette
    val a2: TonalPalette
    val a3: TonalPalette
    val n1: TonalPalette
    val n2: TonalPalette
    var error: TonalPalette

    init {
        val hct = Hct.fromInt(argb)
        val hue = hct.hue
        val chroma = hct.chroma
        if (isContent) {
            a1 = TonalPalette.fromHueAndChroma(hue, chroma)
            a2 = TonalPalette.fromHueAndChroma(hue, chroma / 3.0)
            a3 = TonalPalette.fromHueAndChroma(hue + 60.0, chroma / 2.0)
            n1 = TonalPalette.fromHueAndChroma(hue, min(chroma / 12.0, 4.0))
            n2 = TonalPalette.fromHueAndChroma(hue, min(chroma / 6.0, 8.0))
        } else {
            a1 = TonalPalette.fromHueAndChroma(hue, max(48.0, chroma))
            a2 = TonalPalette.fromHueAndChroma(hue, 16.0)
            a3 = TonalPalette.fromHueAndChroma(hue + 60.0, 24.0)
            n1 = TonalPalette.fromHueAndChroma(hue, 4.0)
            n2 = TonalPalette.fromHueAndChroma(hue, 8.0)
        }
        error = TonalPalette.fromHueAndChroma(25.0, 84.0)
    }

    companion object {
        /**
         * Create key tones from a color.
         *
         * @param argb ARGB representation of a color
         */
        fun of(argb: Int): CorePalette {
            return CorePalette(argb, false)
        }

        /**
         * Create content key tones from a color.
         *
         * @param argb ARGB representation of a color
         */
        fun contentOf(argb: Int): CorePalette {
            return CorePalette(argb, true)
        }
    }
}

private class TonalPalette private constructor(
    /** The hue of the Tonal Palette, in HCT. Ranges from 0 to 360.  */
    private var hue: Double,
    /** The chroma of the Tonal Palette, in HCT. Ranges from 0 to ~130 (for sRGB gamut).  */
    private var chroma: Double,
) {
    private var cache = HashMap<Int, Int>()

    /**
     * Create an ARGB color with HCT hue and chroma of this Tones instance, and the provided HCT tone.
     *
     * @param tone HCT tone, measured from 0 to 100.
     * @return ARGB representation of a color with that tone.
     */
    fun tone(tone: Int): Int {
        var color = cache[tone]
        if (color == null) {
            color = Hct.from(hue, chroma, tone.toDouble()).argb
            cache[tone] = color
        }
        return color
    }

    companion object {
        /**
         * Create tones from a defined HCT hue and chroma.
         *
         * @param hue HCT hue
         * @param chroma HCT chroma
         * @return Tones matching hue and chroma.
         */
        fun fromHueAndChroma(hue: Double, chroma: Double): TonalPalette {
            return TonalPalette(hue, chroma)
        }
    }
}

private class Hct private constructor(argb: Int) {
    var hue = 0.0
    var chroma = 0.0
    var tone = 0.0
    var argb = 0

    init {
        setInternalState(argb)
    }

    private fun setInternalState(argb: Int) {
        this.argb = argb
        val cam = Cam16.fromInt(argb)
        hue = cam.hue
        chroma = cam.chroma
        tone = ColorUtils.lstarFromArgb(argb)
    }

    companion object {
        /**
         * Create an HCT color from hue, chroma, and tone.
         *
         * @param hue 0 <= hue < 360; invalid values are corrected.
         * @param chroma 0 <= chroma < ?; Informally, colorfulness. The color returned may be lower than
         * the requested chroma. Chroma has a different maximum for any given hue and tone.
         * @param tone 0 <= tone <= 100; invalid values are corrected.
         * @return HCT representation of a color in default viewing conditions.
         */
        fun from(hue: Double, chroma: Double, tone: Double): Hct {
            val argb = HctSolver.solveToInt(hue, chroma, tone)
            return Hct(argb)
        }

        /**
         * Create an HCT color from a color.
         *
         * @param argb ARGB representation of a color.
         * @return HCT representation of a color in default viewing conditions
         */
        fun fromInt(argb: Int): Hct {
            return Hct(argb)
        }
    }
}

object HctSolver {
    private val SCALED_DISCOUNT_FROM_LINRGB = arrayOf(
        doubleArrayOf(
            0.001200833568784504, 0.002389694492170889, 0.0002795742885861124
        ), doubleArrayOf(
            0.0005891086651375999, 0.0029785502573438758, 0.0003270666104008398
        ), doubleArrayOf(
            0.00010146692491640572, 0.0005364214359186694, 0.0032979401770712076
        )
    )
    private val LINRGB_FROM_SCALED_DISCOUNT = arrayOf(
        doubleArrayOf(
            1373.2198709594231, -1100.4251190754821, -7.278681089101213
        ), doubleArrayOf(
            -271.815969077903, 559.6580465940733, -32.46047482791194
        ), doubleArrayOf(
            1.9622899599665666, -57.173814538844006, 308.7233197812385
        )
    )
    private val Y_FROM_LINRGB = doubleArrayOf(0.2126, 0.7152, 0.0722)
    private val CRITICAL_PLANES = doubleArrayOf(
        0.015176349177441876,
        0.045529047532325624,
        0.07588174588720938,
        0.10623444424209313,
        0.13658714259697685,
        0.16693984095186062,
        0.19729253930674434,
        0.2276452376616281,
        0.2579979360165119,
        0.28835063437139563,
        0.3188300904430532,
        0.350925934958123,
        0.3848314933096426,
        0.42057480301049466,
        0.458183274052838,
        0.4976837250274023,
        0.5391024159806381,
        0.5824650784040898,
        0.6277969426914107,
        0.6751227633498623,
        0.7244668422128921,
        0.775853049866786,
        0.829304845476233,
        0.8848452951698498,
        0.942497089126609,
        1.0022825574869039,
        1.0642236851973577,
        1.1283421258858297,
        1.1946592148522128,
        1.2631959812511864,
        1.3339731595349034,
        1.407011200216447,
        1.4823302800086415,
        1.5599503113873272,
        1.6398909516233677,
        1.7221716113234105,
        1.8068114625156377,
        1.8938294463134073,
        1.9832442801866852,
        2.075074464868551,
        2.1693382909216234,
        2.2660538449872063,
        2.36523901573795,
        2.4669114995532007,
        2.5710888059345764,
        2.6777882626779785,
        2.7870270208169257,
        2.898822059350997,
        3.0131901897720907,
        3.1301480604002863,
        3.2497121605402226,
        3.3718988244681087,
        3.4967242352587946,
        3.624204428461639,
        3.754355295633311,
        3.887192587735158,
        4.022731918402185,
        4.160988767090289,
        4.301978482107941,
        4.445716283538092,
        4.592217266055746,
        4.741496401646282,
        4.893568542229298,
        5.048448422192488,
        5.20615066083972,
        5.3666897647573375,
        5.5300801301023865,
        5.696336044816294,
        5.865471690767354,
        6.037501145825082,
        6.212438385869475,
        6.390297286737924,
        6.571091626112461,
        6.7548350853498045,
        6.941541251256611,
        7.131223617812143,
        7.323895587840543,
        7.5195704746346665,
        7.7182615035334345,
        7.919981813454504,
        8.124744458384042,
        8.332562408825165,
        8.543448553206703,
        8.757415699253682,
        8.974476575321063,
        9.194643831691977,
        9.417930041841839,
        9.644347703669503,
        9.873909240696694,
        10.106627003236781,
        10.342513269534024,
        10.58158024687427,
        10.8238400726681,
        11.069304815507364,
        11.317986476196008,
        11.569896988756009,
        11.825048221409341,
        12.083451977536606,
        12.345119996613247,
        12.610063955123938,
        12.878295467455942,
        13.149826086772048,
        13.42466730586372,
        13.702830557985108,
        13.984327217668513,
        14.269168601521828,
        14.55736596900856,
        14.848930523210871,
        15.143873411576273,
        15.44220572664832,
        15.743938506781891,
        16.04908273684337,
        16.35764934889634,
        16.66964922287304,
        16.985093187232053,
        17.30399201960269,
        17.62635644741625,
        17.95219714852476,
        18.281524751807332,
        18.614349837764564,
        18.95068293910138,
        19.290534541298456,
        19.633915083172692,
        19.98083495742689,
        20.331304511189067,
        20.685334046541502,
        21.042933821039977,
        21.404114048223256,
        21.76888489811322,
        22.137256497705877,
        22.50923893145328,
        22.884842241736916,
        23.264076429332462,
        23.6469514538663,
        24.033477234264016,
        24.42366364919083,
        24.817520537484558,
        25.21505769858089,
        25.61628489293138,
        26.021211842414342,
        26.429848230738664,
        26.842203703840827,
        27.258287870275353,
        27.678110301598522,
        28.10168053274597,
        28.529008062403893,
        28.96010235337422,
        29.39497283293396,
        29.83362889318845,
        30.276079891419332,
        30.722335150426627,
        31.172403958865512,
        31.62629557157785,
        32.08401920991837,
        32.54558406207592,
        33.010999283389665,
        33.4802739966603,
        33.953417292456834,
        34.430438229418264,
        34.911345834551085,
        35.39614910352207,
        35.88485700094671,
        36.37747846067349,
        36.87402238606382,
        37.37449765026789,
        37.87891309649659,
        38.38727753828926,
        38.89959975977785,
        39.41588851594697,
        39.93615253289054,
        40.460400508064545,
        40.98864111053629,
        41.520882981230194,
        42.05713473317016,
        42.597404951718396,
        43.141702194811224,
        43.6900349931913,
        44.24241185063697,
        44.798841244188324,
        45.35933162437017,
        45.92389141541209,
        46.49252901546552,
        47.065252796817916,
        47.64207110610409,
        48.22299226451468,
        48.808024568002054,
        49.3971762874833,
        49.9904556690408,
        50.587870934119984,
        51.189430279724725,
        51.79514187861014,
        52.40501387947288,
        53.0190544071392,
        53.637271562750364,
        54.259673423945976,
        54.88626804504493,
        55.517063457223934,
        56.15206766869424,
        56.79128866487574,
        57.43473440856916,
        58.08241284012621,
        58.734331877617365,
        59.39049941699807,
        60.05092333227251,
        60.715611475655585,
        61.38457167773311,
        62.057811747619894,
        62.7353394731159,
        63.417162620860914,
        64.10328893648692,
        64.79372614476921,
        65.48848194977529,
        66.18756403501224,
        66.89098006357258,
        67.59873767827808,
        68.31084450182222,
        69.02730813691093,
        69.74813616640164,
        70.47333615344107,
        71.20291564160104,
        71.93688215501312,
        72.67524319850172,
        73.41800625771542,
        74.16517879925733,
        74.9167682708136,
        75.67278210128072,
        76.43322770089146,
        77.1981124613393,
        77.96744375590167,
        78.74122893956174,
        79.51947534912904,
        80.30219030335869,
        81.08938110306934,
        81.88105503125999,
        82.67721935322541,
        83.4778813166706,
        84.28304815182372,
        85.09272707154808,
        85.90692527145302,
        86.72564993000343,
        87.54890820862819,
        88.3767072518277,
        89.2090541872801,
        90.04595612594655,
        90.88742016217518,
        91.73345337380438,
        92.58406282226491,
        93.43925555268066,
        94.29903859396902,
        95.16341895893969,
        96.03240364439274,
        96.9059996312159,
        97.78421388448044,
        98.6670533535366,
        99.55452497210776
    )

    /**
     * Sanitizes a small enough angle in radians.
     *
     * @param angle An angle in radians; must not deviate too much from 0.
     * @return A coterminal angle between 0 and 2pi.
     */
    private fun sanitizeRadians(angle: Double) = (angle + Math.PI * 8) % (Math.PI * 2)

    /**
     * Delinearizes an RGB component, returning a floating-point number.
     *
     * @param rgbComponent 0.0 <= rgb_component <= 100.0, represents linear R/G/B channel
     * @return 0.0 <= output <= 255.0, color channel converted to regular RGB space
     */
    private fun trueDelinearized(rgbComponent: Double): Double {
        val normalized = rgbComponent / 100.0
        val delinearized = if (normalized <= 0.0031308) {
            normalized * 12.92
        } else {
            1.055 * normalized.pow(1.0 / 2.4) - 0.055
        }
        return delinearized * 255.0
    }

    private fun chromaticAdaptation(component: Double): Double {
        val af = abs(component).pow(0.42)
        return component.sign * 400.0 * af / (af + 27.13)
    }

    /**
     * Returns the hue of a linear RGB color in CAM16.
     *
     * @param linrgb The linear RGB coordinates of a color.
     * @return The hue of the color in CAM16, in radians.
     */
    private fun hueOf(linrgb: DoubleArray): Double {
        val scaledDiscount = MathUtils.matrixMultiply(linrgb, SCALED_DISCOUNT_FROM_LINRGB)
        val rA = chromaticAdaptation(scaledDiscount[0])
        val gA = chromaticAdaptation(scaledDiscount[1])
        val bA = chromaticAdaptation(scaledDiscount[2])
        // redness-greenness
        val a = (11.0 * rA + -12.0 * gA + bA) / 11.0
        // yellowness-blueness
        val b = (rA + gA - 2.0 * bA) / 9.0
        return atan2(b, a)
    }

    private fun areInCyclicOrder(a: Double, b: Double, c: Double): Boolean {
        val deltaAB = sanitizeRadians(b - a)
        val deltaAC = sanitizeRadians(c - a)
        return deltaAB < deltaAC
    }

    /**
     * Solves the lerp equation.
     *
     * @param source The starting number.
     * @param mid The number in the middle.
     * @param target The ending number.
     * @return A number t such that lerp(source, target, t) = mid.
     */
    private fun intercept(source: Double, mid: Double, target: Double): Double {
        return (mid - source) / (target - source)
    }

    private fun lerpPoint(source: DoubleArray, t: Double, target: DoubleArray): DoubleArray {
        return doubleArrayOf(
            source[0] + (target[0] - source[0]) * t,
            source[1] + (target[1] - source[1]) * t,
            source[2] + (target[2] - source[2]) * t
        )
    }

    /**
     * Intersects a segment with a plane.
     *
     * @param source The coordinates of point A.
     * @param coordinate The R-, G-, or B-coordinate of the plane.
     * @param target The coordinates of point B.
     * @param axis The axis the plane is perpendicular with. (0: R, 1: G, 2: B)
     * @return The intersection point of the segment AB with the plane R=coordinate, G=coordinate, or
     * B=coordinate
     */
    private fun setCoordinate(
        source: DoubleArray,
        coordinate: Double,
        target: DoubleArray,
        axis: Int
    ): DoubleArray {
        val t = intercept(source[axis], coordinate, target[axis])
        return lerpPoint(source, t, target)
    }

    private fun isBounded(x: Double): Boolean = x in 0.0..100.0

    /**
     * Returns the nth possible vertex of the polygonal intersection.
     *
     * @param y The Y value of the plane.
     * @param n The zero-based index of the point. 0 <= n <= 11.
     * @return The nth possible vertex of the polygonal intersection of the y plane and the RGB cube,
     * in linear RGB coordinates, if it exists. If this possible vertex lies outside of the cube,
     * [-1.0, -1.0, -1.0] is returned.
     */
    private fun nthVertex(y: Double, n: Int): DoubleArray {
        val kR = Y_FROM_LINRGB[0]
        val kG = Y_FROM_LINRGB[1]
        val kB = Y_FROM_LINRGB[2]
        val coordA = if (n % 4 <= 1) 0.0 else 100.0
        val coordB = if (n % 2 == 0) 0.0 else 100.0
        return if (n < 4) {
            val r = (y - coordA * kG - coordB * kB) / kR
            if (isBounded(r)) {
                doubleArrayOf(r, coordA, coordB)
            } else {
                doubleArrayOf(-1.0, -1.0, -1.0)
            }
        } else if (n < 8) {
            val g = (y - coordB * kR - coordA * kB) / kG
            if (isBounded(g)) {
                doubleArrayOf(coordB, g, coordA)
            } else {
                doubleArrayOf(-1.0, -1.0, -1.0)
            }
        } else {
            val b = (y - coordA * kR - coordB * kG) / kB
            if (isBounded(b)) {
                doubleArrayOf(coordA, coordB, b)
            } else {
                doubleArrayOf(-1.0, -1.0, -1.0)
            }
        }
    }

    /**
     * Finds the segment containing the desired color.
     *
     * @param y The Y value of the color.
     * @param targetHue The hue of the color.
     * @return A list of two sets of linear RGB coordinates, each corresponding to an endpoint of the
     * segment containing the desired color.
     */
    private fun bisectToSegment(y: Double, targetHue: Double): Array<DoubleArray> {
        var left = doubleArrayOf(-1.0, -1.0, -1.0)
        var right = left
        var leftHue = 0.0
        var rightHue = 0.0
        var initialized = false
        var uncut = true
        for (n in 0..11) {
            val mid = nthVertex(y, n)
            if (mid[0] < 0) {
                continue
            }
            val midHue = hueOf(mid)
            if (!initialized) {
                left = mid
                right = mid
                leftHue = midHue
                rightHue = midHue
                initialized = true
                continue
            }
            if (uncut || areInCyclicOrder(leftHue, midHue, rightHue)) {
                uncut = false
                if (areInCyclicOrder(leftHue, targetHue, midHue)) {
                    right = mid
                    rightHue = midHue
                } else {
                    left = mid
                    leftHue = midHue
                }
            }
        }
        return arrayOf(left, right)
    }

    private fun midpoint(a: DoubleArray, b: DoubleArray): DoubleArray {
        return doubleArrayOf(
            (a[0] + b[0]) / 2, (a[1] + b[1]) / 2, (a[2] + b[2]) / 2
        )
    }

    private fun criticalPlaneBelow(x: Double): Int {
        return floor(x - 0.5).toInt()
    }

    private fun criticalPlaneAbove(x: Double): Int {
        return ceil(x - 0.5).toInt()
    }

    /**
     * Finds a color with the given Y and hue on the boundary of the cube.
     *
     * @param y The Y value of the color.
     * @param targetHue The hue of the color.
     * @return The desired color, in linear RGB coordinates.
     */
    private fun bisectToLimit(y: Double, targetHue: Double): DoubleArray {
        val segment = bisectToSegment(y, targetHue)
        var left = segment[0]
        var leftHue = hueOf(left)
        var right = segment[1]
        for (axis in 0..2) {
            if (left[axis] != right[axis]) {
                var lPlane = -1
                var rPlane = 255
                if (left[axis] < right[axis]) {
                    lPlane = criticalPlaneBelow(trueDelinearized(left[axis]))
                    rPlane = criticalPlaneAbove(trueDelinearized(right[axis]))
                } else {
                    lPlane = criticalPlaneAbove(trueDelinearized(left[axis]))
                    rPlane = criticalPlaneBelow(trueDelinearized(right[axis]))
                }
                for (i in 0..7) {
                    if (abs(rPlane - lPlane) <= 1) {
                        break
                    } else {
                        val mPlane = floor((lPlane + rPlane) / 2.0).toInt()
                        val midPlaneCoordinate = CRITICAL_PLANES[mPlane]
                        val mid = setCoordinate(left, midPlaneCoordinate, right, axis)
                        val midHue = hueOf(mid)
                        if (areInCyclicOrder(leftHue, targetHue, midHue)) {
                            right = mid
                            rPlane = mPlane
                        } else {
                            left = mid
                            leftHue = midHue
                            lPlane = mPlane
                        }
                    }
                }
            }
        }
        return midpoint(left, right)
    }

    private fun inverseChromaticAdaptation(adapted: Double): Double {
        val adaptedAbs = abs(adapted)
        val base = max(0.0, 27.13 * adaptedAbs / (400.0 - adaptedAbs))
        return adapted.sign * base.pow(1.0 / 0.42)
    }

    /**
     * Finds a color with the given hue, chroma, and Y.
     *
     * @param hueRadians The desired hue in radians.
     * @param chroma The desired chroma.
     * @param y The desired Y.
     * @return The desired color as a hexadecimal integer, if found; 0 otherwise.
     */
    private fun findResultByJ(hueRadians: Double, chroma: Double, y: Double): Int {
        // Initial estimate of j.
        var j = sqrt(y) * 11.0
        // ===========================================================
        // Operations inlined from Cam16 to avoid repeated calculation
        // ===========================================================
        val viewingConditions = ViewingConditions.DEFAULT
        val tInnerCoeff = 1 / (1.64 - 0.29.pow(viewingConditions.n)).pow(0.73)
        val eHue = 0.25 * (cos(hueRadians + 2.0) + 3.8)
        val p1 = eHue * (50000.0 / 13.0) * viewingConditions.nc * viewingConditions.ncb
        val hSin = sin(hueRadians)
        val hCos = cos(hueRadians)
        for (iterationRound in 0..4) {
            // ===========================================================
            // Operations inlined from Cam16 to avoid repeated calculation
            // ===========================================================
            val jNormalized = j / 100.0
            val alpha = if (chroma == 0.0 || j == 0.0) 0.0 else chroma / sqrt(jNormalized)
            val t = (alpha * tInnerCoeff).pow(1.0 / 0.9)
            val ac = (viewingConditions.aw
                    * jNormalized.pow(1.0 / viewingConditions.c / viewingConditions.z))
            val p2 = ac / viewingConditions.nbb
            val gamma = 23.0 * (p2 + 0.305) * t / (23.0 * p1 + 11 * t * hCos + 108.0 * t * hSin)
            val a = gamma * hCos
            val b = gamma * hSin
            val rA = (460.0 * p2 + 451.0 * a + 288.0 * b) / 1403.0
            val gA = (460.0 * p2 - 891.0 * a - 261.0 * b) / 1403.0
            val bA = (460.0 * p2 - 220.0 * a - 6300.0 * b) / 1403.0
            val rCScaled = inverseChromaticAdaptation(rA)
            val gCScaled = inverseChromaticAdaptation(gA)
            val bCScaled = inverseChromaticAdaptation(bA)
            val linrgb = MathUtils.matrixMultiply(
                doubleArrayOf(rCScaled, gCScaled, bCScaled),
                LINRGB_FROM_SCALED_DISCOUNT
            )
            // ===========================================================
            // Operations inlined from Cam16 to avoid repeated calculation
            // ===========================================================
            if (linrgb[0] < 0 || linrgb[1] < 0 || linrgb[2] < 0) {
                return 0
            }
            val kR = Y_FROM_LINRGB[0]
            val kG = Y_FROM_LINRGB[1]
            val kB = Y_FROM_LINRGB[2]
            val fnj = kR * linrgb[0] + kG * linrgb[1] + kB * linrgb[2]
            if (fnj <= 0) {
                return 0
            }
            if (iterationRound == 4 || abs(fnj - y) < 0.002) {
                return if (linrgb[0] > 100.01 || linrgb[1] > 100.01 || linrgb[2] > 100.01) {
                    0
                } else ColorUtils.argbFromLinrgb(linrgb)
            }
            // Iterates with Newton method,
            // Using 2 * fn(j) / j as the approximation of fn'(j)
            j -= (fnj - y) * j / (2 * fnj)
        }
        return 0
    }

    /**
     * Finds an sRGB color with the given hue, chroma, and L*, if possible.
     *
     * @param hueDegrees The desired hue, in degrees.
     * @param chroma The desired chroma.
     * @param lstar The desired L*.
     * @return A hexadecimal representing the sRGB color. The color has sufficiently close hue,
     * chroma, and L* to the desired values, if possible; otherwise, the hue and L* will be
     * sufficiently close, and chroma will be maximized.
     */
    fun solveToInt(hueDegrees: Double, chroma: Double, lstar: Double): Int {
        var hueDegrees = hueDegrees
        if (chroma < 0.0001 || lstar < 0.0001 || lstar > 99.9999) {
            return ColorUtils.argbFromLstar(lstar)
        }
        hueDegrees = MathUtils.sanitizeDegreesDouble(hueDegrees)
        val hueRadians = hueDegrees / 180 * Math.PI
        val y = ColorUtils.yFromLstar(lstar)
        val exactAnswer = findResultByJ(hueRadians, chroma, y)
        if (exactAnswer != 0) {
            return exactAnswer
        }
        val linrgb = bisectToLimit(y, hueRadians)
        return ColorUtils.argbFromLinrgb(linrgb)
    }
}

private class Cam16
/**
 * All of the CAM16 dimensions can be calculated from 3 of the dimensions, in the following
 * combinations: - {j or q} and {c, m, or s} and hue - jstar, astar, bstar Prefer using a static
 * method that constructs from 3 of those dimensions. This constructor is intended for those
 * methods to use to return all possible dimensions.
 *
 * @param hue for example, red, orange, yellow, green, etc.
 * @param chroma informally, colorfulness / color intensity. like saturation in HSL, except
 * perceptually accurate.
 * @param j lightness
 * @param q brightness; ratio of lightness to white point's lightness
 * @param m colorfulness
 * @param s saturation; ratio of chroma to white point's chroma
 * @param jstar CAM16-UCS J coordinate
 * @param astar CAM16-UCS a coordinate
 * @param bstar CAM16-UCS b coordinate
 */ private constructor(
    /** Hue in CAM16  */
    // CAM16 color dimensions, see getters for documentation.
    val hue: Double,
    /** Chroma in CAM16  */
    val chroma: Double,
    /** Lightness in CAM16  */
    private val j: Double,
    /**
     * Brightness in CAM16.
     *
     *
     * Prefer lightness, brightness is an absolute quantity. For example, a sheet of white paper is
     * much brighter viewed in sunlight than in indoor light, but it is the lightest object under any
     * lighting.
     */
    private val q: Double,
    /**
     * Colorfulness in CAM16.
     *
     *
     * Prefer chroma, colorfulness is an absolute quantity. For example, a yellow toy car is much
     * more colorful outside than inside, but it has the same chroma in both environments.
     */
    val m: Double,
    /**
     * Saturation in CAM16.
     *
     *
     * Colorfulness in proportion to brightness. Prefer chroma, saturation measures colorfulness
     * relative to the color's own brightness, where chroma is colorfulness relative to white.
     */
    val s: Double,
    /** Lightness coordinate in CAM16-UCS  */
    // Coordinates in UCS space. Used to determine color distance, like delta E equations in L*a*b*.
    private val jstar: Double,
    /** a* coordinate in CAM16-UCS  */
    private val astar: Double,
    /** b* coordinate in CAM16-UCS  */
    private val bstar: Double
) {
    companion object {
        // Transforms XYZ color space coordinates to 'cone'/'RGB' responses in CAM16.
        val XYZ_TO_CAM16RGB = arrayOf(
            doubleArrayOf(0.401288, 0.650173, -0.051461),
            doubleArrayOf(-0.250268, 1.204414, 0.045854),
            doubleArrayOf(-0.002079, 0.048952, 0.953127)
        )

        /**
         * Create a CAM16 color from a color, assuming the color was viewed in default viewing conditions.
         *
         * @param argb ARGB representation of a color.
         */
        fun fromInt(argb: Int): Cam16 {
            return fromIntInViewingConditions(argb, ViewingConditions.DEFAULT)
        }

        /**
         * Create a CAM16 color from a color in defined viewing conditions.
         *
         * @param argb ARGB representation of a color.
         * @param viewingConditions Information about the environment where the color was observed.
         */
        // The RGB => XYZ conversion matrix elements are derived scientific constants. While the values
        // may differ at runtime due to floating point imprecision, keeping the values the same, and
        // accurate, across implementations takes precedence.
        private fun fromIntInViewingConditions(
            argb: Int,
            viewingConditions: ViewingConditions
        ): Cam16 {
            // Transform ARGB int to XYZ
            val red = argb and 0x00ff0000 shr 16
            val green = argb and 0x0000ff00 shr 8
            val blue = argb and 0x000000ff
            val redL = ColorUtils.linearized(red)
            val greenL = ColorUtils.linearized(green)
            val blueL = ColorUtils.linearized(blue)
            val x = 0.41233895 * redL + 0.35762064 * greenL + 0.18051042 * blueL
            val y = 0.2126 * redL + 0.7152 * greenL + 0.0722 * blueL
            val z = 0.01932141 * redL + 0.11916382 * greenL + 0.95034478 * blueL
            return fromXyzInViewingConditions(x, y, z, viewingConditions)
        }

        private fun fromXyzInViewingConditions(
            x: Double, y: Double, z: Double, viewingConditions: ViewingConditions
        ): Cam16 {
            // Transform XYZ to 'cone'/'rgb' responses
            val matrix = XYZ_TO_CAM16RGB
            val rT = x * matrix[0][0] + y * matrix[0][1] + z * matrix[0][2]
            val gT = x * matrix[1][0] + y * matrix[1][1] + z * matrix[1][2]
            val bT = x * matrix[2][0] + y * matrix[2][1] + z * matrix[2][2]

            // Discount illuminant
            val rD = viewingConditions.rgbD[0] * rT
            val gD = viewingConditions.rgbD[1] * gT
            val bD = viewingConditions.rgbD[2] * bT

            // Chromatic adaptation
            val rAF = (viewingConditions.fl * abs(rD) / 100.0).pow(0.42)
            val gAF = (viewingConditions.fl * abs(gD) / 100.0).pow(0.42)
            val bAF = (viewingConditions.fl * abs(bD) / 100.0).pow(0.42)
            val rA = sign(rD) * 400.0 * rAF / (rAF + 27.13)
            val gA = sign(gD) * 400.0 * gAF / (gAF + 27.13)
            val bA = sign(bD) * 400.0 * bAF / (bAF + 27.13)

            // redness-greenness
            val a = (11.0 * rA + -12.0 * gA + bA) / 11.0
            // yellowness-blueness
            val b = (rA + gA - 2.0 * bA) / 9.0

            // auxiliary components
            val u = (20.0 * rA + 20.0 * gA + 21.0 * bA) / 20.0
            val p2 = (40.0 * rA + 20.0 * gA + bA) / 20.0

            // hue
            val atan2 = atan2(b, a)
            val atanDegrees = Math.toDegrees(atan2)
            val hue =
                if (atanDegrees < 0) atanDegrees + 360.0 else if (atanDegrees >= 360) atanDegrees - 360.0 else atanDegrees
            val hueRadians = Math.toRadians(hue)

            // achromatic response to color
            val ac = p2 * viewingConditions.nbb

            // CAM16 lightness and brightness
            val j = (100.0
                    * (ac / viewingConditions.aw).pow(viewingConditions.c * viewingConditions.z))
            val q = ((4.0
                    / viewingConditions.c) * sqrt(j / 100.0)
                    * (viewingConditions.aw + 4.0)
                    * viewingConditions.flRoot)

            // CAM16 chroma, colorfulness, and saturation.
            val huePrime = if (hue < 20.14) hue + 360 else hue
            val eHue = 0.25 * (cos(Math.toRadians(huePrime) + 2.0) + 3.8)
            val p1 = 50000.0 / 13.0 * eHue * viewingConditions.nc * viewingConditions.ncb
            val t = p1 * hypot(a, b) / (u + 0.305)
            val alpha =
                (1.64 - 0.29.pow(viewingConditions.n)).pow(0.73) * t.pow(0.9)
            // CAM16 chroma, colorfulness, saturation
            val c = alpha * sqrt(j / 100.0)
            val m = c * viewingConditions.flRoot
            val s = 50.0 * sqrt(alpha * viewingConditions.c / (viewingConditions.aw + 4.0))

            // CAM16-UCS components
            val jstar = (1.0 + 100.0 * 0.007) * j / (1.0 + 0.007 * j)
            val mstar = 1.0 / 0.0228 * ln1p(0.0228 * m)
            val astar = mstar * cos(hueRadians)
            val bstar = mstar * sin(hueRadians)
            return Cam16(hue, c, j, q, m, s, jstar, astar, bstar)
        }
    }
}

private class ViewingConditions
/**
 * Parameters are intermediate values of the CAM16 conversion process. Their names are shorthand
 * for technical color science terminology, this class would not benefit from documenting them
 * individually. A brief overview is available in the CAM16 specification, and a complete overview
 * requires a color science textbook, such as Fairchild's Color Appearance Models.
 */ private constructor(
    val n: Double,
    val aw: Double,
    val nbb: Double,
    val ncb: Double,
    val c: Double,
    val nc: Double,
    val rgbD: DoubleArray,
    val fl: Double,
    val flRoot: Double,
    val z: Double
) {

    companion object {
        /** sRGB-like viewing conditions.  */
        val DEFAULT = defaultWithBackgroundLstar(50.0)

        /**
         * Create ViewingConditions from a simple, physically relevant, set of parameters.
         *
         * @param whitePoint White point, measured in the XYZ color space. default = D65, or sunny day
         * afternoon
         * @param adaptingLuminance The luminance of the adapting field. Informally, how bright it is in
         * the room where the color is viewed. Can be calculated from lux by multiplying lux by
         * 0.0586. default = 11.72, or 200 lux.
         * @param backgroundLstar The lightness of the area surrounding the color. measured by L* in
         * L*a*b*. default = 50.0
         * @param surround A general description of the lighting surrounding the color. 0 is pitch dark,
         * like watching a movie in a theater. 1.0 is a dimly light room, like watching TV at home at
         * night. 2.0 means there is no difference between the lighting on the color and around it.
         * default = 2.0
         * @param discountingIlluminant Whether the eye accounts for the tint of the ambient lighting,
         * such as knowing an apple is still red in green light. default = false, the eye does not
         * perform this process on self-luminous objects like displays.
         */
        fun make(
            whitePoint: DoubleArray,
            adaptingLuminance: Double,
            backgroundLstar: Double,
            surround: Double,
            discountingIlluminant: Boolean
        ): ViewingConditions {
            // A background of pure black is non-physical and leads to infinities that represent the idea
            // that any color viewed in pure black can't be seen.
            var backgroundLstar = backgroundLstar
            backgroundLstar = max(0.1, backgroundLstar)
            // Transform white point XYZ to 'cone'/'rgb' responses
            val matrix = Cam16.XYZ_TO_CAM16RGB
            val rW =
                whitePoint[0] * matrix[0][0] + whitePoint[1] * matrix[0][1] + whitePoint[2] * matrix[0][2]
            val gW =
                whitePoint[0] * matrix[1][0] + whitePoint[1] * matrix[1][1] + whitePoint[2] * matrix[1][2]
            val bW =
                whitePoint[0] * matrix[2][0] + whitePoint[1] * matrix[2][1] + whitePoint[2] * matrix[2][2]
            val f = 0.8 + surround / 10.0
            val c = if (f >= 0.9) MathUtils.lerp(
                0.59,
                0.69,
                (f - 0.9) * 10.0
            ) else MathUtils.lerp(0.525, 0.59, (f - 0.8) * 10.0)
            var d =
                if (discountingIlluminant) 1.0 else f * (1.0 - 1.0 / 3.6 * exp((-adaptingLuminance - 42.0) / 92.0))
            d = d.coerceIn(.0, 1.0)
            val rgbD = doubleArrayOf(
                d * (100.0 / rW) + 1.0 - d, d * (100.0 / gW) + 1.0 - d, d * (100.0 / bW) + 1.0 - d
            )
            val k = 1.0 / (5.0 * adaptingLuminance + 1.0)
            val k4 = k * k * k * k
            val k4F = 1.0 - k4
            val fl = k4 * adaptingLuminance + 0.1 * k4F * k4F * Math.cbrt(5.0 * adaptingLuminance)
            val n = ColorUtils.yFromLstar(backgroundLstar) / whitePoint[1]
            val z = 1.48 + sqrt(n)
            val nbb = 0.725 / n.pow(0.2)
            val rgbAFactors = doubleArrayOf(
                (fl * rgbD[0] * rW / 100.0).pow(0.42),
                (fl * rgbD[1] * gW / 100.0).pow(0.42),
                (fl * rgbD[2] * bW / 100.0).pow(0.42)
            )
            val rgbA = doubleArrayOf(
                400.0 * rgbAFactors[0] / (rgbAFactors[0] + 27.13),
                400.0 * rgbAFactors[1] / (rgbAFactors[1] + 27.13),
                400.0 * rgbAFactors[2] / (rgbAFactors[2] + 27.13)
            )
            val aw = (2.0 * rgbA[0] + rgbA[1] + 0.05 * rgbA[2]) * nbb
            return ViewingConditions(
                n,
                aw,
                nbb,
                nbb,
                c,
                f,
                rgbD,
                fl,
                fl.pow(0.25),
                z
            )
        }

        /**
         * Create sRGB-like viewing conditions with a custom background lstar.
         *
         *
         * Default viewing conditions have a lstar of 50, midgray.
         */
        private fun defaultWithBackgroundLstar(lstar: Double): ViewingConditions {
            return make(
                ColorUtils.whitePointD65(),
                200.0 / Math.PI * ColorUtils.yFromLstar(50.0) / 100f,
                lstar,
                2.0,
                false
            )
        }
    }
}

private object ColorUtils {
    private val SRGB_TO_XYZ = arrayOf(
        doubleArrayOf(0.41233895, 0.35762064, 0.18051042),
        doubleArrayOf(0.2126, 0.7152, 0.0722),
        doubleArrayOf(0.01932141, 0.11916382, 0.95034478)
    )
    private val WHITE_POINT_D65 = doubleArrayOf(95.047, 100.0, 108.883)

    /** Converts a color from RGB components to ARGB format.  */
    private fun argbFromRgb(red: Int, green: Int, blue: Int): Int {
        return 255 shl 24 or (red and 255 shl 16) or (green and 255 shl 8) or (blue and 255)
    }

    /** Converts a color from linear RGB components to ARGB format.  */
    fun argbFromLinrgb(linrgb: DoubleArray): Int {
        val r = delinearized(linrgb[0])
        val g = delinearized(linrgb[1])
        val b = delinearized(linrgb[2])
        return argbFromRgb(r, g, b)
    }

    /** Returns the red component of a color in ARGB format.  */
    private fun redFromArgb(argb: Int): Int {
        return argb shr 16 and 255
    }

    /** Returns the green component of a color in ARGB format.  */
    private fun greenFromArgb(argb: Int): Int {
        return argb shr 8 and 255
    }

    /** Returns the blue component of a color in ARGB format.  */
    private fun blueFromArgb(argb: Int): Int {
        return argb and 255
    }

    /** Converts a color from XYZ to ARGB.  */
    private fun xyzFromArgb(argb: Int): DoubleArray {
        val r = linearized(redFromArgb(argb))
        val g = linearized(greenFromArgb(argb))
        val b = linearized(blueFromArgb(argb))
        return MathUtils.matrixMultiply(doubleArrayOf(r, g, b), SRGB_TO_XYZ)
    }

    /**
     * Converts an L* value to an ARGB representation.
     *
     * @param lstar L* in L*a*b*
     * @return ARGB representation of grayscale color with lightness matching L*
     */
    fun argbFromLstar(lstar: Double): Int {
        val y = yFromLstar(lstar)
        val component = delinearized(y)
        return argbFromRgb(component, component, component)
    }

    /**
     * Computes the L* value of a color in ARGB representation.
     *
     * @param argb ARGB representation of a color
     * @return L*, from L*a*b*, coordinate of the color
     */
    fun lstarFromArgb(argb: Int): Double {
        val y = xyzFromArgb(argb)[1]
        return 116.0 * labF(y / 100.0) - 16.0
    }

    /**
     * Converts an L* value to a Y value.
     *
     *
     * L* in L*a*b* and Y in XYZ measure the same quantity, luminance.
     *
     *
     * L* measures perceptual luminance, a linear scale. Y in XYZ measures relative luminance, a
     * logarithmic scale.
     *
     * @param lstar L* in L*a*b*
     * @return Y in XYZ
     */
    fun yFromLstar(lstar: Double): Double {
        return 100.0 * labInvf((lstar + 16.0) / 116.0)
    }

    /**
     * Linearizes an RGB component.
     *
     * @param rgbComponent 0 <= rgb_component <= 255, represents R/G/B channel
     * @return 0.0 <= output <= 100.0, color channel converted to linear RGB space
     */
    fun linearized(rgbComponent: Int): Double {
        val normalized = rgbComponent / 255.0
        return if (normalized <= 0.040449936) {
            normalized / 12.92 * 100.0
        } else {
            ((normalized + 0.055) / 1.055).pow(2.4) * 100.0
        }
    }

    /**
     * Delinearizes an RGB component.
     *
     * @param rgbComponent 0.0 <= rgb_component <= 100.0, represents linear R/G/B channel
     * @return 0 <= output <= 255, color channel converted to regular RGB space
     */
    private fun delinearized(rgbComponent: Double): Int {
        val normalized = rgbComponent / 100.0
        val delinearized = if (normalized <= 0.0031308) {
            normalized * 12.92
        } else {
            1.055 * normalized.pow(1.0 / 2.4) - 0.055
        }
        return (delinearized * 255.0).roundToLong().toInt().coerceIn(0, 255)
    }

    /**
     * Returns the standard white point; white on a sunny day.
     *
     * @return The white point
     */
    fun whitePointD65(): DoubleArray = WHITE_POINT_D65

    private fun labF(t: Double): Double {
        val e = 216.0 / 24389.0
        val kappa = 24389.0 / 27.0
        return if (t > e) {
            t.pow(1.0 / 3.0)
        } else {
            (kappa * t + 16) / 116
        }
    }

    private fun labInvf(ft: Double): Double {
        val e = 216.0 / 24389.0
        val kappa = 24389.0 / 27.0
        val ft3 = ft * ft * ft
        return if (ft3 > e) {
            ft3
        } else {
            (116 * ft - 16) / kappa
        }
    }
}

private object MathUtils {
    /**
     * The linear interpolation function.
     *
     * @return start if amount = 0 and stop if amount = 1
     */
    fun lerp(start: Double, stop: Double, amount: Double): Double {
        return (1.0 - amount) * start + amount * stop
    }

    /**
     * Sanitizes a degree measure as a floating-point number.
     *
     * @return a degree measure between 0.0 (inclusive) and 360.0 (exclusive).
     */
    fun sanitizeDegreesDouble(degreesValue: Double): Double {
        var degrees = degreesValue
        degrees %= 360.0
        if (degrees < 0) {
            degrees += 360.0
        }
        return degrees
    }

    /** Multiplies a 1x3 row vector with a 3x3 matrix.  */
    fun matrixMultiply(row: DoubleArray, matrix: Array<DoubleArray>): DoubleArray {
        val a = row[0] * matrix[0][0] + row[1] * matrix[0][1] + row[2] * matrix[0][2]
        val b = row[0] * matrix[1][0] + row[1] * matrix[1][1] + row[2] * matrix[1][2]
        val c = row[0] * matrix[2][0] + row[1] * matrix[2][1] + row[2] * matrix[2][2]
        return doubleArrayOf(a, b, c)
    }
}
