package com.byagowi.persiancalendar.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;

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

    private View resultViewer;
    private SeekBar redSeekbar, greenSeekbar, blueSeekbar;

    private void init() {
        setOrientation(HORIZONTAL);

        Context context = getContext();
        if (context == null) return;

        float density = context.getResources().getDisplayMetrics().density;
        int padding = (int) (density * 10);
        setPadding(padding, padding, padding, padding);

        resultViewer = new View(context);

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

        addView(seekBars);

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.addView(resultViewer);
        frameLayout.setLayoutParams(new LayoutParams(seekBars.getMeasuredHeight(),
                LayoutParams.MATCH_PARENT));
        frameLayout.setBackgroundColor(Color.LTGRAY);
        int framePadding = (int) density;
        frameLayout.setPadding(framePadding, framePadding, framePadding, framePadding);
        addView(frameLayout);
    }

    private void showColor() {
        resultViewer.setBackgroundColor(Color.argb(0xFF,
                redSeekbar.getProgress(), greenSeekbar.getProgress(), blueSeekbar.getProgress()));
    }

    public void setPickedColor(int color) {
        redSeekbar.setProgress(Color.red(color));
        greenSeekbar.setProgress(Color.blue(color));
        blueSeekbar.setProgress(Color.green(color));
        showColor();
    }

    public int getPickerColor() {
        return Color.argb(0xFF,
                redSeekbar.getProgress(), greenSeekbar.getProgress(), blueSeekbar.getProgress());
    }
}
