package com.byagowi.persiancalendar.ui.compass

import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentCompassBinding
import com.byagowi.persiancalendar.ui.DrawerHost
import com.byagowi.persiancalendar.utils.a11yAnnounceAndClick
import com.byagowi.persiancalendar.utils.coordinates
import com.byagowi.persiancalendar.utils.getCityName
import com.byagowi.persiancalendar.utils.getCompatDrawable
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.navigateSafe
import com.byagowi.persiancalendar.utils.onClick
import com.google.android.material.snackbar.Snackbar
import kotlin.math.abs

/**
 * Compass/Qibla activity
 */
class CompassFragment : Fragment() {

    var stopped = false
    private var binding: FragmentCompassBinding? = null
    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private var orientation = 0f
    private var sensorNotFound = false

    private val compassListener = object : SensorEventListener {
        /*
         * time smoothing constant for low-pass filter 0 ≤ alpha ≤ 1 ; a smaller
         * value basically means more smoothing See:
         * http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
         */
        val ALPHA = 0.15f
        var azimuth: Float = 0f

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {
            // angle between the magnetic north direction
            // 0=North, 90=East, 180=South, 270=West
            if (event == null) return
            val angle = if (stopped) 0f else event.values[0] + orientation
            if (!stopped) checkIfA11yAnnounceIsNeeded(angle)
            azimuth = lowPass(angle, azimuth)
            binding?.compassView?.angle = azimuth
        }

        /**
         * https://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
         * http://developer.android.com/reference/android/hardware/SensorEvent.html#values
         */
        private fun lowPass(input: Float, output: Float): Float = when {
            abs(180 - input) > 170 -> input
            else -> output + ALPHA * (input - output)
        }
    }

    private fun showLongSnackbar(@StringRes messageId: Int, duration: Int) {
        val rootView = view ?: return
        Snackbar.make(rootView, messageId, duration).apply {
            view.setOnClickListener { dismiss() }
            view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines = 5
            anchorView = binding?.fab
        }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentCompassBinding.inflate(inflater, container, false).also { binding ->
        this.binding = binding

        binding.appBar.toolbar.let { toolbar ->
            toolbar.setTitle(R.string.compass)
            toolbar.subtitle = getCityName(inflater.context, true)
            (activity as? DrawerHost)?.setupToolbarWithDrawer(viewLifecycleOwner, toolbar)
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
        updateCompassOrientation()
    }.root

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
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        when {
            sensor != null -> {
                sensorManager?.registerListener(
                    compassListener, sensor, SensorManager.SENSOR_DELAY_FASTEST
                )
                if (coordinates == null) showLongSnackbar(
                    R.string.set_location,
                    Snackbar.LENGTH_SHORT
                )
            }
            else -> {
                showLongSnackbar(R.string.compass_not_found, Snackbar.LENGTH_SHORT)
                sensorNotFound = true
            }
        }
    }

    override fun onPause() {
        if (sensor != null) sensorManager?.unregisterListener(compassListener)
        super.onPause()
    }

    // A11y
    // deliberately true
    private var isCurrentlyNorth = true
    private var isCurrentlyEast = true
    private var isCurrentlyWest = true
    private var isCurrentlySouth = true
    private var isCurrentlyQibla = true
    private fun checkIfA11yAnnounceIsNeeded(angle: Float) {
        val binding = binding ?: return
        fun checkIsReachedToDegree(
            currentState: Boolean, @StringRes directionString: Int, directionAngle: Int
        ): Boolean {
            return if (isNearToDegree(directionAngle.toFloat(), angle)) {
                if (!currentState) a11yAnnounceAndClick(binding.root, directionString)
                true
            } else false
        }
        isCurrentlyNorth = checkIsReachedToDegree(isCurrentlyNorth, R.string.north, 0)
        isCurrentlyEast = checkIsReachedToDegree(isCurrentlyEast, R.string.east, 90)
        isCurrentlySouth = checkIsReachedToDegree(isCurrentlySouth, R.string.south, 180)
        isCurrentlyWest = checkIsReachedToDegree(isCurrentlyWest, R.string.west, 270)
        if (coordinates != null)
            isCurrentlyQibla = checkIsReachedToDegree(isCurrentlyQibla, R.string.west, 270)
    }

    companion object {
        fun isNearToDegree(compareTo: Float, degree: Float): Boolean {
            val difference = abs(degree - compareTo)
            return if (difference > 180) 360 - difference < 3f else difference < 3f
        }
    }
}
