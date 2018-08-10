package com.byagowi.persiancalendar.view.fragment

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast

import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.util.UIUtils
import com.byagowi.persiancalendar.util.Utils
import com.byagowi.persiancalendar.view.QiblaCompassView
import com.github.praytimes.Coordinate

import androidx.annotation.Nullable
import androidx.fragment.app.Fragment

/**
 * Compass/Qibla activity
 *
 * @author ebraminio
 */
class CompassFragment : Fragment() {
  private lateinit var compassView: QiblaCompassView
  private var sensorManager: SensorManager? = null
  private var sensor: Sensor? = null
  private var compassListener: SensorEventListener? = null
  private var orientation = 0f

  var stop = false

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val ctx = context ?: return null
    setHasOptionsMenu(true)

    val view = inflater.inflate(R.layout.fragment_compass, container, false)

    val localActivity = activity
    val coordinate = Utils.getCoordinate(ctx)
    if (localActivity != null) {
      if (coordinate == null) {
        UIUtils.setActivityTitleAndSubtitle(localActivity, getString(R.string.compass), "")
      } else {
        UIUtils.setActivityTitleAndSubtitle(localActivity, getString(R.string.qibla_compass),
            Utils.getCityName(ctx, true))
      }
    }


    compassListener = object : SensorEventListener {
      /*
             * time smoothing constant for low-pass filter 0 ≤ alpha ≤ 1 ; a smaller
             * value basically means more smoothing See:
             * http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
             */
      internal val ALPHA = 0.15f
      internal var azimuth: Float = 0.toFloat()

      override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

      override fun onSensorChanged(event: SensorEvent) {
        // angle between the magnetic north direction
        // 0=North, 90=East, 180=South, 270=West
        var angle = event.values[0] + orientation
        if (stop) angle = 0f
        azimuth = lowPass(angle, azimuth)
        compassView.setBearing(azimuth)
        compassView.invalidate()
      }

      /**
       * https://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
       * http://developer.android.com/reference/android/hardware/SensorEvent.html#values
       */
      private fun lowPass(input: Float, output: Float): Float {
        return if (Math.abs(180 - input) > 170) {
          input
        } else output + ALPHA * (input - output)
      }
    }
    compassView = view.findViewById(R.id.compass_view)
    setCompassMetrics()

    if (coordinate != null) {
      compassView.setLongitude(coordinate.longitude)
      compassView.setLatitude(coordinate.latitude)
      compassView.initCompassView()
      compassView.invalidate()
    }

    sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
    sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
    if (sensor != null) {
      sensorManager?.registerListener(compassListener, sensor,
          SensorManager.SENSOR_DELAY_FASTEST)
    } else {
      Toast.makeText(context, getString(R.string.compass_not_found), Toast.LENGTH_SHORT).show()
    }
    return view
  }

  override fun onConfigurationChanged(newConfig: Configuration?) {
    super.onConfigurationChanged(newConfig)
    setCompassMetrics()
  }

  private fun setCompassMetrics() {
    val displayMetrics = DisplayMetrics()
    activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
    val width = displayMetrics.widthPixels
    val height = displayMetrics.heightPixels
    compassView.setScreenResolution(width, height - 2 * height / 8)

    val wm = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager? ?: return

    when (wm.defaultDisplay.orientation) {
      Surface.ROTATION_0 -> orientation = 0f
      Surface.ROTATION_90 -> orientation = 90f
      Surface.ROTATION_180 -> orientation = 180f
      Surface.ROTATION_270 -> orientation = 270f
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
    super.onCreateOptionsMenu(menu, inflater)
    if (menu != null && inflater != null) {
      menu.clear()
      inflater.inflate(R.menu.compass_menu_button, menu)
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.stop -> {
        stop = !stop
        item.setIcon(if (stop) R.drawable.ic_play else R.drawable.ic_stop)
      }
      else -> {
      }
    }
    return true
  }

  override fun onDestroy() {
    super.onDestroy()
    sensorManager?.unregisterListener(compassListener)
  }
}
