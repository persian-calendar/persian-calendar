package com.byagowi.persiancalendar.utils

/*
 This is separated/ported from https://github.com/cosinekitty/astronomy

 MIT License

 Copyright (c) 2019-2022 Don Cross <cosinekitty@gmail.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

import com.cepmuvakkit.times.posAlgo.Equatorial
import com.cepmuvakkit.times.posAlgo.Horizontal
import io.github.persiancalendar.praytimes.Coordinates
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private val iauRows = listOf(
    IauRow(
        nals0 = 0, nals1 = 0, nals2 = 0, nals3 = 0, nals4 = 1,
        cls0 = -172064161, cls1 = -174666, cls2 = 33386, cls3 = 92052331, cls4 = 9086, cls5 = 15377
    ),
    IauRow(
        nals0 = 0, nals1 = 0, nals2 = 2, nals3 = -2, nals4 = 2,
        cls0 = -13170906, cls1 = -1675, cls2 = -13696, cls3 = 5730336, cls4 = -3015, cls5 = -4587
    ),
    IauRow(
        nals0 = 0, nals1 = 0, nals2 = 2, nals3 = 0, nals4 = 2,
        cls0 = -2276413, cls1 = -234, cls2 = 2796, cls3 = 978459, cls4 = -485, cls5 = 1374
    ),
    IauRow(
        nals0 = 0, nals1 = 0, nals2 = 0, nals3 = 0, nals4 = 2,
        cls0 = 2074554, cls1 = 207, cls2 = -698, cls3 = -897492, cls4 = 470, cls5 = -291
    ),
    IauRow(
        nals0 = 0, nals1 = 1, nals2 = 0, nals3 = 0, nals4 = 0,
        cls0 = 1475877, cls1 = -3633, cls2 = 11817, cls3 = 73871, cls4 = -184, cls5 = -1924
    ),
    IauRow(
        nals0 = 0, nals1 = 1, nals2 = 2, nals3 = -2, nals4 = 2,
        cls0 = -516821, cls1 = 1226, cls2 = -524, cls3 = 224386, cls4 = -677, cls5 = -174
    ),
    IauRow(
        nals0 = 1, nals1 = 0, nals2 = 0, nals3 = 0, nals4 = 0,
        cls0 = 711159, cls1 = 73, cls2 = -872, cls3 = -6750, cls4 = 0, cls5 = 358
    ),
    IauRow(
        nals0 = 0, nals1 = 0, nals2 = 2, nals3 = 0, nals4 = 1,
        cls0 = -387298, cls1 = -367, cls2 = 380, cls3 = 200728, cls4 = 18, cls5 = 318
    ),
    IauRow(
        nals0 = 1, nals1 = 0, nals2 = 2, nals3 = 0, nals4 = 2,
        cls0 = -301461, cls1 = -36, cls2 = 816, cls3 = 129025, cls4 = -63, cls5 = 367
    ),
    IauRow(
        nals0 = 0, nals1 = -1, nals2 = 2, nals3 = -2, nals4 = 2,
        cls0 = 215829, cls1 = -494, cls2 = 111, cls3 = -95929, cls4 = 299, cls5 = 132
    ),
    IauRow(
        nals0 = 0, nals1 = 0, nals2 = 2, nals3 = -2, nals4 = 1,
        cls0 = 128227, cls1 = 137, cls2 = 181, cls3 = -68982, cls4 = -9, cls5 = 39
    ),
    IauRow(
        nals0 = -1, nals1 = 0, nals2 = 2, nals3 = 0, nals4 = 2,
        cls0 = 123457, cls1 = 11, cls2 = 19, cls3 = -53311, cls4 = 32, cls5 = -4
    ),
    IauRow(
        nals0 = -1, nals1 = 0, nals2 = 0, nals3 = 2, nals4 = 0,
        cls0 = 156994, cls1 = 10, cls2 = -168, cls3 = -1235, cls4 = 0, cls5 = 82
    ),
    IauRow(
        nals0 = 1, nals1 = 0, nals2 = 0, nals3 = 0, nals4 = 1,
        cls0 = 63110, cls1 = 63, cls2 = 27, cls3 = -33228, cls4 = 0, cls5 = -9
    ),
    IauRow(
        nals0 = -1, nals1 = 0, nals2 = 0, nals3 = 0, nals4 = 1,
        cls0 = -57976, cls1 = -63, cls2 = -189, cls3 = 31429, cls4 = 0, cls5 = -75
    ),
    IauRow(
        nals0 = -1, nals1 = 0, nals2 = 2, nals3 = 2, nals4 = 2,
        cls0 = -59641, cls1 = -11, cls2 = 149, cls3 = 25543, cls4 = -11, cls5 = 66
    ),
    IauRow(
        nals0 = 1, nals1 = 0, nals2 = 2, nals3 = 0, nals4 = 1,
        cls0 = -51613, cls1 = -42, cls2 = 129, cls3 = 26366, cls4 = 0, cls5 = 78
    ),
    IauRow(
        nals0 = -2, nals1 = 0, nals2 = 2, nals3 = 0, nals4 = 1,
        cls0 = 45893, cls1 = 50, cls2 = 31, cls3 = -24236, cls4 = -10, cls5 = 20
    ),
    IauRow(
        nals0 = 0, nals1 = 0, nals2 = 0, nals3 = 2, nals4 = 0,
        cls0 = 63384, cls1 = 11, cls2 = -150, cls3 = -1220, cls4 = 0, cls5 = 29
    ),
    IauRow(
        nals0 = 0, nals1 = 0, nals2 = 2, nals3 = 2, nals4 = 2,
        cls0 = -38571, cls1 = -1, cls2 = 158, cls3 = 16452, cls4 = -11, cls5 = 68
    ),
    IauRow(
        nals0 = 0, nals1 = -2, nals2 = 2, nals3 = -2, nals4 = 2,
        cls0 = 32481, cls1 = 0, cls2 = 0, cls3 = -13870, cls4 = 0, cls5 = 0
    ),
    IauRow(
        nals0 = -2, nals1 = 0, nals2 = 0, nals3 = 2, nals4 = 0,
        cls0 = -47722, cls1 = 0, cls2 = -18, cls3 = 477, cls4 = 0, cls5 = -25
    ),
    IauRow(
        nals0 = 2, nals1 = 0, nals2 = 2, nals3 = 0, nals4 = 2,
        cls0 = -31046, cls1 = -1, cls2 = 131, cls3 = 13238, cls4 = -11, cls5 = 59
    ),
    IauRow(
        nals0 = 1, nals1 = 0, nals2 = 2, nals3 = -2, nals4 = 2,
        cls0 = 28593, cls1 = 0, cls2 = -1, cls3 = -12338, cls4 = 10, cls5 = -3
    ),
    IauRow(
        nals0 = -1, nals1 = 0, nals2 = 2, nals3 = 0, nals4 = 1,
        cls0 = 20441, cls1 = 21, cls2 = 10, cls3 = -10758, cls4 = 0, cls5 = -3
    ),
    IauRow(
        nals0 = 2, nals1 = 0, nals2 = 0, nals3 = 0, nals4 = 0,
        cls0 = 29243, cls1 = 0, cls2 = -74, cls3 = -609, cls4 = 0, cls5 = 13
    ),
    IauRow(
        nals0 = 0, nals1 = 0, nals2 = 2, nals3 = 0, nals4 = 0,
        cls0 = 25887, cls1 = 0, cls2 = -66, cls3 = -550, cls4 = 0, cls5 = 11
    ),
    IauRow(
        nals0 = 0, nals1 = 1, nals2 = 0, nals3 = 0, nals4 = 1,
        cls0 = -14053, cls1 = -25, cls2 = 79, cls3 = 8551, cls4 = -2, cls5 = -45
    ),
    IauRow(
        nals0 = -1, nals1 = 0, nals2 = 0, nals3 = 2, nals4 = 1,
        cls0 = 15164, cls1 = 10, cls2 = 11, cls3 = -8001, cls4 = 0, cls5 = -1
    ),
    IauRow(
        nals0 = 0, nals1 = 2, nals2 = 2, nals3 = -2, nals4 = 2,
        cls0 = -15794, cls1 = 72, cls2 = -16, cls3 = 6850, cls4 = -42, cls5 = -5
    ),
    IauRow(
        nals0 = 0, nals1 = 0, nals2 = -2, nals3 = 2, nals4 = 0,
        cls0 = 21783, cls1 = 0, cls2 = 13, cls3 = -167, cls4 = 0, cls5 = 13
    ),
    IauRow(
        nals0 = 1, nals1 = 0, nals2 = 0, nals3 = -2, nals4 = 1,
        cls0 = -12873, cls1 = -10, cls2 = -37, cls3 = 6953, cls4 = 0, cls5 = -14
    ),
    IauRow(
        nals0 = 0, nals1 = -1, nals2 = 0, nals3 = 0, nals4 = 1,
        cls0 = -12654, cls1 = 11, cls2 = 63, cls3 = 6415, cls4 = 0, cls5 = 26
    ),
    IauRow(
        nals0 = -1, nals1 = 0, nals2 = 2, nals3 = 2, nals4 = 1,
        cls0 = -10204, cls1 = 0, cls2 = 25, cls3 = 5222, cls4 = 0, cls5 = 15
    ),
    IauRow(
        nals0 = 0, nals1 = 2, nals2 = 0, nals3 = 0, nals4 = 0,
        cls0 = 16707, cls1 = -85, cls2 = -10, cls3 = 168, cls4 = -1, cls5 = 10
    ),
    IauRow(
        nals0 = 1, nals1 = 0, nals2 = 2, nals3 = 2, nals4 = 2,
        cls0 = -7691, cls1 = 0, cls2 = 44, cls3 = 3268, cls4 = 0, cls5 = 19
    ),
    IauRow(
        nals0 = -2, nals1 = 0, nals2 = 2, nals3 = 0, nals4 = 0,
        cls0 = -11024, cls1 = 0, cls2 = -14, cls3 = 104, cls4 = 0, cls5 = 2
    ),
    IauRow(
        nals0 = 0, nals1 = 1, nals2 = 2, nals3 = 0, nals4 = 2,
        cls0 = 7566, cls1 = -21, cls2 = -11, cls3 = -3250, cls4 = 0, cls5 = -5
    ),
    IauRow(
        nals0 = 0, nals1 = 0, nals2 = 2, nals3 = 2, nals4 = 1,
        cls0 = -6637, cls1 = -11, cls2 = 25, cls3 = 3353, cls4 = 0, cls5 = 14
    ),
    IauRow(
        nals0 = 0, nals1 = -1, nals2 = 2, nals3 = 0, nals4 = 2,
        cls0 = -7141, cls1 = 21, cls2 = 8, cls3 = 3070, cls4 = 0, cls5 = 4
    ),
    IauRow(
        nals0 = 0, nals1 = 0, nals2 = 0, nals3 = 2, nals4 = 1,
        cls0 = -6302, cls1 = -11, cls2 = 2, cls3 = 3272, cls4 = 0, cls5 = 4
    ),
    IauRow(
        nals0 = 1, nals1 = 0, nals2 = 2, nals3 = -2, nals4 = 1,
        cls0 = 5800, cls1 = 10, cls2 = 2, cls3 = -3045, cls4 = 0, cls5 = -1
    ),
    IauRow(
        nals0 = 2, nals1 = 0, nals2 = 2, nals3 = -2, nals4 = 2,
        cls0 = 6443, cls1 = 0, cls2 = -7, cls3 = -2768, cls4 = 0, cls5 = -4
    ),
    IauRow(
        nals0 = -2, nals1 = 0, nals2 = 0, nals3 = 2, nals4 = 1,
        cls0 = -5774, cls1 = -11, cls2 = -15, cls3 = 3041, cls4 = 0, cls5 = -5
    ),
    IauRow(
        nals0 = 2, nals1 = 0, nals2 = 2, nals3 = 0, nals4 = 1,
        cls0 = -5350, cls1 = 0, cls2 = 21, cls3 = 2695, cls4 = 0, cls5 = 12
    ),
    IauRow(
        nals0 = 0, nals1 = -1, nals2 = 2, nals3 = -2, nals4 = 1,
        cls0 = -4752, cls1 = -11, cls2 = -3, cls3 = 2719, cls4 = 0, cls5 = -3
    ),
    IauRow(
        nals0 = 0, nals1 = 0, nals2 = 0, nals3 = -2, nals4 = 1,
        cls0 = -4940, cls1 = -11, cls2 = -21, cls3 = 2720, cls4 = 0, cls5 = -9
    ),
    IauRow(
        nals0 = -1, nals1 = -1, nals2 = 0, nals3 = 2, nals4 = 0,
        cls0 = 7350, cls1 = 0, cls2 = -8, cls3 = -51, cls4 = 0, cls5 = 4
    ),
    IauRow(
        nals0 = 2, nals1 = 0, nals2 = 0, nals3 = -2, nals4 = 1,
        cls0 = 4065, cls1 = 0, cls2 = 6, cls3 = -2206, cls4 = 0, cls5 = 1
    ),
    IauRow(
        nals0 = 1, nals1 = 0, nals2 = 0, nals3 = 2, nals4 = 0,
        cls0 = 6579, cls1 = 0, cls2 = -24, cls3 = -199, cls4 = 0, cls5 = 2
    ),
    IauRow(
        nals0 = 0, nals1 = 1, nals2 = 2, nals3 = -2, nals4 = 1,
        cls0 = 3579, cls1 = 0, cls2 = 5, cls3 = -1900, cls4 = 0, cls5 = 1
    ),
    IauRow(
        nals0 = 1, nals1 = -1, nals2 = 0, nals3 = 0, nals4 = 0,
        cls0 = 4725, cls1 = 0, cls2 = -6, cls3 = -41, cls4 = 0, cls5 = 3
    ),
    IauRow(
        nals0 = -2, nals1 = 0, nals2 = 2, nals3 = 0, nals4 = 2,
        cls0 = -3075, cls1 = 0, cls2 = -2, cls3 = 1313, cls4 = 0, cls5 = -1
    ),
    IauRow(
        nals0 = 3, nals1 = 0, nals2 = 2, nals3 = 0, nals4 = 2,
        cls0 = -2904, cls1 = 0, cls2 = 15, cls3 = 1233, cls4 = 0, cls5 = 7
    ),
    IauRow(
        nals0 = 0, nals1 = -1, nals2 = 0, nals3 = 2, nals4 = 0,
        cls0 = 4348, cls1 = 0, cls2 = -10, cls3 = -81, cls4 = 0, cls5 = 2
    ),
    IauRow(
        nals0 = 1, nals1 = -1, nals2 = 2, nals3 = 0, nals4 = 2,
        cls0 = -2878, cls1 = 0, cls2 = 8, cls3 = 1232, cls4 = 0, cls5 = 4
    ),
    IauRow(
        nals0 = 0, nals1 = 0, nals2 = 0, nals3 = 1, nals4 = 0,
        cls0 = -4230, cls1 = 0, cls2 = 5, cls3 = -20, cls4 = 0, cls5 = -2
    ),
    IauRow(
        nals0 = -1, nals1 = -1, nals2 = 2, nals3 = 2, nals4 = 2,
        cls0 = -2819, cls1 = 0, cls2 = 7, cls3 = 1207, cls4 = 0, cls5 = 3
    ),
    IauRow(
        nals0 = -1, nals1 = 0, nals2 = 2, nals3 = 0, nals4 = 0,
        cls0 = -4056, cls1 = 0, cls2 = 5, cls3 = 40, cls4 = 0, cls5 = -2
    ),
    IauRow(
        nals0 = 0, nals1 = -1, nals2 = 2, nals3 = 2, nals4 = 2,
        cls0 = -2647, cls1 = 0, cls2 = 11, cls3 = 1129, cls4 = 0, cls5 = 5
    ),
    IauRow(
        nals0 = -2, nals1 = 0, nals2 = 0, nals3 = 0, nals4 = 1,
        cls0 = -2294, cls1 = 0, cls2 = -10, cls3 = 1266, cls4 = 0, cls5 = -4
    ),
    IauRow(
        nals0 = 1, nals1 = 1, nals2 = 2, nals3 = 0, nals4 = 2,
        cls0 = 2481, cls1 = 0, cls2 = -7, cls3 = -1062, cls4 = 0, cls5 = -3
    ),
    IauRow(
        nals0 = 2, nals1 = 0, nals2 = 0, nals3 = 0, nals4 = 1,
        cls0 = 2179, cls1 = 0, cls2 = -2, cls3 = -1129, cls4 = 0, cls5 = -2
    ),
    IauRow(
        nals0 = -1, nals1 = 1, nals2 = 0, nals3 = 1, nals4 = 0,
        cls0 = 3276, cls1 = 0, cls2 = 1, cls3 = -9, cls4 = 0, cls5 = 0
    ),
    IauRow(
        nals0 = 1, nals1 = 1, nals2 = 0, nals3 = 0, nals4 = 0,
        cls0 = -3389, cls1 = 0, cls2 = 5, cls3 = 35, cls4 = 0, cls5 = -2
    ),
    IauRow(
        nals0 = 1, nals1 = 0, nals2 = 2, nals3 = 0, nals4 = 0,
        cls0 = 3339, cls1 = 0, cls2 = -13, cls3 = -107, cls4 = 0, cls5 = 1
    ),
    IauRow(
        nals0 = -1, nals1 = 0, nals2 = 2, nals3 = -2, nals4 = 1,
        cls0 = -1987, cls1 = 0, cls2 = -6, cls3 = 1073, cls4 = 0, cls5 = -2
    ),
    IauRow(
        nals0 = 1, nals1 = 0, nals2 = 0, nals3 = 0, nals4 = 2,
        cls0 = -1981, cls1 = 0, cls2 = 0, cls3 = 854, cls4 = 0, cls5 = 0
    ),
    IauRow(
        nals0 = -1, nals1 = 0, nals2 = 0, nals3 = 1, nals4 = 0,
        cls0 = 4026, cls1 = 0, cls2 = -353, cls3 = -553, cls4 = 0, cls5 = -139
    ),
    IauRow(
        nals0 = 0, nals1 = 0, nals2 = 2, nals3 = 1, nals4 = 2,
        cls0 = 1660, cls1 = 0, cls2 = -5, cls3 = -710, cls4 = 0, cls5 = -2
    ),
    IauRow(
        nals0 = -1, nals1 = 0, nals2 = 2, nals3 = 4, nals4 = 2,
        cls0 = -1521, cls1 = 0, cls2 = 9, cls3 = 647, cls4 = 0, cls5 = 4
    ),
    IauRow(
        nals0 = -1, nals1 = 1, nals2 = 0, nals3 = 1, nals4 = 1,
        cls0 = 1314, cls1 = 0, cls2 = 0, cls3 = -700, cls4 = 0, cls5 = 0
    ),
    IauRow(
        nals0 = 0, nals1 = -2, nals2 = 2, nals3 = -2, nals4 = 1,
        cls0 = -1283, cls1 = 0, cls2 = 0, cls3 = 672, cls4 = 0, cls5 = 0
    ),
    IauRow(
        nals0 = 1, nals1 = 0, nals2 = 2, nals3 = 2, nals4 = 1,
        cls0 = -1331, cls1 = 0, cls2 = 8, cls3 = 663, cls4 = 0, cls5 = 4
    ),
    IauRow(
        nals0 = -2, nals1 = 0, nals2 = 2, nals3 = 2, nals4 = 2,
        cls0 = 1383, cls1 = 0, cls2 = -2, cls3 = -594, cls4 = 0, cls5 = -2
    ),
    IauRow(
        nals0 = -1, nals1 = 0, nals2 = 0, nals3 = 0, nals4 = 2,
        cls0 = 1405, cls1 = 0, cls2 = 4, cls3 = -610, cls4 = 0, cls5 = 2
    ),
    IauRow(
        nals0 = 1, nals1 = 1, nals2 = 2, nals3 = -2, nals4 = 2,
        cls0 = 1290, cls1 = 0, cls2 = 0, cls3 = -556, cls4 = 0, cls5 = 0
    )
)

private class IauRow(
    val nals0: Int, val nals1: Int, val nals2: Int, val nals3: Int, val nals4: Int,
    val cls0: Int, val cls1: Int, val cls2: Int, val cls3: Int, val cls4: Int, val cls5: Int
)

private const val ASEC360 = 1296000.0
private const val ASEC2RAD = 4.848136811095359935899141e-6
private const val PI2 = 2 * Math.PI

private fun iau2000b_psi(tt: Double): Double {
    /* Adapted from the NOVAS C 3.1 function of the same name. */
    val t = tt / 36525.0
    val el = ((485868.249036 + t * 1717915923.2178) % ASEC360) * ASEC2RAD
    val elp = ((1287104.79305 + t * 129596581.0481) % ASEC360) * ASEC2RAD
    val f = ((335779.526232 + t * 1739527262.8478) % ASEC360) * ASEC2RAD
    val d = ((1072260.70369 + t * 1602961601.2090) % ASEC360) * ASEC2RAD
    val om = ((450160.398036 - t * 6962890.5431) % ASEC360) * ASEC2RAD
    var dp = .0
    var de = .0
    for (i in 76 downTo 0) {
        val arg =
            (iauRows[i].nals0 * el + iauRows[i].nals1 * elp + iauRows[i].nals2 * f + iauRows[i].nals3 * d + iauRows[i].nals4 * om) % PI2
        val sarg = sin(arg)
        val carg = cos(arg)
        dp += (iauRows[i].cls0 + iauRows[i].cls1 * t) * sarg + iauRows[i].cls2 * carg
        de += (iauRows[i].cls3 + iauRows[i].cls4 * t) * carg + iauRows[i].cls5 * sarg
    }
    return -0.000135 + (dp * 1.0e-7)
}

