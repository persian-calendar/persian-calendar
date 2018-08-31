package com.byagowi.persiancalendar.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import com.byagowi.persiancalendar.util.Utils;

public class ItemDayView extends View {
    private DaysPaintResources resource;

    public ItemDayView(Context context, DaysPaintResources resource) {
        super(context);
        this.resource = resource;
    }

    private Rect bounds = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        if (selected) {
            canvas.drawCircle(width / 2, height / 2, height / 2 - 5,
                    resource.selectedPaint);
        }

        if (today) {
            canvas.drawCircle(width / 2, height / 2, height / 2 - 5,
                    resource.todayPaint);
        }

        int color;
        if (isNumber) {
            if (selected) {
                color = holiday ? resource.colorTextHoliday : resource.colorPrimary;
            } else {
                color = holiday ? resource.colorHoliday : resource.colorTextDay;
            }
        } else {
            color = resource.colorDayName;
        }

        // TODO: Better to not change resource's paint objects, but for now
        resource.textPaint.setColor(color);
        resource.textPaint.setTextSize(textSize);
        resource.linePaint.setColor(selected ? color : resource.colorSelectDay);

        if (hasEvent) {
            canvas.drawLine(width / 2 - resource.halfEventBarWidth,
                    height - resource.eventYOffset,
                    width / 2 + resource.halfEventBarWidth,
                    height - resource.eventYOffset, resource.linePaint);
        }

        if (hasAppointment) {
            canvas.drawLine(width / 2 - resource.halfEventBarWidth,
                    height - resource.appointmentYOffset,
                    width / 2 + resource.halfEventBarWidth,
                    height - resource.appointmentYOffset, resource.linePaint);
        }

        int xPos = (width - (int) resource.textPaint.measureText(text)) / 2;
        resource.textPaint.getTextBounds(text, 0, text.length(), bounds);
        int yPos = (height + bounds.height()) / 2;

        canvas.drawText(text, xPos, yPos, resource.textPaint);
    }

    private void setAll(String text, boolean isToday, boolean isSelected,
                        boolean hasEvent, boolean hasAppointment, boolean isHoliday,
                        int textSize, long jdn, int dayOfMonth, boolean isNumber) {
        setContentDescription(text);
        this.text = text;
        this.today = isToday;
        this.selected = isSelected;
        this.hasEvent = hasEvent;
        this.hasAppointment = hasAppointment;
        this.holiday = isHoliday;
        this.textSize = textSize;
        this.jdn = jdn;
        this.dayOfMonth = dayOfMonth;
        this.isNumber = isNumber;
        postInvalidate();
    }

    public void setDayOfMonthItem(boolean isToday, boolean isSelected,
                                  boolean hasEvent, boolean hasAppointment, boolean isHoliday,
                                  int textSize, long jdn, int dayOfMonth) {
        setAll(Utils.formatNumber(dayOfMonth), isToday, isSelected, hasEvent, hasAppointment,
                isHoliday, textSize, jdn, dayOfMonth, true);
    }

    public void setNonDayOfMonthItem(String text, int textSize) {
        setAll(text, false, false, false, false, false,
                textSize, -1, -1, false);
    }

    private String text = "";
    private boolean today, selected, hasEvent, hasAppointment, holiday;
    private int textSize;
    private long jdn = -1;
    private int dayOfMonth = -1;
    private boolean isNumber;

    public long getJdn() {
        return jdn;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }
}
