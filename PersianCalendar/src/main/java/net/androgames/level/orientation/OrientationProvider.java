package net.androgames.level.orientation;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Surface;

import net.androgames.level.Level;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
 *  This file is part of Level (an Android Bubble Level).
 *  <https://github.com/avianey/Level>
 *
 *  Copyright (C) 2014 Antoine Vianey
 *
 *  Level is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Level is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Level. If not, see <http://www.gnu.org/licenses/>
 */
public final class OrientationProvider implements SensorEventListener {

    private static final int MIN_VALUES = 20;

    /**
     * Calibration
     */
    private static final String SAVED_PITCH = "pitch.";
    private static final String SAVED_ROLL = "roll.";
    private static final String SAVED_BALANCE = "balance.";

    private Sensor sensor;
    private SensorManager sensorManager;
    private OrientationListener listener;

    /**
     * indicates whether or not Accelerometer Sensor is supported
     */
    private Boolean supported;

    /**
     * indicates whether or not Accelerometer Sensor is running
     */
    private boolean running = false;

    /**
     * Calibration
     */
    private final float[] calibratedPitch = new float[5];
    private final float[] calibratedRoll = new float[5];
    private final float[] calibratedBalance = new float[5];
    private boolean calibrating = false;

    /**
     * Orientation
     */
    private float pitch;
    private float roll;
    private float balance;
    private float tmp;
    private float oldPitch;
    private float oldRoll;
    private float oldBalance;
    private float minStep = 360;
    private float refValues = 0;
    private Orientation orientation;
    private boolean locked;
    private int displayOrientation;

    /**
     * Rotation Matrix
     */
    private final float[] MAG = new float[]{1f, 1f, 1f};
    private final float[] I = new float[16];
    private final float[] R = new float[16];
    private final float[] outR = new float[16];
    private final float[] LOC = new float[3];

    private Level level;

    public OrientationProvider(Level level) {
        this.level = level;
        this.displayOrientation = level.getWindowManager().getDefaultDisplay().getRotation();
    }

    /**
     * Returns true if the manager is listening to orientation changes
     */
    public boolean isListening() {
        return running;
    }

    /**
     * Unregisters listeners
     */
    public void stopListening() {
        running = false;
        try {
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
        } catch (Exception e) {
        }
    }

    private List<Integer> getRequiredSensors() {
        return Collections.singletonList(Sensor.TYPE_ACCELEROMETER);
    }

    /**
     * Returns true if at least one Accelerometer sensor is available
     */
    public boolean isSupported() {
        if (supported == null) {
            if (level != null) {
                sensorManager = (SensorManager) level.getSystemService(Context.SENSOR_SERVICE);
                boolean supported = true;
                for (int sensorType : getRequiredSensors()) {
                    List<Sensor> sensors = sensorManager.getSensorList(sensorType);
                    supported = (sensors.size() > 0) && supported;
                }
                this.supported = supported;
                return supported;
            }
        }
        return supported;
    }


