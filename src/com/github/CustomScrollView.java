package com.github;

/* 
 * The reason behind this code is the problem of having multiple scrollable 
 * components inside each other, which causes problems while scrolling inner
 * components.
 * 
 * Code from stackoverflow.com
 * http://stackoverflow.com/questions/2646028/android-horizontalscrollview-within-scrollview-touch-handling
 * 
 * More information:
 * http://stackoverflow.com/questions/8381697/viewpager-inside-a-scrollview-does-not-scroll-correclty
 * http://stackoverflow.com/questions/2646028/android-horizontalscrollview-within-scrollview-touch-handling
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class CustomScrollView extends ScrollView {
    private GestureDetector mGestureDetector;
    View.OnTouchListener mGestureListener;

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, new YScrollDetector());
        setFadingEdgeLength(0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
    }

    // Return false if we're scrolling in the x direction  
    class YScrollDetector extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(Math.abs(distanceY) > Math.abs(distanceX)) {
                return true;
            }
            return false;
        }
    }
}
