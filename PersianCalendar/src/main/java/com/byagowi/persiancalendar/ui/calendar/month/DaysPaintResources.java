package com.byagowi.persiancalendar.ui.calendar.month;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Paint;
import android.util.TypedValue;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.utils.TypefaceUtils;
import com.byagowi.persiancalendar.utils.Utils;

import androidx.annotation.ColorInt;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

public class DaysPaintResources {
    @StyleRes
    final public int style;
    @ColorInt
    final int colorHoliday, colorHolidaySelected, colorTextHoliday, colorTextDay,
            colorTextDaySelected, colorTextToday, colorTextDayName, colorSelectDay, colorEventLine;
    final int weekNumberTextSize, weekDaysInitialTextSize, arabicDigitsTextSize, persianDigitsTextSize;
    final int halfEventBarWidth, appointmentYOffset, eventYOffset;
    final Paint textPaint, eventBarPaint, selectedPaint, todayPaint;

    public DaysPaintResources(Activity activity) {
        Resources.Theme theme = activity.getTheme();
        TypedValue value = new TypedValue();

        theme.resolveAttribute(R.attr.colorHoliday, value, true);
        colorHoliday = ContextCompat.getColor(activity, value.resourceId);

        theme.resolveAttribute(R.attr.colorHolidaySelected, value, true);
        colorHolidaySelected = ContextCompat.getColor(activity, value.resourceId);

        theme.resolveAttribute(R.attr.colorTextHoliday, value, true);
        colorTextHoliday = ContextCompat.getColor(activity, value.resourceId);

        theme.resolveAttribute(R.attr.colorTextDay, value, true);
        colorTextDay = ContextCompat.getColor(activity, value.resourceId);

        theme.resolveAttribute(R.attr.colorTextDaySelected, value, true);
        colorTextDaySelected = ContextCompat.getColor(activity, value.resourceId);

        theme.resolveAttribute(R.attr.colorTextToday, value, true);
        colorTextToday = ContextCompat.getColor(activity, value.resourceId);

        theme.resolveAttribute(R.attr.colorTextDayName, value, true);
        colorTextDayName = ContextCompat.getColor(activity, value.resourceId);

        theme.resolveAttribute(R.attr.colorEventLine, value, true);
        colorEventLine = ContextCompat.getColor(activity, value.resourceId);

        theme.resolveAttribute(R.attr.colorSelectDay, value, true);
        colorSelectDay = ContextCompat.getColor(activity, value.resourceId);

        style = Utils.getAppTheme();

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eventBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        Resources resources = activity.getResources();
        eventBarPaint.setStrokeWidth(resources.getDimensionPixelSize(R.dimen.day_item_event_bar_thickness));

        todayPaint.setStyle(Paint.Style.STROKE);
        todayPaint.setStrokeWidth(resources.getDimensionPixelSize(R.dimen.day_item_today_indicator_thickness));

        theme.resolveAttribute(R.attr.colorCurrentDay, value, true);
        todayPaint.setColor(ContextCompat.getColor(activity, value.resourceId));

        selectedPaint.setStyle(Paint.Style.FILL);
        selectedPaint.setColor(colorSelectDay);

        halfEventBarWidth = resources.getDimensionPixelSize(R.dimen.day_item_event_bar_width) / 2;
        eventYOffset = resources.getDimensionPixelSize(R.dimen.day_item_event_y_offset);
        appointmentYOffset = resources.getDimensionPixelSize(R.dimen.day_item_appointment_y_offset);
        weekNumberTextSize = resources.getDimensionPixelSize(R.dimen.day_item_week_number_text_size);
        weekDaysInitialTextSize = resources.getDimensionPixelSize(R.dimen.day_item_week_days_initial_text_size);
        arabicDigitsTextSize = resources.getDimensionPixelSize(R.dimen.day_item_arabic_digits_text_size);
        persianDigitsTextSize = resources.getDimensionPixelSize(R.dimen.day_item_persian_digits_text_size);

        textPaint.setTypeface(TypefaceUtils.getCalendarFragmentFont(activity));
    }
}
