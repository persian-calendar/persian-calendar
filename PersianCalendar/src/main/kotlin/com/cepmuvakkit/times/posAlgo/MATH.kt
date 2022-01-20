package com.cepmuvakkit.times.posAlgo

import kotlin.math.floor
import kotlin.math.sqrt

object MATH {
    // Square root from 3
    const val SQRT3 = 1.732050807568877294

    /**
     * ln(0.5) constant
     */
    const val LOGdiv2 = -0.6931471805599453094

    fun acos(x: Double): Double {
        val f = asin(x)
        return if (f.isNaN()) f else Math.PI / 2 - f
    }

    fun asin(x: Double): Double {
        if (x < -1.0 || x > 1.0) {
            return Double.NaN
        }
        if (x == -1.0) {
            return -Math.PI / 2
        }
        return if (x == 1.0) {
            Math.PI / 2
        } else atan(x / sqrt(1 - x * x))
    }

    fun atan(x: Double): Double {
        var x = x
        var signChange = false
        var Invert = false
        var sp = 0
        var a: Double
        // check up the sign change
        if (x < 0.0) {
            x = -x
            signChange = true
        }
        // check up the invertation
        if (x > 1.0) {
            x = 1 / x
            Invert = true
        }
        // process shrinking the domain until x<PI/12
        while (x > Math.PI / 12) {
            sp++
            a = x + SQRT3
            a = 1 / a
            x *= SQRT3
            x -= 1
            x *= a
        }
        // calculation core
        val x2: Double = x * x
        a = x2 + 1.4087812
        a = 0.55913709 / a
        a += 0.60310579
        a -= x2 * 0.05160454
        a *= x
        // process until sp=0
        while (sp > 0) {
            a += Math.PI / 6
            sp--
        }
        // invertation took place
        if (Invert) {
            a = Math.PI / 2 - a
        }
        // sign change took place
        if (signChange) {
            a = -a
        }
        //
        return a
    }

    fun pow(x: Double, y: Double): Double {
        if (y == 0.0) return 1.0
        if (y == 1.0) return x
        if (x == 0.0) return 0.0
        if (x == 1.0) return 1.0
        //
        val l = floor(y).toLong()
        val integerValue = y == l.toDouble()
        //
        return if (integerValue) {
            var neg = false
            if (y < 0.0) {
                neg = true
            }
            //
            var result = x
            (1 until if (neg) -l else l).forEach { i ->
                result *= x
            }
            //
            if (neg) {
                1.0 / result
            } else {
                result
            }
        } else {
            if (x > 0.0) {
                exp(y * log(x))
            } else {
                Double.NaN
            }
        }
    }

    fun exp(x: Double): Double {
        var x = x
        if (x == 0.0) {
            return 1.0
        }
        //
        var f = 1.0
        val d: Long = 1
        var k: Double
        val isless = x < 0.0
        if (isless) {
            x = -x
        }
        k = x / d
        //
        (2..49).forEach { i ->
            f += k
            k = k * x / i
        }
        //
        return if (isless) 1 / f else f
    }

    private fun _log(x: Double): Double {
        var x = x
        if (x <= 0.0) return Double.NaN
        //
        var f = 0.0
        //
        var appendix = 0
        while (x > 0.0 && x <= 1.0) {
            x *= 2.0
            appendix++
        }
        //
        x /= 2.0
        appendix--
        //
        val y1 = x - 1.0
        var y2 = x + 1.0
        val y = y1 / y2
        //
        var k = y
        y2 = k * y
        //
        run {
            var i: Long = 1
            while (i < 50) {
                f += k / i
                k *= y2
                i += 2
            }
        }
        //
        f *= 2.0
        (0 until appendix).forEach { i -> f += LOGdiv2 }
        //
        return f
    }

    fun log(x: Double): Double {
        var x = x
        if (x <= 0.0) return Double.NaN
        //
        if (x == 1.0) return 0.0
        // Argument of _log must be (0; 1]
        if (x > 1.0) {
            x = 1 / x
            return -_log(x)
        }
        //
        return _log(x)
    }
}