private fun meanObliq(tt: Double): Double {
    val t = tt / 36525.0
    val asec = ((((-0.0000000434 * t
            - 0.000000576) * t
            + 0.00200340) * t
            - 0.0001831) * t
            - 46.836769) * t + 84381.406
    return asec / 3600.0
}

private fun e_tilt_ee(tt: Double): Double {
    val psi = iau2000b_psi(tt)
    val mobl = meanObliq(tt)
    // val tobl = mobl + time.eps / 3600.0
    return psi * cos(Math.toRadians(mobl)) / 15.0
    //earth_tilt_t(time.tt, time.psi, time.eps, ee, mobl, tobl)
}

private val origin = GregorianCalendar(TimeZone.getTimeZone("UTC")).also {
    it.clear()
    it.set(2000, Calendar.JANUARY, 1, 12, 0, 0)
}

private const val DAYS_PER_TROPICAL_YEAR = 365.24217

private fun DeltaT_EspenakMeeus(ut: Double): Double {
    /*
        Fred Espenak writes about Delta-T generically here:
        https://eclipse.gsfc.nasa.gov/SEhelp/deltaT.html
        https://eclipse.gsfc.nasa.gov/SEhelp/deltat2004.html

        He provides polynomial approximations for distant years here:
        https://eclipse.gsfc.nasa.gov/SEhelp/deltatpoly2004.html

        They start with a year value 'y' such that y=2000 corresponds
        to the UTC Date 15-January-2000. Convert difference in days
        to mean tropical years.
     */
    val u: Double
    val u2: Double
    val u3: Double
    val u4: Double
    val u5: Double
    val u6: Double
    val u7: Double
    val y: Double = 2000 + (ut - 14) / DAYS_PER_TROPICAL_YEAR
    if (y < -500) {
        u = (y - 1820) / 100
        return -20 + 32 * u * u
    }
    if (y < 500) {
        u = y / 100
        u2 = u * u
        u3 = u * u2
        u4 = u2 * u2
        u5 = u2 * u3
        u6 = u3 * u3
        return 10583.6 - 1014.41 * u + 33.78311 * u2 - 5.952053 * u3 - 0.1798452 * u4 + 0.022174192 * u5 + 0.0090316521 * u6
    }
    if (y < 1600) {
        u = (y - 1000) / 100
        u2 = u * u
        u3 = u * u2
        u4 = u2 * u2
        u5 = u2 * u3
        u6 = u3 * u3
        return 1574.2 - 556.01 * u + 71.23472 * u2 + 0.319781 * u3 - 0.8503463 * u4 - 0.005050998 * u5 + 0.0083572073 * u6
    }
    if (y < 1700) {
        u = y - 1600
        u2 = u * u
        u3 = u * u2
        return 120 - 0.9808 * u - 0.01532 * u2 + u3 / 7129.0
    }
    if (y < 1800) {
        u = y - 1700
        u2 = u * u
        u3 = u * u2
        u4 = u2 * u2
        return 8.83 + 0.1603 * u - 0.0059285 * u2 + 0.00013336 * u3 - u4 / 1174000
    }
    if (y < 1860) {
        u = y - 1800
        u2 = u * u
        u3 = u * u2
        u4 = u2 * u2
        u5 = u2 * u3
        u6 = u3 * u3
        u7 = u3 * u4
        return 13.72 - 0.332447 * u + 0.0068612 * u2 + 0.0041116 * u3 - 0.00037436 * u4 + 0.0000121272 * u5 - 0.0000001699 * u6 + 0.000000000875 * u7
    }
    if (y < 1900) {
        u = y - 1860
        u2 = u * u
        u3 = u * u2
        u4 = u2 * u2
        u5 = u2 * u3
        return 7.62 + 0.5737 * u - 0.251754 * u2 + 0.01680668 * u3 - 0.0004473624 * u4 + u5 / 233174
    }
    if (y < 1920) {
        u = y - 1900
        u2 = u * u
        u3 = u * u2
        u4 = u2 * u2
        return -2.79 + 1.494119 * u - 0.0598939 * u2 + 0.0061966 * u3 - 0.000197 * u4
    }
    if (y < 1941) {
        u = y - 1920
        u2 = u * u
        u3 = u * u2
        return 21.20 + 0.84493 * u - 0.076100 * u2 + 0.0020936 * u3
    }
    if (y < 1961) {
        u = y - 1950
        u2 = u * u
        u3 = u * u2
        return 29.07 + 0.407 * u - u2 / 233 + u3 / 2547
    }
    if (y < 1986) {
        u = y - 1975
        u2 = u * u
        u3 = u * u2
        return 45.45 + 1.067 * u - u2 / 260 - u3 / 718
    }
    if (y < 2005) {
        u = y - 2000
        u2 = u * u
        u3 = u * u2
        u4 = u2 * u2
        u5 = u2 * u3
        return 63.86 + 0.3345 * u - 0.060374 * u2 + 0.0017275 * u3 + 0.000651814 * u4 + 0.00002373599 * u5
    }
    if (y < 2050) {
        u = y - 2000
        return 62.92 + 0.32217 * u + 0.005589 * u * u
    }
    if (y < 2150) {
        u = (y - 1820) / 100
        return -20.0 + 32.0 * u * u - 0.5628 * (2150 - y)
    }

    /* all years after 2150 */u = (y - 1820) / 100
    return -20 + 32 * u * u
}

