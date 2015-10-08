package com.byagowi.persiancalendar;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

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
        sb.append(ctx.getString(R.string.imsak));
        sb.append(": ");
        sb.append(utils.getPersianFormattedClock(prayTimes.get(PrayTime.Imsak), digits, clockIn24));

        sb.append("\n");
        sb.append(ctx.getString(R.string.sunrise));
        sb.append(": ");
        sb.append(utils.getPersianFormattedClock(prayTimes.get(PrayTime.Sunrise), digits, clockIn24));

        sb.append("\n");
        sb.append(ctx.getString(R.string.dhuhr));
        sb.append(": ");
        sb.append(utils.getPersianFormattedClock(prayTimes.get(PrayTime.Dhuhr), digits, clockIn24));

        sb.append("\n");
        sb.append(ctx.getString(R.string.asr));
        sb.append(": ");
        sb.append(utils.getPersianFormattedClock(prayTimes.get(PrayTime.Asr), digits, clockIn24));

        sb.append("\n");
        sb.append(ctx.getString(R.string.sunset));
        sb.append(": ");
        sb.append(utils.getPersianFormattedClock(prayTimes.get(PrayTime.Sunset), digits, clockIn24));

        sb.append("\n");
        sb.append(ctx.getString(R.string.maghrib));
        sb.append(": ");
        sb.append(utils.getPersianFormattedClock(prayTimes.get(PrayTime.Maghrib), digits, clockIn24));

        sb.append("\n");
        sb.append(ctx.getString(R.string.isha));
        sb.append(": ");
        sb.append(utils.getPersianFormattedClock(prayTimes.get(PrayTime.Isha), digits, clockIn24));

        sb.append("\n");
        sb.append(ctx.getString(R.string.midnight));
        sb.append(": ");
        sb.append(utils.getPersianFormattedClock(prayTimes.get(PrayTime.Midnight), digits, clockIn24));

        utils.prepareTextView(prayTimeTextView);
        prayTimeTextView.setText(Utils.formatNumber(sb.toString(), digits));
    }

    public void clearInfo() {
        prayTimeTextView.setText("");
    }
}
