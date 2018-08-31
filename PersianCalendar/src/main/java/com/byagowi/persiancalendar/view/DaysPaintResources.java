package com.byagowi.persiancalendar.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.util.TypedValue;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.TypeFaceUtils;
import com.byagowi.persiancalendar.util.Utils;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

public class DaysPaintResources {
    @ColorInt
    final public int colorHoliday, colorTextHoliday, colorTextDay, colorPrimary, colorDayName, colorSelectDay;

    final public int halfEventBarWidth, appointmentYOffset, eventYOffset;
    final public int weekNumberTextSize, weekDaysInitialTextSize, arabicDigitsTextSize, persianDigitsTextSize;

    final public Paint textPaint, linePaint, selectedPaint, todayPaint;

    public DaysPaintResources(Context context) {
        Resources.Theme theme = context.getTheme();
        TypedValue value = new TypedValue();

        theme.resolveAttribute(R.attr.colorHoliday, value, true);
        colorHoliday = ContextCompat.getColor(context, value.resourceId);

        theme.resolveAttribute(R.attr.colorTextHoliday, value, true);
        colorTextHoliday = ContextCompat.getColor(context, value.resourceId);

        theme.resolveAttribute(R.attr.colorTextDay, value, true);
        colorTextDay = ContextCompat.getColor(context, value.resourceId);

        theme.resolveAttribute(R.attr.colorPrimary, value, true);
        colorPrimary = ContextCompat.getColor(context, value.resourceId);

        theme.resolveAttribute(R.attr.colorTextDayName, value, true);
        colorDayName = ContextCompat.getColor(context, value.resourceId);

        theme.resolveAttribute(R.attr.colorSelectDay, value, true);
        colorSelectDay = ContextCompat.getColor(context, value.resourceId);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        Resources resources = context.getResources();
        linePaint.setStrokeWidth(resources.getDimensionPixelSize(R.dimen.day_item_event_bar_thickness));

        todayPaint.setStyle(Paint.Style.STROKE);
        todayPaint.setStrokeWidth(resources.getDimensionPixelSize(R.dimen.day_item_today_indicator_thickness));
        theme.resolveAttribute(R.attr.colorCurrentDay, value, true);
        todayPaint.setColor(ContextCompat.getColor(context, value.resourceId));

        selectedPaint.setStyle(Paint.Style.FILL);
        selectedPaint.setColor(colorSelectDay);

        halfEventBarWidth = resources.getDimensionPixelSize(R.dimen.day_item_event_bar_width) / 2;
        eventYOffset = resources.getDimensionPixelSize(R.dimen.day_item_event_y_offset);
        appointmentYOffset = resources.getDimensionPixelSize(R.dimen.day_item_appointment_y_offset);
        weekNumberTextSize = resources.getDimensionPixelSize(R.dimen.day_item_week_number_text_size);
        weekDaysInitialTextSize = resources.getDimensionPixelSize(R.dimen.day_item_week_days_initial_text_size);
        arabicDigitsTextSize = resources.getDimensionPixelSize(R.dimen.day_item_arabic_digits_text_size);
        persianDigitsTextSize = resources.getDimensionPixelSize(R.dimen.day_item_persian_digits_text_size);

        textPaint.setTypeface(TypeFaceUtils.getCalendarFragmentFont(context));
    }
}