private fun terrestrialTime(ut: Double) = ut + DeltaT_EspenakMeeus(ut) / 86400.0

private fun siderealTime(time: GregorianCalendar): Double {
    val ut = (time.timeInMillis - origin.timeInMillis) / (24 * 3600 * 1000.0)
    val tt = terrestrialTime(ut)
    val t = tt / 36525
    /* Replace with eqeq=0 to get GMST instead of GAST (if we ever need it) */
    val eqeq = 15.0 * e_tilt_ee(tt)
    val theta = era(ut)
    val st = (eqeq + 0.014506 + ((((-0.0000000368 * t
            - 0.000029956) * t
            - 0.00000044) * t
            + 1.3915817) * t
            + 4612.156534) * t)
    var gst = ((st / 3600.0 + theta) % 360.0) / 15.0
    if (gst < 0.0) gst += 24.0
    return gst // return sidereal hours in the half-open range [0, 24).
}

private fun era(ut: Double): Double /* Earth Rotation Angle */ {
    val thet1 = 0.7790572732640 + 0.00273781191135448 * ut
    val thet3 = ut % 1.0
    var theta = 360.0 * ((thet1 + thet3) % 1.0)
    if (theta < 0.0) theta += 360.0
    return theta
}

private fun spin(angle: Double, pos: DoubleArray): DoubleArray {
    val angr = Math.toRadians(angle)
    val cosang = cos(angr)
    val sinang = sin(angr)
    return doubleArrayOf(
        +cosang * pos[0] + sinang * pos[1],
        -sinang * pos[0] + cosang * pos[1],
        pos[2]
    )
}

