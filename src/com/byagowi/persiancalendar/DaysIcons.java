package com.byagowi.persiancalendar;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ebrahim Byagowi <ebrahim@byagowi.com>
 */
public class DaysIcons {
    private static DaysIcons ourInstance = new DaysIcons();

    public static DaysIcons getInstance() {
        return ourInstance;
    }

    private DaysIcons() {
    }

    Map<Integer, Integer> icons = new HashMap<>();

    public int getDayIconResource(int day) {
        if (icons.containsKey(day))
            return icons.get(day);

        int res = 0; // null here means static field
        try {
            res = R.drawable.class.getField("day" + day).getInt(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.e("com.byagowi.calendar", "No such field is available");
            return 0;
        }

        icons.put(day, res);
        return res;
    }
}
