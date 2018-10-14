package com.byagowi.persiancalendar.util;

import android.content.Context;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.calendar.IslamicDate;
import com.byagowi.persiancalendar.calendar.PersianDate;

import androidx.annotation.StringRes;

// Based on Mehdi's work
public class AstronomicalUtils {
    @StringRes
    final private static int[] YEARS_NAME = {
            R.string.year10, R.string.year11, R.string.year12,
            R.string.year1, R.string.year2, R.string.year3,
            R.string.year4, R.string.year5, R.string.year6,
            R.string.year7, R.string.year8, R.string.year9
    };
    @StringRes
    final private static int[] ZODIAC_MONTHS = {
            R.string.empty,
            R.string.aries, R.string.taurus, R.string.gemini,
            R.string.cancer, R.string.leo, R.string.virgo,
            R.string.libra, R.string.scorpio, R.string.sagittarius,
            R.string.capricorn, R.string.aquarius, R.string.pisces
    };
    @StringRes
    final private static int[] ZODIAC_MONTHS_EMOJI = {
            R.string.empty,
            R.string.aries_emoji, R.string.taurus_emoji, R.string.gemini_emoji,
            R.string.cancer_emoji, R.string.leo_emoji, R.string.virgo_emoji,
            R.string.libra_emoji, R.string.scorpio_emoji, R.string.sagittarius_emoji,
            R.string.capricorn_emoji, R.string.aquarius_emoji, R.string.pisces_emoji
    };

    public static boolean isMoonInScorpio(PersianDate persianDate, IslamicDate islamicDate) {
        int res = (int) (((((float) (islamicDate.getDayOfMonth() + 1) * 12.2f) +
                (persianDate.getDayOfMonth() + 1)) / 30.f) + persianDate.getMonth());
        if (res > 12) res -= 12;
        return res == 8;
    }

    static public String getZodiacInfo(Context context, long jdn, boolean withEmoji) {
        if (!Utils.isAstronomicalFeaturesEnabled()) return "";

        PersianDate persianDate = new PersianDate(jdn);
        IslamicDate islamicDate = new IslamicDate(jdn);
        return String.format("%s: %s\n%s: %s %s\n%s",
                context.getString(R.string.year_name),
                context.getString(YEARS_NAME[persianDate.getYear() % 12]),
                context.getString(R.string.zodiac),
                withEmoji ? context.getString(ZODIAC_MONTHS_EMOJI[persianDate.getMonth()]) : "",
                context.getString(ZODIAC_MONTHS[persianDate.getMonth()]),
                isMoonInScorpio(persianDate, islamicDate)
                        ? context.getString(R.string.moonInScorpio) : "").trim();
    }
}
