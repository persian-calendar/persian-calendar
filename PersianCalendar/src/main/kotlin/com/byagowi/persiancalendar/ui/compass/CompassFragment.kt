package com.byagowi.persiancalendar.ui.compass

import android.animation.ValueAnimator
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.Variants.debugAssertNotNull
import com.byagowi.persiancalendar.databinding.FragmentCompassBinding
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.utils.SensorEventAnnouncer
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.cityName
import com.byagowi.persiancalendar.utils.formatCoordinateISO6709
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Compass/Qibla activity
 */
class CompassFragment : Fragment() {

    private var stopped = false
    private var binding: FragmentCompassBinding? = null
    private var sensorManager: SensorManager? = null
    private var orientationSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null
    private var orientation = 0f
    private var sensorNotFound = false

    private abstract inner class BaseSensorListener : SensorEventListener {
        /*
         * time smoothing constant for low-pass filter 0 ≤ alpha ≤ 1 ; a smaller
         * value basically means more smoothing See:
         * https://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
         */
        private val ALPHA = 0.15f
        private var azimuth: Float = 0f

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        protected fun update(value: Float) {
            // angle between the magnetic north direction
            // 0=North, 90=East, 180=South, 270=West
            val angle = if (stopped) 0f else value + orientation
            if (!stopped) checkIfA11yAnnounceIsNeeded(angle)
            azimuth = lowPass(angle, azimuth)
            binding?.compassView?.angle = azimuth
        }

        /**
         * https://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
         * https://developer.android.com/reference/android/hardware/SensorEvent.html#values
         */
        private fun lowPass(input: Float, output: Float): Float = when {
            abs(180 - input) > 170 -> input
            else -> output + ALPHA * (input - output)
        }
    }

