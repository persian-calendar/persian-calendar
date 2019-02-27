package com.byagowi.persiancalendar.ui.calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.duolingo.open.rtlviewpager.RtlViewPager;

// https://stackoverflow.com/a/47774679
public class TabsViewPager extends RtlViewPager {
    private View mCurrentView;

    public TabsViewPager(Context context) {
        super(context);
    }

    public TabsViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mCurrentView != null) {
            int height = 0;
            mCurrentView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int h = mCurrentView.getMeasuredHeight();
            if (h > height) height = h;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void measureCurrentView(View currentView) {
        mCurrentView = currentView;
        requestLayout();
    }
}