fun rotationEqdHor(time: GregorianCalendar, observer: Coordinates): List<DoubleArray> {
    val sinlat = sin(Math.toRadians(observer.latitude))
    val coslat = cos(Math.toRadians(observer.latitude))
    val sinlon = sin(Math.toRadians(observer.longitude))
    val coslon = cos(Math.toRadians(observer.longitude))
    val uze = doubleArrayOf(coslat * coslon, coslat * sinlon, sinlat)
    val une = doubleArrayOf(-sinlat * coslon, -sinlat * sinlon, coslat)
    val uwe = doubleArrayOf(sinlon, -coslon, .0)
    // Multiply sidereal hours by -15 to convert to degrees and flip eastward
    // rotation of the Earth to westward apparent movement of objects with time.
    val angle = -15 * siderealTime(time)
    val uz = spin(angle, uze)
    val un = spin(angle, une)
    val uw = spin(angle, uwe)
    val rot = List(3) { DoubleArray(3) { .0 } }
    rot[0][0] = un[0]; rot[1][0] = un[1]; rot[2][0] = un[2]
    rot[0][1] = uw[0]; rot[1][1] = uw[1]; rot[2][1] = uw[2]
    rot[0][2] = uz[0]; rot[1][2] = uz[1]; rot[2][2] = uz[2]
    return rot
}

