package com.byagowi.persiancalendar;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import com.byagowi.persiancalendar.locale.CalendarStrings;
import com.github.praytimes.Clock;
import com.github.praytimes.Coordinate;
import com.github.praytimes.PrayTime;
import com.github.praytimes.PrayTimesCalculator;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Pray time helper. It is like an aspect for activity class somehow
 *
 * @author ebraminio
 */
class PrayTimeActivityHelper {

    private final Utils utils = Utils.getInstance();
    private final Activity activity;
    private final TextView prayTimeTextView;
    private Date date = new Date();

    public PrayTimeActivityHelper(Activity activity) {
        this.activity = activity;
        utils.loadLanguageFromSettings(activity);
        prayTimeTextView = (TextView) activity.findViewById(R.id.today_praytimes);
    }

    public void setDate(int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth);
        date = c.getTime();
    }

    public void fillPrayTime() {
        if (utils.getCoordinate(activity) == null) {
            return;
        }
        Coordinate coord = utils.getCoordinate(activity);

        PrayTimesCalculator ptc = new PrayTimesCalculator(
                utils.getCalculationMethod(activity));
        StringBuilder sb = new StringBuilder();
        Map<PrayTime, Clock> prayTimes = ptc.calculate(date, coord);

        char[] digits = utils.preferredDigits(activity);
        boolean clockIn24 = utils.clockIn24(activity);

        Context ctx = activity.getApplication();
        sb.append(utils.getString(CalendarStrings.IMSAK));
        sb.append(": ");
        sb.append(utils.getPersianFormattedClock(prayTimes.get(PrayTime.Imsak), digits, clockIn24));

        sb.append("\n");
        sb.append(utils.getString(CalendarStrings.SUNRISE));
        sb.append(": ");
        sb.append(utils.getPersianFormattedClock(prayTimes.get(PrayTime.Sunrise), digits, clockIn24));

        sb.append("\n");
        sb.append(utils.getString(CalendarStrings.DHUHR));
        sb.append(": ");
        sb.append(utils.getPersianFormattedClock(prayTimes.get(PrayTime.Dhuhr), digits, clockIn24));

        sb.append("\n");
        sb.append(utils.getString(CalendarStrings.ASR));
        sb.append(": ");
        sb.append(utils.getPersianFormattedClock(prayTimes.get(PrayTime.Asr), digits, clockIn24));

        sb.append("\n");
        sb.append(utils.getString(CalendarStrings.SUNSET));
        sb.append(": ");
        sb.append(utils.getPersianFormattedClock(prayTimes.get(PrayTime.Sunset), digits, clockIn24));

        sb.append("\n");
        sb.append(utils.getString(CalendarStrings.MAGHRIB));
        sb.append(": ");
        sb.append(utils.getPersianFormattedClock(prayTimes.get(PrayTime.Maghrib), digits, clockIn24));

        sb.append("\n");
        sb.append(utils.getString(CalendarStrings.ISHA));
        sb.append(": ");
        sb.append(utils.getPersianFormattedClock(prayTimes.get(PrayTime.Isha), digits, clockIn24));

        sb.append("\n");
        sb.append(utils.getString(CalendarStrings.MIDNIGHT));
        sb.append(": ");
        sb.append(utils.getPersianFormattedClock(prayTimes.get(PrayTime.Midnight), digits, clockIn24));

        utils.prepareTextView(prayTimeTextView);
        prayTimeTextView.setText(Utils.formatNumber(sb.toString(), digits));
    }

    public void clearInfo() {
        prayTimeTextView.setText("");
    }
}
