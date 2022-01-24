package com.byagowi.persiancalendar.ui.map

// Unlike rest of the project this file doesn't have a clear copyright status thus isn't
// used on the app, this is a port of http://alperen.cepmuvakkit.com/js/rendo_en.htm (down now)
// or my cleaned up https://ebraminio.github.io/panorendo/ version. It is published by the same
// person who has developed QiblaCompassView so having the same copyright status of that isn't
// unexpected, yet, isn't mentioned anywhere also.
// So this isn't published with the same license of the project for now as far as I can say.

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.set
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun panoRendo(
    Elev: Double = 30.0, // Sun Elev. min -20 max 90 °
    Azi: Double = 0.0, // Sun Azi. min -180 max 180 °
    Alti: Double = 0.0, // Altitude. min 0 man 99 km
    Turbi: Double = 3.0, // Haze. min 0 max 99
    Ozone: Double = 300.0, // Ozone. min 230 max 460 step 10
    Luma: Double = 10.0, // Luma. min 1 max 99
    hd: Int = 0, // Tone Map. 0 -> Reinhard, 1 -> sRGB, 2 -> Linear
    zoom: Int = 0, // Zoom. min 0 max 10
): Bitmap {
    val RPD = PI / 180
    val O1 = 5e-5
    val O2 = 5e-5
    val O3 = 5e-6
    val R1 = .04
    val R2 = .09
    val R3 = .25

    val SE = Elev * RPD
    val SA = Azi * RPD
    var H = Alti * 1
    var T = Turbi
    val O = Ozone
    val B = Luma * if (hd != 0) 18 else 36
    val z = zoom * 1.0

    var R = if (T > 0) 1.0 else 0.0
    if (T < 0) T = -T
    val m1: Double
    if (T != .0) m1 = 1500.0 else {
        m1 = 300.0; H = .0
    }
    // GetId("Alti").hidden = GetId("Ozone").hidden = !T
    val EH = acos(6371 / (6371 + H))
    val x0 = PI / max(z, 1.0)
    val y0 = x0 / 2
    val x1 = if (z != .0) 800 else 400
    val y1 = if (z != .0) 200 else x1
    val y2 = if (z != .0) (y1 * (1 + EH / y0)).toInt() else y1
    val result = Bitmap.createBitmap(x1, y2, Bitmap.Config.ARGB_8888)
    val cosS = sin(SE)
    val sinS = cos(SE)
    val HS = if (cosS > 0) H else H - 6371.0 * (1 - sinS)
    var RS = 500.0 * cosS
    RS = min((sqrt(RS * RS + 1001) - RS) * exp(-HS / 8.4), 75.0)
    var MS = m1 * cosS
    MS = min((sqrt(MS * MS + 3001) - MS) * exp(-HS / 1.2), 200.0)
    var OS = 125.0 * cosS
    OS = min((sqrt(OS * OS + 251) - OS) * exp(-HS / 40), 25.0) * O
    val W1 = .03 * max(-HS, .0)
    val W2 = .03 * W1 * W1
    val M2 = if (T != .0) .06 * (T - 2 + 1 / T) else .33
    val MH = M2 * exp(-H / 1.2)
    val d = .94 * exp(-MH)
    val g = d * exp(-.02 * MS)
    val g2 = g * g
    val c = .89 - .11 * g
    val M1 = if (T != .0) M2 * c else .4
    val M3 = if (T != .0) M2 / c else .2
    R *= 42.0 / (3 + d)
    val MP0 = 2.7 * (1 - g2) / (3 + d + 2 * d * g2)
    val A0 = 8000 / max(MH, .001) / d
    val V0 = SE - EH - RPD
    val V1 = (1 + 20 * EH) / 60

    var Z1 = .0
    var Z2 = .0
    var Z3 = .0
    ((if (z > 1) -1 else 0) until y2).forEach { y ->
        val VE = if (y == -1) 90 * RPD else (1 - y.toDouble() / y1) * y0
        val fe = 1 - VE * 2 / PI
        val cosV = sin(VE)
        val sinV = cos(VE)
        val sinSV = sinS * sinV
        val cosSV = cosS * cosV
        val HV = if (cosV > 0) H else H - 6371 * (1 - sinV)
        var RV = 500 * cosV
        RV = (sqrt(RV * RV + 1001) - RV) * exp(-HV / 8.4)
        var MV = m1 * cosV
        MV = (sqrt(MV * MV + 3001) - MV) * exp(-HV / 1.2)
        val R0 = sqrt(RV * RS)
        val M0 = sqrt(MV * MS)
        val S1 =
            (1 - exp(-R1 * RV - M1 * MV)) / (7 * R1 * RV + M1 * MV) * exp(-O1 * OS - W1 / RV - W2)
        val S2 =
            (1 - exp(-R2 * RV - M2 * MV)) / (7 * R2 * RV + M2 * MV) * exp(-O2 * OS - W1 / RV - W2)
        val S3 =
            (1 - exp(-R3 * RV - M3 * MV)) / (7 * R3 * RV + M3 * MV) * exp(-O3 * OS - W1 / RV - W2)
        val RS1 = R * R1 * RV * S1 / (2 + R1 * R0)
        val RS2 = R * R2 * RV * S2 / (2 + R2 * R0)
        val RS3 = R * R3 * RV * S3 / (2 + R3 * R0)
        if (y < 0 || y == 0 && z < 2) {
            Z1 = RS1
            Z2 = RS2
            Z3 = RS3
            if (z > 1) return@forEach
        }
        val MS1 = M1 * MV * S1 * exp(-M1 * M0 / 6)
        val MS2 = M2 * MV * S2 * exp(-M2 * M0 / 6)
        val MS3 = M3 * MV * S3 * exp(-M3 * M0 / 6)
        val A1 = exp(-O1 * OS - M1 * MV / 6) * 2 / (2 + R1 * RV)
        val A2 = exp(-O2 * OS - M2 * MV / 6) * 2 / (2 + R2 * RV)
        val A3 = exp(-O3 * OS - M3 * MV / 6) * 2 / (2 + R3 * RV)
        val V2 = V1 * sin(VE + EH + RPD) / if (V0 < 0) 1 - cos(V0) else .0
        val dx = if (z != .0) 1.0 else x1 / 15.0 / (90 - VE / RPD)
        val x2 = 2 * x0 / x1
        var x = .0
        while (x < x1) {
            val VA = x * x2 - x0
            val G = sinSV * cos(VA - SA) + cosSV
            val P = 1 + (if (G > 0) d else g) * G * G
            var MP = 1 + g2 - 2 * g * G
            MP = MP0 / MP / sqrt(MP)
            val V = 1 + 1 / max(V2 + G, .0)
            val A = A0 * (1 - G) + .01
            var I1 = max((max(RS1 / V, Z1) + MP * (MS1 + A1 / A)) * P, .005)
            var I2 = max((max(RS2 / V, Z2) + MP * (MS2 + A2 / A)) * P, .009)
            var I3 = max((max(RS3 / V, Z3) + MP * (MS3 + A3 / A)) * P, .007)
            if (hd == 0) {
                I1 /= 1 + I1
                I2 /= 1 + I2
                I3 /= 1 + I3
            } else if (hd == 1) {
                I1 = if (I1 < .0031308) 12.92 * I1 else 1.055 * I1.pow(5 / 12) - .055
                I2 = if (I2 < .0031308) 12.92 * I2 else 1.055 * I2.pow(5 / 12) - .055
                I3 = if (I3 < .0031308) 12.92 * I3 else 1.055 * I3.pow(5 / 12) - .055
            }
            if (z != .0) result[x.toInt(), y] = Color.rgb(
                (I1 * B).toInt().coerceIn(0, 255),
                (I2 * B).toInt().coerceIn(0, 255),
                (I3 * B).toInt().coerceIn(0, 255)
            ) else result[
                    (x1 / 2 * (1 + fe * cos(VA))).toInt(), (y1 / 2 * (1 + fe * sin(VA))).toInt()
            ] = Color.rgb(
                (I1 * B).toInt().coerceIn(0, 255),
                (I2 * B).toInt().coerceIn(0, 255),
                (I3 * B).toInt().coerceIn(0, 255)
            )
            x += dx
        }
    }
    return result
//    if (!T) {
//        HTML("sclr", "Martian Sky");
//    } else if (HS >= 0) {
//        const E1 = exp(-O1 * OS - M1 * MS - R1 * RS);
//        const E2 = exp(-O2 * OS - M2 * MS - R2 * RS);
//        const E3 = exp(-O3 * OS - M3 * MS - R3 * RS);
//        ctx.fillStyle = "rgb(255," + (E2 / E1 * 255).toFixed() + "," + (E3 / E1 * 255).toFixed() + ")";
//        GetId("sclr").style.color = ctx.fillStyle;
//        HTML("sclr", "Sun Color = " + ctx.fillStyle.toUpperCase());
//    } else HTML("sclr", " ");
}

//fun showXY(userX: Int, userY: Int): String {
//    val dx = 2 * (userX + 1) / x1 - 1.0
//    val dy = 2 * (userY + 1) / y1 - 1.0
//    val x: Double
//    var y: Double
//    if (z != .0) {
//        x = atan2(dy, dx) / RPD
//        y = sqrt(dx * dx + dy * dy)
//        if (y > 1) return ""
//        y = 90 * (1 - y)
//    } else {
//        x = dx * x0 / RPD
//        y = (.5 - dy / 2) * y0 / RPD
//    }
//    return "Elev = $y; Azi = $x; Color = "
//}
