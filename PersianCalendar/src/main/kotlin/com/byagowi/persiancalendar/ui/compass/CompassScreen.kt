package com.byagowi.persiancalendar.ui.compass

import android.animation.ValueAnimator
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_SHOW_QIBLA_IN_COMPASS
import com.byagowi.persiancalendar.PREF_TRUE_NORTH_IN_COMPASS
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.common.StopButton
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.SensorEventAnnouncer
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.TEN_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.THIRTY_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.cityName
import com.byagowi.persiancalendar.utils.formatCoordinateISO6709
import kotlinx.coroutines.delay
import java.util.Date
import java.util.GregorianCalendar
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

// Lots of bad practices, should be rewritten sometime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreen(
    openDrawer: () -> Unit,
    navigateToLevel: () -> Unit,
    navigateToMap: () -> Unit,
    activity: ComponentActivity,
) {
    val orientation = remember(LocalConfiguration.current) {
        when (activity.getSystemService<WindowManager>()?.defaultDisplay?.rotation) {
            android.view.Surface.ROTATION_0 -> 0f
            android.view.Surface.ROTATION_90 -> 90f
            android.view.Surface.ROTATION_180 -> 180f
            android.view.Surface.ROTATION_270 -> 270f
            else -> 0f
        }
    }
    var sensorNotFound by remember { mutableStateOf(false) }
    var timeShift by remember { mutableStateOf(0f) }
    var isTimeShiftAnimate by remember { mutableStateOf(false) }
    val timeShiftAnimate by animateFloatAsState(
        if (isTimeShiftAnimate) 24f else 0f,
        animationSpec = tween(durationMillis = if (isTimeShiftAnimate) TEN_SECONDS_IN_MILLIS.toInt() else 0),
        label = "timeShift",
    ) {
        if (isTimeShiftAnimate) {
            timeShift = 0f
            isTimeShiftAnimate = false
        }
    }
    val context = LocalContext.current
    val prefs = remember { context.appPrefs }
    val cityName = remember {
        prefs.cityName ?: coordinates.value?.run {
            formatCoordinateISO6709(latitude, longitude, elevation.takeIf { it != 0.0 })
        }
    }
    val sliderValue by derivedStateOf { if (isTimeShiftAnimate) timeShiftAnimate else timeShift }
    val isSliderShown by derivedStateOf { sliderValue != 0f }
    var baseTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(null) {
        while (true) {
            delay(THIRTY_SECONDS_IN_MILLIS)
            baseTime = Date()
        }
    }
    val time by derivedStateOf {
        GregorianCalendar().also {
            it.time = baseTime
            it.add(GregorianCalendar.MINUTE, (sliderValue * 60f).roundToInt())
        }
    }
    var isStopped by remember { mutableStateOf(false) }
    // Ugly, for now
    var compassView by remember { mutableStateOf<CompassView?>(null) }
    Column {
        // TODO: Ideally this should be onPrimary
        val colorOnAppBar = Color(context.resolveColor(R.attr.colorOnAppBar))
        TopAppBar(
            title = {
                Column {
                    Text(if (isSliderShown) Clock(time).toBasicFormatString() else stringResource(R.string.compass))
                    if (cityName != null) Text(
                        cityName,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                navigationIconContentColor = colorOnAppBar,
                actionIconContentColor = colorOnAppBar,
                titleContentColor = colorOnAppBar,
            ),
            navigationIcon = {
                IconButton(onClick = { openDrawer() }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(R.string.open_drawer)
                    )
                }
            },
            actions = {
                val coordinates by coordinates.collectAsState()
                if (coordinates != null) TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(stringResource(R.string.show_sun_and_moon_path_in_24_hours))
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = { isTimeShiftAnimate = true }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_in_24_hours),
                            contentDescription = stringResource(
                                R.string.show_sun_and_moon_path_in_24_hours
                            ),
                        )
                    }
                }
                var showMenu by rememberSaveable { mutableStateOf(false) }
                if (cityName != null || BuildConfig.DEVELOPMENT) IconButton(
                    onClick = { showMenu = !showMenu },
                ) {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip { Text(text = stringResource(R.string.more_options)) }
                        },
                        state = rememberTooltipState()
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more_options),
                        )
                    }
                }
                var showTrueNorth by rememberSaveable {
                    mutableStateOf(prefs.getBoolean(PREF_TRUE_NORTH_IN_COMPASS, false))
                }
                var showQibla by rememberSaveable {
                    mutableStateOf(prefs.getBoolean(PREF_SHOW_QIBLA_IN_COMPASS, true))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuCheckableItem(stringResource(R.string.true_north), showTrueNorth) {
                        showTrueNorth = it
                        showMenu = false
                        compassView?.isTrueNorth = it
                    }
                    DropdownMenuCheckableItem(stringResource(R.string.qibla), showQibla) {
                        showQibla = it
                        showMenu = false
                        compassView?.isShowQibla = it
                        prefs.edit { putBoolean(PREF_SHOW_QIBLA_IN_COMPASS, it) }
                    }
                    if (BuildConfig.DEVELOPMENT) {
                        DropdownMenuItem(
                            text = { Text("Do a rotation") },
                            onClick = {
                                // Ugly, but is test only
                                val animator = ValueAnimator.ofFloat(0f, 1f)
                                animator.duration = TEN_SECONDS_IN_MILLIS
                                animator.addUpdateListener {
                                    compassView?.angle = it.animatedFraction * 360
                                }
                                if (Random.nextBoolean()) animator.start() else animator.reverse()
                                showMenu = false
                            },
                        )
                    }
                }
            },
        )
        Surface(shape = MaterialCornerExtraLargeTop()) {
            Box {
                Column {
                    AndroidView(
                        modifier = Modifier.weight(1f),
                        factory = {
                            val root = CompassView(it)
                            compassView = root
                            root
                        },
                        update = { it.setTime(time) },
                    )
                    BottomAppBar {
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = navigateToLevel) {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_level),
                                contentDescription = stringResource(R.string.level)
                            )
                        }
                        IconButton(onClick = navigateToMap) {
                            Icon(
                                Icons.Default.Map,
                                contentDescription = stringResource(R.string.map)
                            )
                        }
                        IconButton(onClick = {
                            showLongToast(
                                context, if (sensorNotFound) R.string.compass_not_found
                                else R.string.calibrate_compass_summary, 5000
                            )
                        }) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = stringResource(R.string.help)
                            )
                        }
                        Spacer(Modifier.weight(1f, fill = true))
                        StopButton(isStopped) { isStopped = it }
                        Spacer(Modifier.width(16.dp))
                    }
                }
            }
            AnimatedVisibility(visible = isSliderShown) {
                Slider(
                    valueRange = 0f..24f,
                    value = sliderValue,
                    onValueChange = {
                        isTimeShiftAnimate = false
                        timeShift = if (it == 24f) 0f else it
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }

    // Accessibility announcing helpers on when the phone is headed on a specific direction
    val checkIfA11yAnnounceIsNeeded = remember {
        val northAnnouncer = SensorEventAnnouncer(R.string.north)
        val eastAnnouncer = SensorEventAnnouncer(R.string.east, false)
        val westAnnouncer = SensorEventAnnouncer(R.string.west, false)
        val southAnnouncer = SensorEventAnnouncer(R.string.south, false)
        val qiblaAnnouncer = SensorEventAnnouncer(R.string.qibla, false);
        { angle: Float ->
            northAnnouncer.check(context, isNearToDegree(0f, angle))
            eastAnnouncer.check(context, isNearToDegree(90f, angle))
            southAnnouncer.check(context, isNearToDegree(180f, angle))
            westAnnouncer.check(context, isNearToDegree(270f, angle))
            compassView?.qiblaHeading?.heading?.also {
                qiblaAnnouncer.check(context, isNearToDegree(it, angle))
            }
            Unit
        }
    }

    val orientationSensorListener = remember {
        object : OrientationSensorListener() {
            override val compassView: CompassView? get() = compassView
            override val isStopped: Boolean get() = isStopped
            override val orientation: Float get() = orientation
            override fun checkIfA11yAnnounceIsNeeded(angle: Float) =
                checkIfA11yAnnounceIsNeeded(angle)
        }
    }
    val accelerometerMagneticSensorListener = remember {
        object : AccelerometerMagneticSensorListener() {
            override val compassView: CompassView? get() = compassView
            override val isStopped: Boolean get() = isStopped
            override val orientation: Float get() = orientation
            override fun checkIfA11yAnnounceIsNeeded(angle: Float) =
                checkIfA11yAnnounceIsNeeded(angle)
        }
    }
    var sensorManager by remember { mutableStateOf<SensorManager?>(null) }
    var accelerometerSensor by remember { mutableStateOf<Sensor?>(null) }
    var magnetometerSensor by remember { mutableStateOf<Sensor?>(null) }
    var orientationSensor by remember { mutableStateOf<Sensor?>(null) }

    LocalLifecycleOwner.current.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            sensorManager = activity.getSystemService<SensorManager>()
            accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magnetometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            orientationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)

            if (orientationSensor != null) {
                sensorManager?.registerListener(
                    orientationSensorListener, orientationSensor, SensorManager.SENSOR_DELAY_FASTEST
                )
                if (BuildConfig.DEVELOPMENT) Toast.makeText(
                    context, "dev: orientation", Toast.LENGTH_LONG
                ).show()
                if (coordinates.value == null) showLongToast(
                    context, R.string.set_location, Toast.LENGTH_SHORT
                )
            } else if (accelerometerSensor != null && magnetometerSensor != null) {
                sensorManager?.registerListener(
                    accelerometerMagneticSensorListener,
                    accelerometerSensor,
                    SensorManager.SENSOR_DELAY_GAME
                )
                sensorManager?.registerListener(
                    accelerometerMagneticSensorListener,
                    magnetometerSensor,
                    SensorManager.SENSOR_DELAY_GAME
                )
                if (BuildConfig.DEVELOPMENT) Toast.makeText(
                    context, "dev: acc+magnet", Toast.LENGTH_LONG
                ).show()
                if (coordinates.value == null) showLongToast(
                    context, R.string.set_location, Toast.LENGTH_SHORT
                )
            } else {
                showLongToast(context, R.string.compass_not_found, Toast.LENGTH_SHORT)
                sensorNotFound = true
            }
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            if (orientationSensor != null) sensorManager?.unregisterListener(
                orientationSensorListener
            )
            else if (accelerometerSensor != null && magnetometerSensor != null) sensorManager?.unregisterListener(
                accelerometerMagneticSensorListener
            )
        }
    })
}

