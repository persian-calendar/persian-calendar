package com.byagowi.persiancalendar.view.sunrisesunset;

import java.math.BigDecimal;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */

public class Zen {
    private final BigDecimal degrees;

    public static final Zen ASTRONOMICAL = new Zen(108);
    public static final Zen NAUTICAL = new Zen(102);
    public static final Zen CIVIL = new Zen(96);
    public static final Zen OFFICIAL = new Zen(90.8333);

    private Zen(double degrees) {
        this.degrees = BigDecimal.valueOf(degrees);
    }

    public BigDecimal degrees() {
        return degrees;
    }
}
