package com.byagowi.persiancalendar.view.sunrisesunset;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */

public class SolarCalculator {
    final private Location location;
    final private TimeZone timeZone;

    public SolarCalculator(Location location, TimeZone timeZone) {
        this.location = location;
        this.timeZone = timeZone;
    }

    public String computeSunriseTime(Zen solarZen, Calendar date) {
        return getLocalTimeAsString(computeSolarEventTime(solarZen, date, true));
    }

    public Calendar computeSunriseCalendar(Zen solarZen, Calendar date) {
        return getLocalTimeAsCalendar(computeSolarEventTime(solarZen, date, true), date);
    }

    public String computeSunsetTime(Zen solarZen, Calendar date) {
        return getLocalTimeAsString(computeSolarEventTime(solarZen, date, false));
    }

    public Calendar computeSunsetCalendar(Zen solarZen, Calendar date) {
        return getLocalTimeAsCalendar(computeSolarEventTime(solarZen, date, false), date);
    }

    private BigDecimal computeSolarEventTime(Zen solarZen, Calendar date, boolean isSunrise) {
        date.setTimeZone(this.timeZone);
        BigDecimal longitudeHour = getLongitudeHour(date, isSunrise);

        BigDecimal meanAnomaly = getMeanAnomaly(longitudeHour);
        BigDecimal sunTrueLong = getSunTrueLongitude(meanAnomaly);
        BigDecimal cosineSunLocalHour = getCosineSunLocalHour(sunTrueLong, solarZen);
        if ((cosineSunLocalHour.doubleValue() < -1.0) || (cosineSunLocalHour.doubleValue() > 1.0)) {
            return null;
        }

        BigDecimal sunLocalHour = getSunLocalHour(cosineSunLocalHour, isSunrise);
        BigDecimal localMeanTime = getLocalMeanTime(sunTrueLong, longitudeHour, sunLocalHour);
        return getLocalTime(localMeanTime, date);
    }

    private BigDecimal getBaseLongitudeHour() {
        return divideBy(location.getLongitude(), BigDecimal.valueOf(15));
    }

    private BigDecimal getLongitudeHour(Calendar date, Boolean isSunrise) {
        int offset = 18;
        if (isSunrise) {
            offset = 6;
        }
        BigDecimal dividend = BigDecimal.valueOf(offset).subtract(getBaseLongitudeHour());
        BigDecimal addend = divideBy(dividend, BigDecimal.valueOf(24));
        BigDecimal longHour = getDayOfYear(date).add(addend);
        return setScale(longHour);
    }

    private BigDecimal getMeanAnomaly(BigDecimal longitudeHour) {
        BigDecimal meanAnomaly = multiplyBy(new BigDecimal("0.9856"), longitudeHour).subtract(new BigDecimal("3.289"));
        return setScale(meanAnomaly);
    }

    private BigDecimal getSunTrueLongitude(BigDecimal meanAnomaly) {
        BigDecimal sinMeanAnomaly = new BigDecimal(Math.sin(convertDegreesToRadians(meanAnomaly).doubleValue()));
        BigDecimal sinDoubleMeanAnomaly = new BigDecimal(Math.sin(multiplyBy(convertDegreesToRadians(meanAnomaly), BigDecimal.valueOf(2))
                .doubleValue()));

        BigDecimal firstPart = meanAnomaly.add(multiplyBy(sinMeanAnomaly, new BigDecimal("1.916")));
        BigDecimal secondPart = multiplyBy(sinDoubleMeanAnomaly, new BigDecimal("0.020")).add(new BigDecimal("282.634"));
        BigDecimal trueLongitude = firstPart.add(secondPart);

        if (trueLongitude.doubleValue() > 360) {
            trueLongitude = trueLongitude.subtract(BigDecimal.valueOf(360));
        }
        return setScale(trueLongitude);
    }

