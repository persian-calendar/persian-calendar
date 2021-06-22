package net.androgames.level

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface

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
class OrientationProvider(activity: Activity, private val view: LevelView) : SensorEventListener {
    /**
     * Rotation Matrix
     */
    private val MAG = floatArrayOf(1f, 1f, 1f)
    private val I = FloatArray(16)
    private val R = FloatArray(16)
    private val outR = FloatArray(16)
    private val LOC = FloatArray(3)
    private val sensorManager: SensorManager?
    private val displayOrientation: Int
    private val sensor: Sensor?
    /**
     * Returns true if the manager is listening to orientation changes
     */
    /**
     * indicates whether or not Accelerometer Sensor is running
     */
    var isListening = false
        private set

    /**
     * Orientation
     */
    private var pitch = 0f
    private var roll = 0f
    private var balance = 0f
    private var minStep = 360f
    private var refValues = 0f

    /**
     * Unregisters listeners
     */
    fun stopListening() {
        isListening = false
        try {
            sensorManager?.unregisterListener(this)
        } catch (ignore: Exception) {
        }
    }

    /**
     * Registers a listener and start listening
     */
    fun startListening() {
        // register listener and start listening
        if (sensorManager == null || sensor == null) return
        isListening =
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
        view.invalidate()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    override fun onSensorChanged(event: SensorEvent) {
        val oldPitch = pitch
        val oldRoll = roll
        val oldBalance = balance
        SensorManager.getRotationMatrix(R, I, event.values, MAG)
        when (displayOrientation) {
            Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
                R,
                SensorManager.AXIS_MINUS_Y,
                SensorManager.AXIS_X,
                outR
            )
            Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
                R,
                SensorManager.AXIS_MINUS_X,
                SensorManager.AXIS_MINUS_Y,
                outR
            )
            Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
                R,
                SensorManager.AXIS_Y,
                SensorManager.AXIS_MINUS_X,
                outR
            )
            Surface.ROTATION_0 -> SensorManager.remapCoordinateSystem(
                R,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Y,
                outR
            )
            else -> SensorManager.remapCoordinateSystem(
                R,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Y,
                outR
            )
        }
        SensorManager.getOrientation(outR, LOC)

        // normalize z on ux, uy
        var tmp = Math.sqrt((outR[8] * outR[8] + outR[9] * outR[9]).toDouble())
            .toFloat()
        tmp = if (tmp == 0f) 0 else outR[8] / tmp

        // LOC[0] compass
        pitch = Math.toDegrees(LOC[1].toDouble()).toFloat()
        roll = (-Math.toDegrees(LOC[2].toDouble())).toFloat()
        balance = Math.toDegrees(Math.asin(tmp.toDouble())).toFloat()

        // calculating minimal sensor step
        if (oldRoll != roll || oldPitch != pitch || oldBalance != balance) {
            if (oldPitch != pitch) {
                minStep = Math.min(minStep, Math.abs(pitch - oldPitch))
            }
            if (oldRoll != roll) {
                minStep = Math.min(minStep, Math.abs(roll - oldRoll))
            }
            if (oldBalance != balance) {
                minStep = Math.min(minStep, Math.abs(balance - oldBalance))
            }
            if (refValues < MIN_VALUES) {
                refValues++
            }
        }
        val orientation: Orientation
        orientation = if (pitch < -45 && pitch > -135) {
            // top side up
            Orientation.TOP
        } else if (pitch > 45 && pitch < 135) {
            // bottom side up
            Orientation.BOTTOM
        } else if (roll > 45) {
            // right side up
            Orientation.RIGHT
        } else if (roll < -45) {
            // left side up
            Orientation.LEFT
        } else {
            // landing
            Orientation.LANDING
        }

        // propagation of the orientation
        view.setOrientation(orientation, pitch, roll, balance)
    }

    companion object {
        private const val MIN_VALUES = 20
    }

    init {
        displayOrientation = activity.windowManager.defaultDisplay.rotation
        sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager == null) return
        val sensors = sensorManager!!.getSensorList(Sensor.TYPE_ACCELEROMETER)
        if (sensors.size == 0) return
        sensor = sensors[0]
    }
}
