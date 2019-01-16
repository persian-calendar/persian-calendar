package com.byagowi.persiancalendar.view.itemdayview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;

public class ItemDayView extends View {
    private DaysPaintResources resource;
    private Rect bounds = new Rect();
    private RectF drawingRect = new RectF();
    private String text = "";
    private boolean today, selected, hasEvent, hasAppointment, holiday;
    private int textSize;
    private long jdn = -1;
    private int dayOfMonth = -1;
    private boolean isNumber;
    private String header = "";

    public ItemDayView(Context context, DaysPaintResources resource) {
        super(context);
        this.resource = resource;
    }

    // These constructors shouldn't be used
    // as the first one reuses resource retrieval across the days
    public ItemDayView(Context context) {
        super(context);
        if (context instanceof Activity) {
            resource = new DaysPaintResources((Activity) context);
        }
    }

    public ItemDayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (context instanceof Activity) {
            resource = new DaysPaintResources((Activity) context);
        }
    }

    public ItemDayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (context instanceof Activity) {
            resource = new DaysPaintResources((Activity) context);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2;

        boolean isModernTheme = resource.style == R.style.ModernTheme;
        getDrawingRect(bounds);
        drawingRect.set(bounds);
        drawingRect.inset(radius * 0.1f, radius * 0.1f);
        int yOffsetToApply = isModernTheme ? (int) (-height * .07f) : 0;

        if (selected) {
            if (isModernTheme) {
                canvas.drawRoundRect(drawingRect, 0, 0, resource.selectedPaint);
            } else {
                canvas.drawCircle(width / 2f, height / 2f, radius - 5,
                        resource.selectedPaint);
            }
        }

        if (today) {
            if (isModernTheme) {
                canvas.drawRoundRect(drawingRect, 0, 0, resource.todayPaint);
            } else {
                canvas.drawCircle(width / 2f, height / 2f, radius - 5,
                        resource.todayPaint);
            }
        }

        int color;
        if (isNumber) {
            color = holiday
                    ? (selected ? resource.colorHolidaySelected : resource.colorHoliday)
                    : (selected ? resource.colorTextDaySelected : resource.colorTextDay);
//            if (today && !selected) {
//                color = resource.colorTextToday;
//            }
        } else {
            color = resource.colorTextDayName;
        }

        resource.eventBarPaint.setColor((selected && !isModernTheme) ? color : resource.colorEventLine);

        if (hasEvent) {
            canvas.drawLine(width / 2f - resource.halfEventBarWidth,
                    height - resource.eventYOffset + yOffsetToApply,
                    width / 2f + resource.halfEventBarWidth,
                    height - resource.eventYOffset + yOffsetToApply, resource.eventBarPaint);
        }

        if (hasAppointment) {
            canvas.drawLine(width / 2f - resource.halfEventBarWidth,
                    height - resource.appointmentYOffset + yOffsetToApply,
                    width / 2f + resource.halfEventBarWidth,
                    height - resource.appointmentYOffset + yOffsetToApply, resource.eventBarPaint);
        }

        // TODO: Better to not change resource's paint objects, but for now
        resource.textPaint.setColor(color);
        resource.textPaint.setTextSize(textSize);

        if (isModernTheme) {
            resource.textPaint.setFakeBoldText(today);
            resource.textPaint.setTextSize(textSize * .8f);
        }

        int xPos = (width - (int) resource.textPaint.measureText(text)) / 2;
        String textToMeasureHeight =
                isNumber ? text : (Utils.getAppLanguage().equals(Constants.LANG_EN_US) ? "Y" : "شچ");
        resource.textPaint.getTextBounds(textToMeasureHeight, 0, textToMeasureHeight.length(), bounds);
        int yPos = (height + bounds.height()) / 2;
        yPos += yOffsetToApply;
        canvas.drawText(text, xPos, yPos, resource.textPaint);

        resource.textPaint.setColor(selected ? resource.colorTextDaySelected : resource.colorTextDay);
        resource.textPaint.setTextSize(textSize / 2.f);
        if (!TextUtils.isEmpty(header)) {
            int headerXPos = (width - (int) resource.textPaint.measureText(header)) / 2;
            canvas.drawText(header, headerXPos, yPos * 0.87f - bounds.height(), resource.textPaint);
        }
    }

    private void setAll(String text, boolean isToday, boolean isSelected,
                        boolean hasEvent, boolean hasAppointment, boolean isHoliday,
                        int textSize, long jdn, int dayOfMonth, boolean isNumber,
                        String header) {
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
        this.header = header;
        postInvalidate();
    }

    public void setDayOfMonthItem(boolean isToday, boolean isSelected,
                                  boolean hasEvent, boolean hasAppointment, boolean isHoliday,
                                  int textSize, long jdn, int dayOfMonth, String header) {
        String dayOfMonthString = Utils.formatNumber(dayOfMonth);
        setAll(dayOfMonthString, isToday, isSelected, hasEvent, hasAppointment,
                isHoliday, textSize, jdn, dayOfMonth, true, header);
    }

    public void setNonDayOfMonthItem(String text, int textSize) {
        setAll(text, false, false, false, false, false,
                textSize, -1, -1, false, "");
    }

    public long getJdn() {
        return jdn;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }
}