    private BigDecimal getRightAscension(BigDecimal sunTrueLong) {
        BigDecimal tanL = new BigDecimal(Math.tan(convertDegreesToRadians(sunTrueLong).doubleValue()));

        BigDecimal innerParent = multiplyBy(convertRadiansToDegrees(tanL), new BigDecimal("0.91764"));
        BigDecimal rightAscension = new BigDecimal(Math.atan(convertDegreesToRadians(innerParent).doubleValue()));
        rightAscension = setScale(convertRadiansToDegrees(rightAscension));

        if (rightAscension.doubleValue() < 0) {
            rightAscension = rightAscension.add(BigDecimal.valueOf(360));
        } else if (rightAscension.doubleValue() > 360) {
            rightAscension = rightAscension.subtract(BigDecimal.valueOf(360));
        }

        BigDecimal ninety = BigDecimal.valueOf(90);
        BigDecimal longitudeQuadrant = sunTrueLong.divide(ninety, 0, RoundingMode.FLOOR);
        longitudeQuadrant = longitudeQuadrant.multiply(ninety);

        BigDecimal rightAscensionQuadrant = rightAscension.divide(ninety, 0, RoundingMode.FLOOR);
        rightAscensionQuadrant = rightAscensionQuadrant.multiply(ninety);

        BigDecimal agenda = longitudeQuadrant.subtract(rightAscensionQuadrant);
        return divideBy(rightAscension.add(agenda), BigDecimal.valueOf(15));
    }

    private BigDecimal getCosineSunLocalHour(BigDecimal sunTrueLong, Zen zen) {
        BigDecimal sinSunDeclination = getSinOfSunDeclination(sunTrueLong);
        BigDecimal cosineSunDeclination = getCosineOfSunDeclination(sinSunDeclination);

        BigDecimal zenithInRads = convertDegreesToRadians(zen.degrees());
        BigDecimal cosineZenith = BigDecimal.valueOf(Math.cos(zenithInRads.doubleValue()));
        BigDecimal sinLatitude = BigDecimal.valueOf(Math.sin(convertDegreesToRadians(location.getLatitude()).doubleValue()));
        BigDecimal cosLatitude = BigDecimal.valueOf(Math.cos(convertDegreesToRadians(location.getLatitude()).doubleValue()));

        BigDecimal sinDeclinationTimesSinLat = sinSunDeclination.multiply(sinLatitude);
        BigDecimal dividend = cosineZenith.subtract(sinDeclinationTimesSinLat);
        BigDecimal divisor = cosineSunDeclination.multiply(cosLatitude);

        return setScale(divideBy(dividend, divisor));
    }

    private BigDecimal getSinOfSunDeclination(BigDecimal sunTrueLong) {
        BigDecimal sinTrueLongitude = BigDecimal.valueOf(Math.sin(convertDegreesToRadians(sunTrueLong).doubleValue()));
        BigDecimal sinOfDeclination = sinTrueLongitude.multiply(new BigDecimal("0.39782"));
        return setScale(sinOfDeclination);
    }

    private BigDecimal getCosineOfSunDeclination(BigDecimal sinSunDeclination) {
        BigDecimal arcSinOfSinDeclination = BigDecimal.valueOf(Math.asin(sinSunDeclination.doubleValue()));
        BigDecimal cosDeclination = BigDecimal.valueOf(Math.cos(arcSinOfSinDeclination.doubleValue()));
        return setScale(cosDeclination);
    }

    private BigDecimal getSunLocalHour(BigDecimal cosineSunLocalHour, Boolean isSunrise) {
        BigDecimal arcCosineOfCosineHourAngle = getArcCosineFor(cosineSunLocalHour);
        BigDecimal localHour = convertRadiansToDegrees(arcCosineOfCosineHourAngle);
        if (isSunrise) {
            localHour = BigDecimal.valueOf(360).subtract(localHour);
        }
        return divideBy(localHour, BigDecimal.valueOf(15));
    }

    private BigDecimal getLocalMeanTime(BigDecimal sunTrueLong, BigDecimal longitudeHour, BigDecimal sunLocalHour) {
        BigDecimal rightAscension = this.getRightAscension(sunTrueLong);
        BigDecimal innerParent = longitudeHour.multiply(new BigDecimal("0.06571"));
        BigDecimal localMeanTime = sunLocalHour.add(rightAscension).subtract(innerParent);
        localMeanTime = localMeanTime.subtract(new BigDecimal("6.622"));

        if (localMeanTime.doubleValue() < 0) {
            localMeanTime = localMeanTime.add(BigDecimal.valueOf(24));
        } else if (localMeanTime.doubleValue() > 24) {
            localMeanTime = localMeanTime.subtract(BigDecimal.valueOf(24));
        }
        return setScale(localMeanTime);
    }

    private BigDecimal getLocalTime(BigDecimal localMeanTime, Calendar date) {
        BigDecimal utcTime = localMeanTime.subtract(getBaseLongitudeHour());
        BigDecimal utcOffSet = getUTCOffSet(date);
        BigDecimal utcOffSetTime = utcTime.add(utcOffSet);
        return adjustForDST(utcOffSetTime, date);
    }

