package com.byagowi.persiancalendar;

import com.github.praytimes.Coordinate;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

/**
 * Compass/Qibla activity
 * 
 * @author ebraminio
 */
public class CompassActivity extends Activity {
	private CalendarUtils utils = CalendarUtils.getInstance();

	CompassView compassView;
	SensorManager sensorManager;
	Sensor sensor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		utils.setTheme(this);
		super.onCreate(savedInstanceState);
		compassView = new CompassView(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(compassView);

		Coordinate coordinate = utils.getCoordinate(this);
		if (coordinate != null) {
			compassView.setQibla(QiblaDirectionCalculator
					.getQiblaDirectionFromNorth(coordinate.getLatitude(),
							coordinate.getLongitude()));
		}

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		if (sensor != null) {
			sensorManager.registerListener(mySensorEventListener, sensor,
					SensorManager.SENSOR_DELAY_NORMAL);
			Log.i("Compass MainActivity", "Registerered for ORIENTATION Sensor");

		} else {
			Log.e("Compass MainActivity", "Registerered for ORIENTATION Sensor");
			Toast.makeText(this, "ORIENTATION Sensor not found",
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	private SensorEventListener mySensorEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// angle between the magnetic north direction
			// 0=North, 90=East, 180=South, 270=West
			float azimuth = event.values[0];
			compassView.setPosition(azimuth);
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (sensor != null) {
			sensorManager.unregisterListener(mySensorEventListener);
		}
	}
}
