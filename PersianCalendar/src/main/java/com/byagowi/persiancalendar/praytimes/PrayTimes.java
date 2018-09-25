package com.byagowi.persiancalendar.praytimes;

public class PrayTimes {
    private final double imsak, fajr, sunrise, dhuhr, asr, sunset, maghrib, isha, midnight;

    public PrayTimes(double imsak, double fajr, double sunrise, double dhuhr,
                     double asr, double sunset, double maghrib, double isha, double midnight) {
        this.imsak = imsak;
        this.fajr = fajr;
        this.sunrise = sunrise;
        this.dhuhr = dhuhr;
        this.asr = asr;
        this.sunset = sunset;
        this.maghrib = maghrib;
        this.isha = isha;
        this.midnight = midnight;
    }

    double getImsak() {
        return imsak;
    }

    public Clock getImsakClock() {
        return Clock.fromDouble(imsak);
    }

    double getFajr() {
        return fajr;
    }

    public Clock getFajrClock() {
        return Clock.fromDouble(fajr);
    }

    double getSunrise() {
        return sunrise;
    }

    public Clock getSunriseClock() {
        return Clock.fromDouble(sunrise);
    }

    double getDhuhr() {
        return dhuhr;
    }

    public Clock getDhuhrClock() {
        return Clock.fromDouble(dhuhr);
    }

    double getAsr() {
        return asr;
    }

    public Clock getAsrClock() {
        return Clock.fromDouble(asr);
    }

    double getSunset() {
        return sunset;
    }

    public Clock getSunsetClock() {
        return Clock.fromDouble(sunset);
    }

    double getMaghrib() {
        return maghrib;
    }

    public Clock getMaghribClock() {
        return Clock.fromDouble(maghrib);
    }

    double getIsha() {
        return isha;
    }

    public Clock getIshaClock() {
        return Clock.fromDouble(isha);
    }

    double getMidnight() {
        return midnight;
    }

    public Clock getMidnightClock() {
        return Clock.fromDouble(midnight);
    }
}
