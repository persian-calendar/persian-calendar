package com.cepmuvakkit.times.posAlgo;

public class MATH {
    // Square root from 3

    final static public double SQRT3 = 1.732050807568877294;
    /** ln(0.5) constant */
    final static public double LOGdiv2 = -0.6931471805599453094;

    static public int round(float a) {
        return (int) Math.floor(a + 0.5f);
    }

    static public long round(double a) {
        return (long) Math.floor(a + 0.5d);
    }

    static public double acos(double x) {
        double f = asin(x);
        if (f == Double.NaN) {
            return f;
        }
        return Math.PI / 2 - f;
    }

    static public double asin(double x) {
        if (x < -1. || x > 1.) {
            return Double.NaN;
        }
        if (x == -1.) {
            return -Math.PI / 2;
        }
        if (x == 1) {
            return Math.PI / 2;
        }
        return atan(x / Math.sqrt(1 - x * x));
    }

    static public double atan(double x) {
        boolean signChange = false;
        boolean Invert = false;
        int sp = 0;
        double x2, a;
        // check up the sign change
        if (x < 0.) {
            x = -x;
            signChange = true;
        }
        // check up the invertation
        if (x > 1.) {
            x = 1 / x;
            Invert = true;
        }
        // process shrinking the domain until x<PI/12
        while (x > Math.PI / 12) {
            sp++;
            a = x + SQRT3;
            a = 1 / a;
            x = x * SQRT3;
            x = x - 1;
            x = x * a;
        }
        // calculation core
        x2 = x * x;
        a = x2 + 1.4087812;
        a = 0.55913709 / a;
        a = a + 0.60310579;
        a = a - (x2 * 0.05160454);
        a = a * x;
        // process until sp=0
        while (sp > 0) {
            a = a + Math.PI / 6;
            sp--;
        }
        // invertation took place
        if (Invert) {
            a = Math.PI / 2 - a;
        }
        // sign change took place
        if (signChange) {
            a = -a;
        }
        //
        return a;
    }

    static public double atan2(double y, double x) {
        // if x=y=0
        if (y == 0. && x == 0.) {
            return 0.;
        }
        // if x>0 atan(y/x)
        if (x > 0.) {
            return atan(y / x);
        }
        // if x<0 sign(y)*(pi - atan(|y/x|))
        if (x < 0.) {
            if (y < 0.) {
                return -(Math.PI - atan(y / x));
            } else {
                return Math.PI - atan(-y / x);
            }
        }
        // if x=0 y!=0 sign(y)*pi/2
        if (y < 0.) {
            return -Math.PI / 2.;
        } else {
            return Math.PI / 2.;
        }
    }

    static public double frac(double x) {
        return x - Math.floor(x);
    }

    static public double pow(double x, double y) {
        if (y == 0.) {
            return 1.;
        }
        if (y == 1.) {
            return x;
        }
        if (x == 0.) {
            return 0.;
        }
        if (x == 1.) {
            return 1.;
        }
        //
        long l = (long) Math.floor(y);
        boolean integerValue = (y == (double) l);
        //
        if (integerValue) {
            boolean neg = false;
            if (y < 0.) {
                neg = true;
            }
            //
            double result = x;
            for (long i = 1; i < (neg ? -l : l); i++) {
                result = result * x;
            }
            //
            if (neg) {
                return 1. / result;
            } else {
                return result;
            }
        } else {
            if (x > 0.) {
                return exp(y * log(x));
            } else {
                return Double.NaN;
            }
        }
    }

    static public double exp(double x) {
        if (x == 0.) {
            return 1.;
        }
        //
        double f = 1;
        long d = 1;
        double k;
        boolean isless = (x < 0.);
        if (isless) {
            x = -x;
        }
        k = x / d;
        //
        for (long i = 2; i < 50; i++) {
            f = f + k;
            k = k * x / i;
        }
        //
        if (isless) {
            return 1 / f;
        } else {
            return f;
        }
    }

    static private double _log(double x) {
        if (!(x > 0.)) {
            return Double.NaN;
        }
        //
        double f = 0.0;
        //
        int appendix = 0;
        while (x > 0.0 && x <= 1.0) {
            x *= 2.0;
            appendix++;
        }
        //
        x /= 2.0;
        appendix--;
        //
        double y1 = x - 1.;
        double y2 = x + 1.;
        double y = y1 / y2;
        //
        double k = y;
        y2 = k * y;
        //
        for (long i = 1; i < 50; i += 2) {
            f += k / i;
            k *= y2;
        }
        //
        f *= 2.0;
        for (int i = 0; i < appendix; i++) {
            f += LOGdiv2;
        }
        //
        return f;
    }

    static public double log(double x) {
        if (!(x > 0.)) {
            return Double.NaN;
        }
        //
        if (x == 1.0) {
            return 0.0;
        }
        // Argument of _log must be (0; 1]
        if (x > 1.) {
            x = 1 / x;
            return -_log(x);
        }
        //
        return _log(x);
    }

    static public double Frac(double x) {
        return x - Math.floor(x);
    }
}
