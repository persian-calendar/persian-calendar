package com.byagowi.persiancalendar.entity;

import android.content.Context;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.praytimes.Clock;
import com.byagowi.persiancalendar.praytimes.PrayTimes;
import com.byagowi.persiancalendar.util.Utils;

import java.util.Arrays;
import java.util.List;

import static java.util.concurrent.TimeUnit.MINUTES;

public class Widget4x2OwghatEntity {
    private int mIndexOfNextOwghat = -1;
    private Clock[] mClocks = new Clock[5];
    private int[] mOwghatTitle;
    private String mRemainingTime = "";

    public Widget4x2OwghatEntity(Context context, PrayTimes prayTimes, boolean isShia, Clock currentClock) {
        //TODO We may want to show Imsak only in Ramadan

        int[] shia1 = {R.string.imsak, R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.sunset};
        //int[] shia2 = {R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.sunset, R.string.maghrib};
        int[] shia3 = {R.string.sunrise, R.string.dhuhr, R.string.sunset, R.string.maghrib, R.string.midnight};

        int[] sunni1 = {R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.asr, R.string.sunset};
        //int[] sunni2 = {R.string.sunrise, R.string.dhuhr, R.string.asr, R.string.sunset, R.string.maghrib};
        int[] sunni3 = {R.string.dhuhr, R.string.asr, R.string.sunset, R.string.maghrib, R.string.isha};

        List<Clock> clocks = isShia ? Arrays.asList(
                prayTimes.getImsakClock(), //0
                prayTimes.getFajrClock(), //1
                prayTimes.getSunriseClock(), //2
                prayTimes.getDhuhrClock(), //3
                prayTimes.getSunsetClock(), //4
                prayTimes.getMaghribClock(), //5
                prayTimes.getMidnightClock() //6
        ) : Arrays.asList(
                prayTimes.getFajrClock(), //0
                prayTimes.getSunriseClock(), //1
                prayTimes.getDhuhrClock(), //2
                prayTimes.getAsrClock(), //3
                prayTimes.getSunsetClock(), //4
                prayTimes.getMaghribClock(), //5
                prayTimes.getIshaClock() //6
        );

        int indexOfNextOwghat = getClockIndex(clocks, currentClock);
        //Log.d(TAG, "indexOfNextOwghat is " + indexOfNextOwghat);

        switch (indexOfNextOwghat) {
            case 0:
            case 1:
            case 2:
                clocks = clocks.subList(0, 5);
                clocks.toArray(mClocks);
                if (isShia) mOwghatTitle = shia1;
                else mOwghatTitle = sunni1;
                break;
            /*case 3:
                clocks = clocks.subList(1, 6);
                clocks.toArray(mClocks);
                if (isShia) mOwghatTtitle = shia2;
                else mOwghatTtitle = sunni2;
                break;*/
            default:
                clocks = clocks.subList(2, 7);
                clocks.toArray(mClocks);
                if (isShia) mOwghatTitle = shia3;
                else mOwghatTitle = sunni3;
        }

        if (indexOfNextOwghat != -1) {
            mIndexOfNextOwghat = getClockIndex(clocks, currentClock);
            mRemainingTime = getRemainingString(context, mClocks[mIndexOfNextOwghat], currentClock);
        }
    }

    public int getIndexOfNextOwghat() {
        return mIndexOfNextOwghat;
    }

    public Clock[] getClocks() {
        return mClocks;
    }

    public int[] getTitle() {
        return mOwghatTitle;
    }

    public String getRemainingTime() {
        return mRemainingTime;
    }

    private int getClockIndex(List<Clock> clocks, Clock clock) {
        int index = 0;
        int next = -1;
        for (Clock c : clocks) {
            if (c.toInt() >= clock.toInt()) {
                next = index;
                break;
            }
            index++;
        }
        return next;
    }

    private String getRemainingString(Context context, Clock startDate, Clock endDate) {
        int difference = Math.abs(endDate.toInt() - startDate.toInt());

        int hrs = (int) (MINUTES.toHours(difference) % 24);
        int min = (int) (MINUTES.toMinutes(difference) % 60);

        if (hrs == 0) {
            return String.format(context.getString(R.string.n_minutes), Utils.formatNumber(min));
        }
        if (min == 0) {
            return String.format(context.getString(R.string.n_hours), Utils.formatNumber(hrs));
        }
        return String.format(context.getString(R.string.n_minutes_and_hours), Utils.formatNumber(hrs), Utils.formatNumber(min));
    }
}