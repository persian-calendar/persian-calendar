package com.byagowi.persiancalendar.view.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;

public class MonthMainLayout extends LinearLayout {

    private CalendarFragment calendarFragment;

    public MonthMainLayout(Context context) {
        super(context);
    }

    public MonthMainLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MonthMainLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (calendarFragment == null || (
                action != AccessibilityNodeInfo.ACTION_SCROLL_FORWARD &&
                        action != AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)) {
            return super.performAccessibilityAction(action, arguments);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            calendarFragment.a11yBringPrevNextDay(action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        }

        return true;
    }

    public void setCalendarFragment(CalendarFragment calendarFragment) {
        this.calendarFragment = calendarFragment;
    }
}
