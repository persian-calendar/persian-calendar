package com.byagowi.persiancalendar;

import android.util.Log;
import android.util.SparseIntArray;

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

    SparseIntArray icons = new SparseIntArray();

    public int getDayIconResource(int day) {
        if (icons.get(day, -1) != -1)
            return icons.get(day);

        int res = 0; // null here means static field
        try {
            res = R.drawable.class.getField("day" + day).getInt(null);
        } catch (IllegalAccessException e) {
            Log.e("com.byagowi.calendar", "No such field is available");
            return 0;
        } catch (NoSuchFieldException e) {
            Log.e("com.byagowi.calendar", "No such field is available");
            return 0;
        }

        icons.put(day, res);
        return res;
    }
}
