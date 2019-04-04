package com.byagowi.persiancalendar.ui.compass

import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentCompassBinding
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency
import com.byagowi.persiancalendar.praytimes.Coordinate
import com.byagowi.persiancalendar.utils.Utils
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Compass/Qibla activity
 *
 * @author ebraminio
 */
class CompassFragment : DaggerFragment() {
    var stop = false
    @Inject
    lateinit var mainActivityDependency: MainActivityDependency
    private lateinit var binding: FragmentCompassBinding
    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private var orientation = 0f
    private var sensorNotFound = false
    private var coordinate: Coordinate? = null
    private val compassListener = object : SensorEventListener {
        /*
         * time smoothing constant for low-pass filter 0 ≤ alpha ≤ 1 ; a smaller
         * value basically means more smoothing See:
         * http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
         */
        val ALPHA = 0.15f
        var azimuth: Float = 0f

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            // angle between the magnetic north direction
            // 0=North, 90=East, 180=South, 270=West
            var angle = event.values[0] + orientation
            if (stop)
                angle = 0f
            else
                binding.compassView.isOnDirectionAction()

            azimuth = lowPass(angle, azimuth)
            binding.compassView.setBearing(azimuth)
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

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCompassBinding.inflate(inflater, container, false).apply {
            coordinate = Utils.getCoordinate(mainActivityDependency.mainActivity)

            mainActivityDependency.mainActivity.setTitleAndSubtitle(getString(R.string.compass),
                    Utils.getCityName(mainActivityDependency.mainActivity, true))

            bottomAppbar.replaceMenu(R.menu.compass_menu_buttons)
            bottomAppbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.level -> mainActivityDependency.mainActivity.navigateTo(R.id.level)
                    R.id.help -> Utils.createAndShowSnackbar(view, mainActivityDependency.mainActivity
                            .getString(if (sensorNotFound)
                                R.string.compass_not_found
                            else
                                R.string.calibrate_compass_summary), 5000)
                    else -> {
                    }
                }
                true
            }
            fab.setOnClickListener {
                stop = !stop
                fab.setImageResource(if (stop) R.drawable.ic_play else R.drawable.ic_stop)
                fab.contentDescription = mainActivityDependency.mainActivity
                        .getString(if (stop) R.string.resume else R.string.stop)
            }
        }

        setCompassMetrics()
        coordinate?.longitude?.let { binding.compassView.setLongitude(it) }
        coordinate?.latitude?.let { binding.compassView.setLatitude(it) }
        binding.compassView.initCompassView()

        return binding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setCompassMetrics()
    }

    private fun setCompassMetrics() {
        val displayMetrics = DisplayMetrics()

        mainActivityDependency.mainActivity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        binding.compassView.setScreenResolution(width, height - 2 * height / 8)

        val wm: WindowManager? = mainActivityDependency.mainActivity.getSystemService()

        when (wm?.defaultDisplay?.rotation) {
            Surface.ROTATION_0 -> orientation = 0f
            Surface.ROTATION_90 -> orientation = 90f
            Surface.ROTATION_180 -> orientation = 180f
            Surface.ROTATION_270 -> orientation = 270f
        }
    }

    override fun onResume() {
        super.onResume()

        val mainActivity = mainActivityDependency.mainActivity
        sensorManager = mainActivity.getSystemService()
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        if (sensor != null) {
            sensorManager?.registerListener(compassListener, sensor, SensorManager.SENSOR_DELAY_FASTEST)
            if (coordinate == null) {
                Utils.createAndShowShortSnackbar(mainActivity.coordinator, R.string.set_location)
            }
        } else {
            Utils.createAndShowShortSnackbar(view, R.string.compass_not_found)
            sensorNotFound = true
        }
    }

    override fun onPause() {
        if (sensor != null) {
            sensorManager?.unregisterListener(compassListener)
        }
        super.onPause()
    }
}
