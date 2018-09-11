/*
 * Single class, no dependency, ColorPickerView.
 * Unlike the rest of the project is released under MIT license.
 * Feel free to copy and use it wherever you like or suggest improvements to it
 *
 * Copyright (c) 2018 Ebrahim Byagowi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.byagowi.persiancalendar.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

public class ColorPickerView extends LinearLayout {
    public ColorPickerView(Context context) {
        super(context);
        init();
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private TextView colorResultView;
    private SeekBar redSeekbar, greenSeekbar, blueSeekbar;
    private LinearLayout colorsToPick;

    private boolean colorCodeVisibility = false;

    private void init() {
        setOrientation(VERTICAL);

        Context context = getContext();
        if (context == null) return;

        float density = context.getResources().getDisplayMetrics().density;
        int padding = (int) (density * 10);
        setPadding(padding, padding, padding, padding);

        colorResultView = new TextView(context);
        colorResultView.setTextIsSelectable(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            colorResultView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        }
        colorResultView.setOnClickListener(view -> {
            colorCodeVisibility = !colorCodeVisibility;
            showColor();
        });

        redSeekbar = new SeekBar(context);
        greenSeekbar = new SeekBar(context);
        blueSeekbar = new SeekBar(context);

        int seekbarPadding = (int) density * 8;
        int currentSidePad = redSeekbar.getPaddingLeft();
        redSeekbar.setPadding(currentSidePad, seekbarPadding, currentSidePad, seekbarPadding);
        greenSeekbar.setPadding(currentSidePad, seekbarPadding, currentSidePad, seekbarPadding);
        blueSeekbar.setPadding(currentSidePad, seekbarPadding, currentSidePad, seekbarPadding);

        redSeekbar.setMax(255);
        greenSeekbar.setMax(255);
        blueSeekbar.setMax(255);

        redSeekbar.getProgressDrawable().setColorFilter(0xFFC00000, PorterDuff.Mode.SRC_IN);
        greenSeekbar.getProgressDrawable().setColorFilter(0xFF00C000, PorterDuff.Mode.SRC_IN);
        blueSeekbar.getProgressDrawable().setColorFilter(0xFF0000C0, PorterDuff.Mode.SRC_IN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            redSeekbar.getThumb().setColorFilter(0xFFC00000, PorterDuff.Mode.SRC_IN);
            greenSeekbar.getThumb().setColorFilter(0xFF00C000, PorterDuff.Mode.SRC_IN);
            blueSeekbar.getThumb().setColorFilter(0xFF0000C0, PorterDuff.Mode.SRC_IN);
        }

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                showColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        redSeekbar.setOnSeekBarChangeListener(listener);
        greenSeekbar.setOnSeekBarChangeListener(listener);
        blueSeekbar.setOnSeekBarChangeListener(listener);

        LinearLayout seekBars = new LinearLayout(context);
        seekBars.setOrientation(VERTICAL);
        seekBars.addView(redSeekbar);
        seekBars.addView(greenSeekbar);
        seekBars.addView(blueSeekbar);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        seekBars.setLayoutParams(params);
        seekBars.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.addView(colorResultView);
        frameLayout.setLayoutParams(new LayoutParams(seekBars.getMeasuredHeight(),
                LayoutParams.MATCH_PARENT));
        frameLayout.setBackgroundColor(Color.LTGRAY);
        int framePadding = (int) density;
        frameLayout.setPadding(framePadding, framePadding, framePadding, framePadding);

        LinearLayout widgetMain = new LinearLayout(context);
        widgetMain.addView(seekBars);
        widgetMain.addView(frameLayout);

        colorsToPick = new LinearLayout(context);
        colorsToPick.setGravity(Gravity.CENTER);
        colorsToPick.setOrientation(HORIZONTAL);

        addView(widgetMain);
        addView(colorsToPick);
    }

    public void setColorsToPick(/*@ColorInt*/ int[] colors) {
        colorsToPick.removeAllViews();

        Context context = getContext();
        if (context == null) return;

        float density = context.getResources().getDisplayMetrics().density;

        for (int color : colors) {
            View view = new View(context);
            view.setBackgroundColor(color);
            view.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));

            FrameLayout frameLayout = new FrameLayout(context);
            int rectSize = (int) (40 * density);
            int margin = (int) (5 * density);
            LayoutParams layoutParams = new LayoutParams(rectSize, rectSize);
            layoutParams.setMargins(margin, margin * 2, margin, margin);
            frameLayout.setBackgroundColor(Color.LTGRAY);
            frameLayout.setLayoutParams(layoutParams);
            frameLayout.addView(view);
            int framePadding = (int) density;
            frameLayout.setPadding(framePadding, framePadding, framePadding, framePadding);

            frameLayout.setOnClickListener(v -> setPickedColor(color));

            colorsToPick.addView(frameLayout);
        }
    }

    private void showColor() {
        int color = Color.argb(0xFF, redSeekbar.getProgress(),
                greenSeekbar.getProgress(), blueSeekbar.getProgress());
        colorResultView.setBackgroundColor(color);
        colorResultView.setText(colorCodeVisibility
                ? String.format(Locale.ENGLISH, "#%06X", 0xFFFFFF & color)
                : "");
        colorResultView.setTextColor(color ^ 0xFFFFFF);
    }

    public void setPickedColor(/*@ColorInt*/ int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            redSeekbar.setProgress(Color.red(color), true);
            greenSeekbar.setProgress(Color.green(color), true);
            blueSeekbar.setProgress(Color.blue(color), true);
        } else {
            redSeekbar.setProgress(Color.red(color));
            greenSeekbar.setProgress(Color.green(color));
            blueSeekbar.setProgress(Color.blue(color));
        }
        showColor();
    }

    /*@ColorInt*/
    public int getPickerColor() {
        return Color.argb(0xFF,
                redSeekbar.getProgress(), greenSeekbar.getProgress(), blueSeekbar.getProgress());
    }
}
