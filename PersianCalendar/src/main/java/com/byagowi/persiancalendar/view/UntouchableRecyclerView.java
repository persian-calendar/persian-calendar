package com.byagowi.persiancalendar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;

// https://stackoverflow.com/a/47671471
public class UntouchableRecyclerView extends RecyclerView {

    public UntouchableRecyclerView(Context context) {
        super(context);
    }

    public UntouchableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UntouchableRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return false;
    }
}