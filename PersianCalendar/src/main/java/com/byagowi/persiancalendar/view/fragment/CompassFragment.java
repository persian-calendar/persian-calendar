package com.byagowi.persiancalendar.view.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.QiblaCompassView;
import com.github.praytimes.Coordinate;

/**
 * Compass/Qibla activity
 *
 * @author ebraminio
 */
public class CompassFragment extends Fragment {
    public QiblaCompassView compassView;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener compassListener;
    private float orientation = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_compass, container, false);

        Context context = getContext();
        Coordinate coordinate = Utils.getCoordinate(getContext());
        if (coordinate == null) {
            Utils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.compass), "");
        } else {
            Utils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.qibla_compass),
                    Utils.getCityName(context, true));
        }


        compassListener = new SensorEventListener() {
            /*
             * time smoothing constant for low-pass filter 0 ≤ alpha ≤ 1 ; a smaller
             * value basically means more smoothing See:
             * http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
             */
            static final float ALPHA = 0.15f;
            float azimuth;

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                // angle between the magnetic north direction
                // 0=North, 90=East, 180=South, 270=West
                float angle = event.values[0] + orientation;
                if (stop) angle = 0;
                azimuth = lowPass(angle, azimuth);
                compassView.setBearing(azimuth);
                compassView.invalidate();
            }

            /**
             * https://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
             * http://developer.android.com/reference/android/hardware/SensorEvent.html#values
             */
            private float lowPass(float input, float output) {
                if (Math.abs(180 - input) > 170) {
                    return input;
                }
                return output + ALPHA * (input - output);
            }
        };
        compassView = view.findViewById(R.id.compass_view);
        setCompassMetrics();

        if (coordinate != null) {
            compassView.setLongitude(coordinate.getLongitude());
            compassView.setLatitude(coordinate.getLatitude());
            compassView.initCompassView();
            compassView.invalidate();
        }

        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (sensor != null) {
            sensorManager.registerListener(compassListener, sensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Toast.makeText(context, getString(R.string.compass_not_found), Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCompassMetrics();
    }

    private void setCompassMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        compassView.setScreenResolution(width, height - 2 * height / 8);

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        switch (wm.getDefaultDisplay().getOrientation()) {
            case Surface.ROTATION_0:
                orientation = 0;
                break;
            case Surface.ROTATION_90:
                orientation = 90;
                break;
            case Surface.ROTATION_180:
                orientation = 180;
                break;
            case Surface.ROTATION_270:
                orientation = 270;
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.compass_menu_button, menu);
    }

    public boolean stop = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop:
                stop = !stop;
                item.setIcon(stop ? R.drawable.ic_play : R.drawable.ic_stop);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensor != null) {
            sensorManager.unregisterListener(compassListener);
        }
    }
}