private fun pivot(rotation: List<DoubleArray>, axis: Int, angle: Double): List<DoubleArray> {
    val radians = Math.toRadians(angle)
    val c = cos(radians)
    val s = sin(radians)
    /*
        We need to maintain the "right-hand" rule, no matter which
        axis was selected. That means we pick (i, j, k) axis order
        such that the following vector cross product is satisfied:
        i x j = k
    */
    val i = (axis + 1) % 3
    val j = (axis + 2) % 3
    val k = axis
    val rot = List(3) { DoubleArray(3) { .0 } }
    rot[i][i] = c * rotation[i][i] - s * rotation[i][j]
    rot[i][j] = s * rotation[i][i] + c * rotation[i][j]
    rot[i][k] = rotation[i][k]
    rot[j][i] = c * rotation[j][i] - s * rotation[j][j]
    rot[j][j] = s * rotation[j][i] + c * rotation[j][j]
    rot[j][k] = rotation[j][k]
    rot[k][i] = c * rotation[k][i] - s * rotation[k][j]
    rot[k][j] = s * rotation[k][i] + c * rotation[k][j]
    rot[k][k] = rotation[k][k]
    return rot
}

private fun rotateVector(rotation: List<DoubleArray>, vector: DoubleArray) = doubleArrayOf(
    rotation[0][0] * vector[0] + rotation[1][0] * vector[1] + rotation[2][0] * vector[2],
    rotation[0][1] * vector[0] + rotation[1][1] * vector[1] + rotation[2][1] * vector[2],
    rotation[0][2] * vector[0] + rotation[1][2] * vector[1] + rotation[2][2] * vector[2]
)