private abstract class BaseSensorListener : SensorEventListener {
    /*
     * time smoothing constant for low-pass filter 0 ≤ alpha ≤ 1 ; a smaller
     * value basically means more smoothing See:
     * https://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     */
    private val ALPHA = 0.15f
    private var azimuth: Float = 0f

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    abstract val compassView: CompassView?
    abstract val isStopped: Boolean
    abstract val orientation: Float
    abstract fun checkIfA11yAnnounceIsNeeded(angle: Float)

    protected fun update(value: Float) {
        // angle between the magnetic north direction
        // 0=North, 90=East, 180=South, 270=West
        val angle = if (isStopped) 0f else value + orientation
        if (!isStopped) checkIfA11yAnnounceIsNeeded(angle)
        azimuth = lowPass(angle, azimuth)
        compassView?.angle = azimuth
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

private abstract class OrientationSensorListener : BaseSensorListener() {
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        update(event.values[0])
    }
}

private abstract class AccelerometerMagneticSensorListener : BaseSensorListener() {
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

        if (isAccelerationsAvailable && isMagneticFieldAvailable && SensorManager.getRotationMatrix(
                rotationMatrix, null, acceleration, magneticField
            )
        ) {
            SensorManager.getOrientation(rotationMatrix, orientationMatrix)
            update(Math.toDegrees(orientationMatrix[0].toDouble()).toFloat())
            isAccelerationsAvailable = false
            isMagneticFieldAvailable = false
        }
    }
}

// TODO: Snackbar should be better here, just make sure it will support long messages
private fun showLongToast(context: Context, @StringRes messageId: Int, duration: Int) {
    Toast.makeText(context, messageId, duration).show()
}

@Composable
private fun DropdownMenuCheckableItem(
    text: String,
    isChecked: Boolean,
    setChecked: (Boolean) -> Unit,
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Text(text)
                Spacer(
                    Modifier
                        .weight(1f)
                        .width(16.dp),
                )
                Checkbox(checked = isChecked, onCheckedChange = null)
            }
        },
        onClick = { setChecked(!isChecked) },
    )
}

@VisibleForTesting
fun isNearToDegree(compareTo: Float, degree: Float): Boolean {
    val difference = abs(degree - compareTo)
    return if (difference > 180) 360 - difference < 3f else difference < 3f
}