    /**
     * Registers a listener and start listening
     *
     * @param orientationListener callback for accelerometer events
     */
    public void startListening(OrientationListener orientationListener) {
        final Activity context = level;
        // load calibration
        calibrating = false;
        Arrays.fill(calibratedPitch, 0);
        Arrays.fill(calibratedRoll, 0);
        Arrays.fill(calibratedBalance, 0);
        SharedPreferences prefs = context.getPreferences(Context.MODE_PRIVATE);
        for (Orientation orientation : Orientation.values()) {
            calibratedPitch[orientation.ordinal()] =
                    prefs.getFloat(SAVED_PITCH + orientation.toString(), 0);
            calibratedRoll[orientation.ordinal()] =
                    prefs.getFloat(SAVED_ROLL + orientation.toString(), 0);
            calibratedBalance[orientation.ordinal()] =
                    prefs.getFloat(SAVED_BALANCE + orientation.toString(), 0);
        }
        // register listener and start listening
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        running = true;
        for (int sensorType : getRequiredSensors()) {
            List<Sensor> sensors = sensorManager.getSensorList(sensorType);
            if (sensors.size() > 0) {
                sensor = sensors.get(0);
                running = sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL) && running;
            }
        }
        if (running) {
            listener = orientationListener;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        oldPitch = pitch;
        oldRoll = roll;
        oldBalance = balance;

        SensorManager.getRotationMatrix(R, I, event.values, MAG);

        // compute pitch, roll & balance
        switch (displayOrientation) {
            case Surface.ROTATION_270:
                SensorManager.remapCoordinateSystem(
                        R,
                        SensorManager.AXIS_MINUS_Y,
                        SensorManager.AXIS_X,
                        outR);
                break;
            case Surface.ROTATION_180:
                SensorManager.remapCoordinateSystem(
                        R,
                        SensorManager.AXIS_MINUS_X,
                        SensorManager.AXIS_MINUS_Y,
                        outR);
                break;
            case Surface.ROTATION_90:
                SensorManager.remapCoordinateSystem(
                        R,
                        SensorManager.AXIS_Y,
                        SensorManager.AXIS_MINUS_X,
                        outR);
                break;
            case Surface.ROTATION_0:
            default:
                SensorManager.remapCoordinateSystem(
                        R,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Y,
                        outR);
                break;
        }

        SensorManager.getOrientation(outR, LOC);

        // normalize z on ux, uy
        tmp = (float) Math.sqrt(outR[8] * outR[8] + outR[9] * outR[9]);
        tmp = (tmp == 0 ? 0 : outR[8] / tmp);

        // LOC[0] compass
        pitch = (float) Math.toDegrees(LOC[1]);
        roll = -(float) Math.toDegrees(LOC[2]);
        balance = (float) Math.toDegrees(Math.asin(tmp));

        // calculating minimal sensor step
        if (oldRoll != roll || oldPitch != pitch || oldBalance != balance) {
            if (oldPitch != pitch) {
                minStep = Math.min(minStep, Math.abs(pitch - oldPitch));
            }
            if (oldRoll != roll) {
                minStep = Math.min(minStep, Math.abs(roll - oldRoll));
            }
            if (oldBalance != balance) {
                minStep = Math.min(minStep, Math.abs(balance - oldBalance));
            }
            if (refValues < MIN_VALUES) {
                refValues++;
            }
        }

        if (!locked || orientation == null) {
            if (pitch < -45 && pitch > -135) {
                // top side up
                orientation = Orientation.TOP;
            } else if (pitch > 45 && pitch < 135) {
                // bottom side up
                orientation = Orientation.BOTTOM;
            } else if (roll > 45) {
                // right side up
                orientation = Orientation.RIGHT;
            } else if (roll < -45) {
                // left side up
                orientation = Orientation.LEFT;
            } else {
                // landing
                orientation = Orientation.LANDING;
            }
        }

        if (calibrating) {
            calibrating = false;
            Editor editor = level.getPreferences(Context.MODE_PRIVATE).edit();
            editor.putFloat(SAVED_PITCH + orientation.toString(), pitch);
            editor.putFloat(SAVED_ROLL + orientation.toString(), roll);
            editor.putFloat(SAVED_BALANCE + orientation.toString(), balance);
            final boolean success = editor.commit();
            if (success) {
                calibratedPitch[orientation.ordinal()] = pitch;
                calibratedRoll[orientation.ordinal()] = roll;
                calibratedBalance[orientation.ordinal()] = balance;
            }
//            listener.onCalibrationSaved(success);
            pitch = 0;
            roll = 0;
            balance = 0;
        } else {
            pitch -= calibratedPitch[orientation.ordinal()];
            roll -= calibratedRoll[orientation.ordinal()];
            balance -= calibratedBalance[orientation.ordinal()];
        }

        // propagation of the orientation
        listener.onOrientationChanged(orientation, pitch, roll, balance);
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