    private BigDecimal adjustForDST(BigDecimal localMeanTime, Calendar date) {
        BigDecimal localTime = localMeanTime;
        if (timeZone.inDaylightTime(date.getTime())) {
            localTime = localTime.add(BigDecimal.ONE);
        }
        if (localTime.doubleValue() > 24.0) {
            localTime = localTime.subtract(BigDecimal.valueOf(24));
        }
        return localTime;
    }

    private String getLocalTimeAsString(BigDecimal localTimeParam) {
        if (localTimeParam == null) {
            return "99:99";
        }

        BigDecimal localTime = localTimeParam;
        if (localTime.compareTo(BigDecimal.ZERO) < 0) {
            localTime = localTime.add(BigDecimal.valueOf(24.0D));
        }
        String[] timeComponents = localTime.toPlainString().split("\\.");
        int hour = Integer.parseInt(timeComponents[0]);

        BigDecimal minutes = new BigDecimal("0." + timeComponents[1]);
        minutes = minutes.multiply(BigDecimal.valueOf(60)).setScale(0, RoundingMode.HALF_EVEN);
        if (minutes.intValue() == 60) {
            minutes = BigDecimal.ZERO;
            hour += 1;
        }
        if (hour == 24) {
            hour = 0;
        }

        String minuteString = minutes.intValue() < 10 ? "0" + minutes.toPlainString() : minutes.toPlainString();
        String hourString = (hour < 10) ? "0" + String.valueOf(hour) : String.valueOf(hour);
        return hourString + ":" + minuteString;
    }

    private Calendar getLocalTimeAsCalendar(BigDecimal localTimeParam, Calendar date) {
        if (localTimeParam == null) {
            return null;
        }

        // Create a clone of the input calendar so we get locale/timezone information.
        Calendar resultTime = (Calendar) date.clone();

        BigDecimal localTime = localTimeParam;
        if (localTime.compareTo(BigDecimal.ZERO) < 0) {
            localTime = localTime.add(BigDecimal.valueOf(24.0D));
            resultTime.add(Calendar.HOUR_OF_DAY, -24);
        }
        String[] timeComponents = localTime.toPlainString().split("\\.");
        int hour = Integer.parseInt(timeComponents[0]);

        BigDecimal minutes = new BigDecimal("0." + timeComponents[1]);
        minutes = minutes.multiply(BigDecimal.valueOf(60)).setScale(0, RoundingMode.HALF_EVEN);
        if (minutes.intValue() == 60) {
            minutes = BigDecimal.ZERO;
            hour += 1;
        }
        if (hour == 24) {
            hour = 0;
        }

        // Set the local time
        resultTime.set(Calendar.HOUR_OF_DAY, hour);
        resultTime.set(Calendar.MINUTE, minutes.intValue());
        resultTime.set(Calendar.SECOND, 0);
        resultTime.set(Calendar.MILLISECOND, 0);
        resultTime.setTimeZone(date.getTimeZone());

        return resultTime;
    }

    private BigDecimal getDayOfYear(Calendar date) {
        return new BigDecimal(date.get(Calendar.DAY_OF_YEAR));
    }

    private BigDecimal getUTCOffSet(Calendar date) {
        BigDecimal offSetInMillis = new BigDecimal(date.get(Calendar.ZONE_OFFSET));
        return offSetInMillis.divide(new BigDecimal(3600000), new MathContext(2));
    }

    private BigDecimal getArcCosineFor(BigDecimal radians) {
        BigDecimal arcCosine = BigDecimal.valueOf(Math.acos(radians.doubleValue()));
        return setScale(arcCosine);
    }

    private BigDecimal convertRadiansToDegrees(BigDecimal radians) {
        return multiplyBy(radians, new BigDecimal(180 / Math.PI));
    }

    private BigDecimal convertDegreesToRadians(BigDecimal degrees) {
        return multiplyBy(degrees, BigDecimal.valueOf(Math.PI / 180.0));
    }

    private BigDecimal multiplyBy(BigDecimal multiplicand, BigDecimal multiplier) {
        return setScale(multiplicand.multiply(multiplier));
    }

    private BigDecimal divideBy(BigDecimal dividend, BigDecimal divisor) {
        return dividend.divide(divisor, 4, RoundingMode.HALF_EVEN);
    }

    private BigDecimal setScale(BigDecimal number) {
        return number.setScale(4, RoundingMode.HALF_EVEN);
    }
}
