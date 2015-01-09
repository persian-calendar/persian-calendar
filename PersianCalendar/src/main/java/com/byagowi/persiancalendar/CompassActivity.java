package com.byagowi.persiancalendar;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.byagowi.persiancalendar.view.QiblaCompassView;
import com.github.praytimes.Coordinate;

/**
 * Compass/Qibla activity
 *
 * @author ebraminio
 */
public class CompassActivity extends Activity {
    QiblaCompassView compassView;
    TextView degree;
    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener compassListener;
    private Utils utils = Utils.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // utils.setTheme(this);
        super.onCreate(savedInstanceState);
        compassListener = new CompassListener(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.compass);
        compassView = (QiblaCompassView) findViewById(R.id.compass_view);

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        compassView.setScreenResolution(width, height - 2 * height / 8);

        Coordinate coordinate = utils.getCoordinate(this);
        if (coordinate != null) {
            compassView.setLongitude(coordinate.getLongitude());
            compassView.setLatitude(coordinate.getLatitude());
            compassView.initCompassView();
            compassView.invalidate();
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (sensor != null) {
            sensorManager.registerListener(compassListener, sensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            utils.quickToast(getString(R.string.compass_not_found), this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensor != null) {
            sensorManager.unregisterListener(compassListener);
        }
    }
}
