package com.byagowi.persiancalendar.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.duolingo.open.rtlviewpager.RtlViewPager;

// https://stackoverflow.com/a/47774679
public class CardsViewPager extends RtlViewPager {
    private View mCurrentView;

    public CardsViewPager(Context context) {
        super(context);
    }

    public CardsViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mCurrentView == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int height = 0;
        mCurrentView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int h = mCurrentView.getMeasuredHeight();
        if (h > height) height = h;
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void measureCurrentView(View currentView) {
        mCurrentView = currentView;
        requestLayout();
    }
}
