package com.byagowi.persiancalendar.entity;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.praytimes.Clock;
import com.byagowi.persiancalendar.praytimes.PrayTimes;

import java.util.Arrays;
import java.util.List;

import static java.util.concurrent.TimeUnit.MINUTES;

public class Widget4x2OwghatEntity {
    private final String TAG = Widget4x2OwghatEntity.class.getSimpleName();

    private int mIndexOfNextOwghat = -1;
    private Clock[] mClocks = new Clock[5];
    private int[] mOwghatTtitle;
    private String mRemainingTime = "";

    public Widget4x2OwghatEntity(PrayTimes prayTimes, boolean isShia, Clock currentClock) {

        //TODO We may want to show Imsak only in Ramadan for Shia

        List<Clock> shiaClocks = Arrays.asList(
                prayTimes.getImsakClock(), //0
                prayTimes.getFajrClock(), //1
                prayTimes.getSunriseClock(), //2
                prayTimes.getDhuhrClock(), //3
                prayTimes.getSunsetClock(), //4
                prayTimes.getMaghribClock(), //5
                prayTimes.getMidnightClock() //6
        );

        int[] shia1 = {R.string.imsak, R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.sunset};
        //int[] shia2 = {R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.sunset, R.string.maghrib};
        int[] shia3 = {R.string.sunrise, R.string.dhuhr, R.string.sunset, R.string.maghrib, R.string.midnight};

        List<Clock> sunniClocks = Arrays.asList(
                prayTimes.getFajrClock(), //0
                prayTimes.getSunriseClock(), //1
                prayTimes.getDhuhrClock(), //2
                prayTimes.getAsrClock(), //3
                prayTimes.getSunsetClock(), //4
                prayTimes.getMaghribClock(), //5
                prayTimes.getIshaClock() //6
        );

        int[] sunni1 = {R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.asr, R.string.sunset};
        //int[] sunni2 = {R.string.sunrise, R.string.dhuhr, R.string.asr, R.string.sunset, R.string.maghrib};
        int[] sunni3 = {R.string.dhuhr, R.string.asr, R.string.sunset, R.string.maghrib, R.string.isha};

        List<Clock> clocks;
        if (isShia)
            clocks = shiaClocks;
        else
            clocks = sunniClocks;

        int indexOfNextOwghat = getClockIndex(clocks, currentClock);
        //Log.d(TAG, "indexOfNextOwghat is " + indexOfNextOwghat);

        switch (indexOfNextOwghat) {
            case 0:
            case 1:
            case 2:
                clocks = clocks.subList(0, 5);
                clocks.toArray(mClocks);
                if (isShia) mOwghatTtitle = shia1;
                else mOwghatTtitle = sunni1;
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
                if (isShia) mOwghatTtitle = shia3;
                else mOwghatTtitle = sunni3;
        }

        //Log.d(TAG, "clocks " + clocks.toString());

        if (indexOfNextOwghat != -1) {
            mIndexOfNextOwghat = getClockIndex(clocks, currentClock);
            mRemainingTime = getRemaningString(mClocks[mIndexOfNextOwghat], currentClock);
        }
        //Log.d(TAG, "mIndexOfNextOwghat is " + mIndexOfNextOwghat);
    }

    public int getIndexOfNextOwghat() {
        return mIndexOfNextOwghat;
    }

    public Clock[] getClocks() {
        return mClocks;
    }

    public int[] getTitle() {
        return mOwghatTtitle;
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

    private String getRemaningString(Clock startDate, Clock endDate) {

        int different = Math.abs(endDate.toInt() - startDate.toInt());

        //Log.d(TAG, "startDate : " + startDate);
        //Log.d(TAG, "endDate : " + endDate);
        //Log.d(TAG, "different : " + different);

        MINUTES.toHours(different);
        int hrs = (int) (MINUTES.toHours(different) % 24);
        int min = (int) (MINUTES.toMinutes(different) % 60);

        if (hrs == 0) {
            return String.format("%d دقیقه", min);
        }
        if (min == 0) {
            return String.format("%d ساعت", hrs);
        }
        if (hrs == 0 && min == 0) {
            return "";
        }
        return String.format("%d ساعت و %d دقیقه", hrs, min);
    }
}