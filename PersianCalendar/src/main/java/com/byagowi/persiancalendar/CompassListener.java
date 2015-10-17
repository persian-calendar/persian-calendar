package com.byagowi.persiancalendar;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.byagowi.persiancalendar.view.Fragment.CompassFragment;

public final class CompassListener implements SensorEventListener {
    /*
     * time smoothing constant for low-pass filter 0 ≤ alpha ≤ 1 ; a smaller
     * value basically means more smoothing See:
     * http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     */
    static final float ALPHA = 0.15f;
    float azimuth;
    private CompassFragment compassFragment;

    /**
     * @param compassFragment
     */
    public CompassListener(CompassFragment compassFragment) {
        this.compassFragment = compassFragment;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // angle between the magnetic north direction
        // 0=North, 90=East, 180=South, 270=West
        azimuth = lowPass(event.values[0], azimuth);
        compassFragment.compassView.setBearing(azimuth);
        compassFragment.compassView.invalidate();
    }

    /**
     * @see https://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
     * @see http://developer.android.com/reference/android/hardware/SensorEvent.html#values
     */
    private float lowPass(float input, float output) {
        if (Math.abs(180 - input) > 170) {
            return input;
        }
        return output + ALPHA * (input - output);
    }
}