/** Result is degrees counterclockwise from up */
fun lunarSunlitTilt(
    sunEquatorialVector: DoubleArray, moonHorizontal: Horizontal, time: GregorianCalendar,
    observer: Coordinates
): Double {
    // Get the rotation matrix that converts equatorial to horizontal coordintes for this place and time.
    var rot = rotationEqdHor(time, observer)
    // Modify the rotation matrix in two steps:
    // First, rotate the orientation so we are facing the Moon's azimuth.
    // We do this by pivoting around the zenith axis.
    // Horizontal axes are: 0 = north, 1 = west, 2 = zenith.
    // Tricky: because the pivot angle increases counterclockwise, and azimuth
    // increases clockwise, we undo the azimuth by adding the positive value.
    rot = pivot(rot, 2, moonHorizontal.azimuth)
    // Second, pivot around the leftward axis to bring the Moon to the camera's altitude level.
    // From the point of view of the leftward axis, looking toward the camera,
    // adding the angle is the correct sense for subtracting the altitude.
    rot = pivot(rot, 1, moonHorizontal.altitude)
    val vec = rotateVector(rot, sunEquatorialVector)
    // Calculate the tilt angle of the sunlit side, as seen by the camera.
    // The x-axis is now pointing directly at the object, z is up in the camera image, y is to the left.
    return Math.toDegrees(atan2(vec[2], vec[1]))
}