    private val orientationSensorListener = object : BaseSensorListener() {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return
            update(event.values[0])
        }
    }

    private val accelerometerMagneticSensorListener = object : BaseSensorListener() {
        private val acceleration = FloatArray(3)
        private val magneticField = FloatArray(3)
        private var isAccelerationsAvailable = false
        private var isMagneticFieldAvailable = false
        private val rotationMatrix = FloatArray(9)
        private val orientationMatrix = FloatArray(3)

        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return

            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                acceleration[0] = event.values[0]
                acceleration[1] = event.values[1]
                acceleration[2] = event.values[2]
                isAccelerationsAvailable = true
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticField[0] = event.values[0]
                magneticField[1] = event.values[1]
                magneticField[2] = event.values[2]
                isMagneticFieldAvailable = true
            }

            if (isAccelerationsAvailable && isMagneticFieldAvailable &&
                SensorManager.getRotationMatrix(rotationMatrix, null, acceleration, magneticField)
            ) {
                SensorManager.getOrientation(rotationMatrix, orientationMatrix)
                update(Math.toDegrees(orientationMatrix[0].toDouble()).toFloat())
                isAccelerationsAvailable = false
                isMagneticFieldAvailable = false
            }
        }
    }

    private fun showLongSnackbar(@StringRes messageId: Int, duration: Int) {
        val rootView = view ?: return
        Snackbar.make(rootView, messageId, duration).apply {
            view.setOnClickListener { dismiss() }
            view.findViewById<TextView?>(com.google.android.material.R.id.snackbar_text)
                .debugAssertNotNull?.maxLines = 5
            anchorView = binding?.fab
        }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCompassBinding.inflate(inflater, container, false)
        this.binding = binding

        binding.appBar.toolbar.let { toolbar ->
            toolbar.setTitle(R.string.compass)
            toolbar.subtitle = inflater.context.appPrefs.cityName ?: coordinates?.run {
                formatCoordinateISO6709(latitude, longitude, elevation.takeIf { it != 0.0 })
            }
            toolbar.setupMenuNavigation()
        }

        binding.bottomAppbar.menu.add(R.string.help).also {
            it.icon = binding.bottomAppbar.context.getCompatDrawable(R.drawable.ic_info_in_menu)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.onClick {
                showLongSnackbar(
                    if (sensorNotFound) R.string.compass_not_found
                    else R.string.calibrate_compass_summary, 5000
                )
            }
        }
        binding.bottomAppbar.menu.add(R.string.map).also {
            it.icon = binding.bottomAppbar.context.getCompatDrawable(R.drawable.ic_map)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.onClick {
                runCatching {
                    CustomTabsIntent.Builder().build().launchUrl(
                        activity ?: return@runCatching, "https://g.co/qiblafinder".toUri()
                    )
                }.onFailure(logException)
            }
        }
        binding.bottomAppbar.menu.add(R.string.level).also {
            it.icon = binding.bottomAppbar.context.getCompatDrawable(R.drawable.ic_level)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.onClick {
                findNavController().navigateSafe(CompassFragmentDirections.actionCompassToLevel())
            }
        }

        binding.fab.setOnClickListener {
            stopped = !stopped
            binding.fab.setImageResource(if (stopped) R.drawable.ic_play else R.drawable.ic_stop)
            binding.fab.contentDescription = resources
                .getString(if (stopped) R.string.resume else R.string.stop)
        }

        if (coordinates != null) {
            binding.appBar.toolbar.menu.add(R.string.show_sun_and_moon_path_in_24_hours).also {
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                it.onClick(::animateMoonAndSun)
            }
        }

        updateCompassOrientation()

        return binding.root
    }

    private fun animateMoonAndSun() {
        val binding = binding ?: return
        val valueAnimator = ValueAnimator.ofFloat(0f, 24f)
        valueAnimator.repeatCount = 1
        valueAnimator.repeatMode = ValueAnimator.REVERSE
        valueAnimator.duration = 10000
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()
        valueAnimator.addUpdateListener {
            val value = valueAnimator.animatedValue as? Float ?: 0f
            val time = GregorianCalendar()
            time.add(Calendar.MINUTE, (value * 60f).roundToInt())
            binding.appBar.toolbar.title =
                if (value == 0f) getString(R.string.compass)
                else Clock(time).toBasicFormatString()
            binding.compassView.setTime(time)
        }
        valueAnimator.start()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateCompassOrientation()
    }

    private fun updateCompassOrientation() {
        orientation = when (activity?.getSystemService<WindowManager>()?.defaultDisplay?.rotation) {
            Surface.ROTATION_0 -> 0f
            Surface.ROTATION_90 -> 90f
            Surface.ROTATION_180 -> 180f
            Surface.ROTATION_270 -> 270f
            else -> 0f
        }
    }

    override fun onResume() {
        super.onResume()

        sensorManager = activity?.getSystemService()
        val sensorManager = sensorManager ?: return
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        if (orientationSensor != null) {
            sensorManager.registerListener(
                orientationSensorListener, orientationSensor, SensorManager.SENSOR_DELAY_FASTEST
            )
            if (BuildConfig.DEVELOPMENT)
                Toast.makeText(context, "dev: orientation", Toast.LENGTH_LONG).show()
            if (coordinates == null) showLongSnackbar(R.string.set_location, Snackbar.LENGTH_SHORT)
        } else if (accelerometerSensor != null && magnetometerSensor != null) {
            sensorManager.registerListener(
                accelerometerMagneticSensorListener, accelerometerSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
            sensorManager.registerListener(
                accelerometerMagneticSensorListener, magnetometerSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
            if (BuildConfig.DEVELOPMENT)
                Toast.makeText(context, "dev: acc+magnet", Toast.LENGTH_LONG).show()
            if (coordinates == null) showLongSnackbar(R.string.set_location, Snackbar.LENGTH_SHORT)
        } else {
            showLongSnackbar(R.string.compass_not_found, Snackbar.LENGTH_SHORT)
            sensorNotFound = true
        }
    }

    override fun onPause() {
        if (orientationSensor != null)
            sensorManager?.unregisterListener(orientationSensorListener)
        else if (accelerometerSensor != null && magnetometerSensor != null)
            sensorManager?.unregisterListener(accelerometerMagneticSensorListener)
        super.onPause()
    }

    // Accessibility announcing helpers on when the phone is headed on a specific direction
    private var northAnnouncer = SensorEventAnnouncer(R.string.north)
    private var eastAnnouncer = SensorEventAnnouncer(R.string.east, false)
    private var westAnnouncer = SensorEventAnnouncer(R.string.west, false)
    private var southAnnouncer = SensorEventAnnouncer(R.string.south, false)
    private var qiblaAnnouncer = SensorEventAnnouncer(R.string.qibla, false)
    private fun checkIfA11yAnnounceIsNeeded(angle: Float) {
        val binding = binding ?: return
        northAnnouncer.check(binding.root.context, isNearToDegree(0f, angle))
        eastAnnouncer.check(binding.root.context, isNearToDegree(90f, angle))
        southAnnouncer.check(binding.root.context, isNearToDegree(180f, angle))
        westAnnouncer.check(binding.root.context, isNearToDegree(270f, angle))
        if (coordinates != null) {
            val qiblaAngle = binding.compassView.qiblaHeading
            qiblaAnnouncer.check(binding.root.context, isNearToDegree(qiblaAngle, angle))
        }
    }

    companion object {
        fun isNearToDegree(compareTo: Float, degree: Float): Boolean {
            val difference = abs(degree - compareTo)
            return if (difference > 180) 360 - difference < 3f else difference < 3f
        }
    }
}
