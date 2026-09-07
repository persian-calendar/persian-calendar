/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.byagowi.persiancalendar.shared

internal val G_COEFF = arrayOf(
    doubleArrayOf(0.0),
    doubleArrayOf(-29404.5, -1450.7),
    doubleArrayOf(-2500.0, 2982.0, 1676.8),
    doubleArrayOf(1363.9, -2381.0, 1236.2, 525.7),
    doubleArrayOf(903.1, 809.4, 86.2, -309.4, 47.9),
    doubleArrayOf(-234.4, 363.1, 187.8, -140.7, -151.2, 13.7),
    doubleArrayOf(65.9, 65.6, 73.0, -121.5, -36.2, 13.5, -64.7),
    doubleArrayOf(80.6, -76.8, -8.3, 56.5, 15.8, 6.4, -7.2, 9.8),
    doubleArrayOf(23.6, 9.8, -17.5, -0.4, -21.1, 15.3, 13.7, -16.5, -0.3),
    doubleArrayOf(5.0, 8.2, 2.9, -1.4, -1.1, -13.3, 1.1, 8.9, -9.3, -11.9),
    doubleArrayOf(-1.9, -6.2, -0.1, 1.7, -0.9, 0.6, -0.9, 1.9, 1.4, -2.4, -3.9),
    doubleArrayOf(3.0, -1.4, -2.5, 2.4, -0.9, 0.3, -0.7, -0.1, 1.4, -0.6, 0.2, 3.1),
    doubleArrayOf(-2.0, -0.1, 0.5, 1.3, -1.2, 0.7, 0.3, 0.5, -0.2, -0.5, 0.1, -1.1, -0.3)
)

internal val H_COEFF = arrayOf(
    doubleArrayOf(0.0),
    doubleArrayOf(0.0, 4652.9),
    doubleArrayOf(0.0, -2991.6, -734.8),
    doubleArrayOf(0.0, -82.2, 241.8, -542.9),
    doubleArrayOf(0.0, 282.0, -158.4, 199.8, -350.1),
    doubleArrayOf(0.0, 47.7, 208.4, -121.3, 32.2, 99.1),
    doubleArrayOf(0.0, -19.1, 25.0, 52.7, -64.4, 9.0, 68.1),
    doubleArrayOf(0.0, -51.4, -16.8, 2.3, 23.5, -2.2, -27.2, -1.9),
    doubleArrayOf(0.0, 8.4, -15.3, 12.8, -11.8, 14.9, 3.6, -6.9, 2.8),
    doubleArrayOf(0.0, -23.3, 11.1, 9.8, -5.1, -6.2, 7.8, 0.4, -1.5, 9.7),
    doubleArrayOf(0.0, 3.4, -0.2, 3.5, 4.8, -8.6, -0.1, -4.2, -3.4, -0.1, -8.8),
    doubleArrayOf(0.0, 0.0, 2.6, -0.5, -0.4, 0.6, -0.2, -1.7, -1.6, -3.0, -2.0, -2.6),
    doubleArrayOf(0.0, -1.2, 0.5, 1.3, -1.8, 0.1, 0.7, -0.1, 0.6, 0.2, -0.9, 0.0, 0.5)
)

internal val DELTA_G = arrayOf(
    doubleArrayOf(0.0),
    doubleArrayOf(6.7, 7.7),
    doubleArrayOf(-11.5, -7.1, -2.2),
    doubleArrayOf(2.8, -6.2, 3.4, -12.2),
    doubleArrayOf(-1.1, -1.6, -6.0, 5.4, -5.5),
    doubleArrayOf(-0.3, 0.6, -0.7, 0.1, 1.2, 1.0),
    doubleArrayOf(-0.6, -0.4, 0.5, 1.4, -1.4, 0.0, 0.8),
    doubleArrayOf(-0.1, -0.3, -0.1, 0.7, 0.2, -0.5, -0.8, 1.0),
    doubleArrayOf(-0.1, 0.1, -0.1, 0.5, -0.1, 0.4, 0.5, 0.0, 0.4),
    doubleArrayOf(-0.1, -0.2, 0.0, 0.4, -0.3, 0.0, 0.3, 0.0, 0.0, -0.4),
    doubleArrayOf(0.0, 0.0, 0.0, 0.2, -0.1, -0.2, 0.0, -0.1, -0.2, -0.1, 0.0),
    doubleArrayOf(0.0, -0.1, 0.0, 0.0, 0.0, -0.1, 0.0, 0.0, -0.1, -0.1, -0.1, -0.1),
    doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.1)
)

internal val DELTA_H = arrayOf(
    doubleArrayOf(0.0),
    doubleArrayOf(0.0, -25.1),
    doubleArrayOf(0.0, -30.2, -23.9),
    doubleArrayOf(0.0, 5.7, -1.0, 1.1),
    doubleArrayOf(0.0, 0.2, 6.9, 3.7, -5.6),
    doubleArrayOf(0.0, 0.1, 2.5, -0.9, 3.0, 0.5),
    doubleArrayOf(0.0, 0.1, -1.8, -1.4, 0.9, 0.1, 1.0),
    doubleArrayOf(0.0, 0.5, 0.6, -0.7, -0.2, -1.2, 0.2, 0.3),
    doubleArrayOf(0.0, -0.3, 0.7, -0.2, 0.5, -0.3, -0.5, 0.4, 0.1),
    doubleArrayOf(0.0, -0.3, 0.2, -0.4, 0.4, 0.1, 0.0, -0.2, 0.5, 0.2),
    doubleArrayOf(0.0, 0.0, 0.1, -0.3, 0.1, -0.2, 0.1, 0.0, -0.1, 0.2, 0.0),
    doubleArrayOf(0.0, 0.0, 0.1, 0.0, 0.2, 0.0, 0.0, 0.1, 0.0, -0.1, 0.0, 0.0),
    doubleArrayOf(0.0, 0.0, 0.0, -0.1, 0.1, 0.0, 0.0, 0.0, 0.1, 0.0, 0.0, 0.0, -0.1)
)
