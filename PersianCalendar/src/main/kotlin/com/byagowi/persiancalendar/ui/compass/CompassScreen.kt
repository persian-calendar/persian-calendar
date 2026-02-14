package com.byagowi.persiancalendar.ui.compass

import android.animation.ValueAnimator
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_COMPASS_SET_LOCATION_IGNORED
import com.byagowi.persiancalendar.PREF_SHOW_QIBLA_IN_COMPASS
import com.byagowi.persiancalendar.PREF_TRUE_NORTH_IN_COMPASS
import com.byagowi.persiancalendar.QIBLA_LATITUDE
import com.byagowi.persiancalendar.QIBLA_LONGITUDE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_COMPASS
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_LEVEL
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_MAP
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_STOP
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.EarthPosition
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.showQibla
import com.byagowi.persiancalendar.global.showTrueNorth
import com.byagowi.persiancalendar.ui.common.AngleDisplay
import com.byagowi.persiancalendar.ui.common.AppBottomAppBar
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuCheckableItem
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.ChangesHapticFeedback
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.NavigationOpenNavigationRailIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.StopButton
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.icons.In24HoursIcon
import com.byagowi.persiancalendar.ui.theme.appSliderColor
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.SensorEventAnnouncer
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform
import com.byagowi.persiancalendar.ui.utils.appContentSizeAnimationSpec
import com.byagowi.persiancalendar.utils.debugLog
import com.byagowi.persiancalendar.utils.formatCoordinateISO6709
import com.byagowi.persiancalendar.utils.preferences
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import java.util.GregorianCalendar
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@Composable
fun SharedTransitionScope.CompassScreen(
    openNavigationRail: () -> Unit,
    navigateToLevel: () -> Unit,
    navigateToMap: () -> Unit,
    navigateToSettingsLocationTab: () -> Unit,
    noBackStackAction: (() -> Unit)?,
    today: Jdn,
    now: Long,
) {
    val context = LocalContext.current
    val orientation = remember(LocalConfiguration.current) {
        when (ActivityCompat.getDisplayOrDefault(context).rotation) {
            android.view.Surface.ROTATION_0 -> 0f
            android.view.Surface.ROTATION_90 -> 90f
            android.view.Surface.ROTATION_180 -> 180f
            android.view.Surface.ROTATION_270 -> 270f
            else -> 0f
        }
    }
    var sensorNotFound by remember { mutableStateOf(false) }
    var timeShift by remember { mutableFloatStateOf(0f) }
    var isTimeShiftAnimate by remember { mutableStateOf(false) }
    val timeShiftAnimate by animateFloatAsState(
        targetValue = if (isTimeShiftAnimate) 24f else 0f,
        animationSpec = tween(durationMillis = if (isTimeShiftAnimate) 10.seconds.inWholeMilliseconds.toInt() else 0),
    ) {
        if (isTimeShiftAnimate) {
            timeShift = 0f
            isTimeShiftAnimate = false
        }
    }
    val coordinates = coordinates
    val sliderValue = if (isTimeShiftAnimate) timeShiftAnimate else timeShift
    val isSliderShown = sliderValue != 0f
    val time = GregorianCalendar().also {
        it.timeInMillis = now
        it.add(GregorianCalendar.MINUTE, (sliderValue * 60f).roundToInt())
    }
    val isStopped = rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    fun showSnackbarMessage(message: String, duration: SnackbarDuration) {
        coroutineScope.launch { snackbarHostState.showSnackbar(message, duration = duration) }
    }

    val resources = LocalResources.current

    fun showSetLocationMessage() {
        coroutineScope.launch {
            when (snackbarHostState.showSnackbar(
                resources.getString(R.string.set_location),
                duration = SnackbarDuration.Long,
                actionLabel = resources.getString(R.string.settings),
                withDismissAction = true,
            )) {
                SnackbarResult.ActionPerformed -> navigateToSettingsLocationTab()
                SnackbarResult.Dismissed -> context.preferences.edit {
                    putBoolean(PREF_COMPASS_SET_LOCATION_IGNORED, true)
                }
            }
        }
    }

    val angle = rememberSaveable { mutableFloatStateOf(0f) }
    ChangesHapticFeedback(angle)
    val qiblaHeading = coordinates?.run {
        val qibla = EarthPosition(QIBLA_LATITUDE, QIBLA_LONGITUDE)
        EarthPosition(latitude, longitude).toEarthHeading(qibla)
    }

    val declination = remember(coordinates, now) {
        derivedStateOf {
            coordinates?.let {
                GeomagneticField(
                    it.latitude.toFloat(),
                    it.longitude.toFloat(),
                    it.elevation.toFloat(),
                    now,
                ).declination
            } ?: 0f
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                title = {
                    Column {
                        Text(
                            if (isSliderShown) Clock(time).toBasicFormatString() else stringResource(
                                R.string.compass,
                            ),
                        )
                        val subtitle = cityName ?: coordinates?.run {
                            formatCoordinateISO6709(
                                latitude,
                                longitude,
                                elevation.takeIf { it != 0.0 },
                            )
                        }
                        if (subtitle != null) SelectionContainer {
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                },
                colors = appTopAppBarColors(),
                navigationIcon = {
                    if (noBackStackAction != null) NavigationNavigateUpIcon(noBackStackAction)
                    else NavigationOpenNavigationRailIcon(openNavigationRail)
                },
                actions = {
                    if (coordinates != null) AppIconButton(
                        icon = In24HoursIcon,
                        title = stringResource(R.string.show_sun_and_moon_path_in_24_hours),
                        modifier = Modifier.rotate(sliderValue / 24f * 360f),
                    ) {
                        if (isTimeShiftAnimate) {
                            isTimeShiftAnimate = false
                            timeShift = 0f
                        } else isTimeShiftAnimate = true
                    }
                    if (coordinates != null) ThreeDotsDropdownMenu { closeMenu ->
                        AppDropdownMenuCheckableItem(
                            text = { Text(stringResource(R.string.true_north)) },
                            isChecked = showTrueNorth,
                        ) {
                            context.preferences.edit { putBoolean(PREF_TRUE_NORTH_IN_COMPASS, it) }
                            closeMenu()
                        }
                        AppDropdownMenuCheckableItem(
                            text = { Text(stringResource(R.string.qibla)) },
                            isChecked = showQibla,
                        ) {
                            closeMenu()
                            context.preferences.edit { putBoolean(PREF_SHOW_QIBLA_IN_COMPASS, it) }
                        }
                        if (isAstronomicalExtraFeaturesEnabled && language.isPersianOrDari) {
                            var value by remember { mutableStateOf<String?>(null) }
                            AppDropdownMenuItem(
                                {
                                    Crossfade(
                                        targetState = value ?: "مزبوره/مذکوره",
                                        Modifier.animateContentSize(appContentSizeAnimationSpec),
                                    ) { Text(it) }
                                },
                            ) {
                                val dayOfMonth = today.toIslamicDate().dayOfMonth
                                value = if (value == null) "مزبوره: " + when (dayOfMonth) {
                                    1, 9, 17, 25 -> "شرق"
                                    2, 10, 18, 26 -> "شمال شرق"
                                    3, 11, 19, 27 -> "شمال"
                                    4, 12, 20, 28 -> "شمال غرب"
                                    5, 13, 21, 29 -> "غرب"
                                    6, 14, 22, 30 -> "جنوب غرب"
                                    7, 15, 23 -> "جنوب"
                                    8, 16, 24 -> "جنوب شرق"
                                    else -> ""
                                } + "\nمذکوره: " + when (dayOfMonth) {
                                    1, 11, 21 -> "شرق"
                                    2, 12, 22 -> "جنوب شرق"
                                    3, 13, 23 -> "جنوب"
                                    4, 14, 24 -> "جنوب غرب"
                                    5, 15, 25 -> "غرب"
                                    6, 16, 26 -> "شمال غرب"
                                    7, 17, 27 -> "شمال"
                                    8, 18, 28 -> "شمال شرق"
                                    9, 19, 29 -> "تحت الارض"
                                    10, 20, 30 -> "فوق الارض"
                                    else -> ""
                                } else null
                            }
                        }
                        if (BuildConfig.DEVELOPMENT) {
                            AppDropdownMenuItem({ Text("Do a rotation") }) {
                                closeMenu()
                                // Ugly, but is test only
                                val animator = ValueAnimator.ofFloat(0f, 1f)
                                animator.duration = 10.seconds.inWholeMilliseconds
                                animator.addUpdateListener {
                                    angle.floatValue = it.animatedFraction * 360
                                }
                                if (Random.nextBoolean()) animator.start() else animator.reverse()
                            }
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            ScreenSurface {
                Column {
                    Box(Modifier.weight(1f, fill = false)) {
                        Box(
                            Modifier.sharedBounds(
                                rememberSharedContentState(key = SHARED_CONTENT_KEY_COMPASS),
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                boundsTransform = appBoundsTransform,
                            ),
                        ) {
                            Compass(
                                declination = declination.value,
                                qiblaHeading = qiblaHeading,
                                time = time,
                                angle = angle,
                            )
                        }
                        Column {
                            AnimatedVisibility(
                                visible = isSliderShown,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically(),
                            ) {
                                Slider(
                                    valueRange = 0f..24f,
                                    value = sliderValue,
                                    onValueChange = {
                                        isTimeShiftAnimate = false
                                        timeShift = if (it == 24f) 0f else it
                                    },
                                    colors = appSliderColor(),
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        }
                        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
                    }
                    AppBottomAppBar(overlay = { Angle(angle, declination.value) }) {
                        AppIconButton(
                            icon = ImageVector.vectorResource(R.drawable.ic_level),
                            title = stringResource(R.string.level),
                            modifier = Modifier.sharedBounds(
                                rememberSharedContentState(key = SHARED_CONTENT_KEY_LEVEL),
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                boundsTransform = appBoundsTransform,
                            ),
                            onClick = navigateToLevel,
                        )
                        AppIconButton(
                            icon = Icons.Default.Map,
                            title = stringResource(R.string.map),
                            modifier = Modifier.sharedBounds(
                                rememberSharedContentState(key = SHARED_CONTENT_KEY_MAP),
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                boundsTransform = appBoundsTransform,
                            ),
                            onClick = navigateToMap,
                        )
                        Spacer(Modifier.weight(1f))
                        AppIconButton(
                            icon = Icons.Default.Info,
                            title = stringResource(R.string.help),
                        ) {
                            if (coordinates == null) {
                                showSetLocationMessage()
                            } else showSnackbarMessage(
                                resources.getString(
                                    if (sensorNotFound) R.string.compass_not_found
                                    else R.string.calibrate_compass_summary,
                                ),
                                SnackbarDuration.Long,
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Box(
                            Modifier.sharedElement(
                                rememberSharedContentState(SHARED_CONTENT_KEY_STOP),
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                boundsTransform = appBoundsTransform,
                            ),
                        ) { StopButton(isStopped) }
                    }
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val sensorManager =
            context.getSystemService<SensorManager>() ?: return@DisposableEffect onDispose {}
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        @Suppress("DEPRECATION") val orientationSensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        // Accessibility announcing helpers on when the phone is headed on a specific direction
        val checkIfA11yAnnounceIsNeeded = run {
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
                qiblaHeading?.also {
                    qiblaAnnouncer.check(context, isNearToDegree(it.heading, angle))
                }
                Unit
            }
        }
        val orientationSensorListener = object : OrientationSensorListener() {
            override fun setAngle(value: Float) {
                angle.floatValue = value
            }

            override val getDeclination: Float get() = declination.value
            override val isStopped: Boolean get() = isStopped.value
            override val orientation: Float get() = orientation
            override fun checkIfA11yAnnounceIsNeeded(angle: Float) =
                checkIfA11yAnnounceIsNeeded(angle)
        }
        val accelerometerMagneticSensorListener = object : AccelerometerMagneticSensorListener() {
            override fun setAngle(value: Float) {
                angle.floatValue = value
            }

            override val getDeclination: Float get() = declination.value
            override val isStopped: Boolean get() = isStopped.value
            override val orientation: Float get() = orientation
            override fun checkIfA11yAnnounceIsNeeded(angle: Float) =
                checkIfA11yAnnounceIsNeeded(angle)
        }

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                debugLog("compass: ON_RESUME")
                if (coordinates == null) {
                    if (!context.preferences.getBoolean(PREF_COMPASS_SET_LOCATION_IGNORED, false)) {
                        showSetLocationMessage()
                    }
                }
                if (orientationSensor != null) {
                    sensorManager.registerListener(
                        orientationSensorListener,
                        orientationSensor,
                        SensorManager.SENSOR_DELAY_FASTEST,
                    )
                    if (BuildConfig.DEVELOPMENT) Toast.makeText(
                        context, "dev: orientation", Toast.LENGTH_SHORT,
                    ).show()
                } else if (accelerometerSensor != null && magnetometerSensor != null) {
                    sensorManager.registerListener(
                        accelerometerMagneticSensorListener,
                        accelerometerSensor,
                        SensorManager.SENSOR_DELAY_GAME,
                    )
                    sensorManager.registerListener(
                        accelerometerMagneticSensorListener,
                        magnetometerSensor,
                        SensorManager.SENSOR_DELAY_GAME,
                    )
                    if (BuildConfig.DEVELOPMENT) Toast.makeText(
                        context, "dev: acc+magnet", Toast.LENGTH_SHORT,
                    ).show()
                } else if (coordinates != null) {
                    showSnackbarMessage(
                        resources.getString(R.string.compass_not_found),
                        SnackbarDuration.Short,
                    )
                    sensorNotFound = true
                }
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                debugLog("compass: ON_PAUSE")
                if (orientationSensor != null) {
                    sensorManager.unregisterListener(orientationSensorListener)
                } else if (accelerometerSensor != null && magnetometerSensor != null) {
                    sensorManager.unregisterListener(accelerometerMagneticSensorListener)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@Composable
private fun Angle(angle: MutableFloatState, declination: Float) {
    val angleToDisplay by remember {
        derivedStateOf {
            (angle.floatValue + if (showTrueNorth) declination else 0f).roundToInt().mod(360)
        }
    }
    val context = LocalContext.current
    val angleDisplay = remember { AngleDisplay(context, "0", "888") }
    Canvas(Modifier.fillMaxSize()) {
        angleDisplay.updatePlacement(center.x.roundToInt(), center.y.roundToInt())
        angleDisplay.draw(drawContext.canvas.nativeCanvas, angleToDisplay.toFloat())
    }
}

private abstract class BaseSensorListener : SensorEventListener {
    /*
     * time smoothing constant for low-pass filter 0 ≤ alpha ≤ 1 ; a smaller
     * value basically means more smoothing See:
     * https://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     */
    private val alpha = 0.15f
    private var azimuth: Float = 0f

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    abstract fun setAngle(value: Float)
    abstract val isStopped: Boolean
    abstract val getDeclination: Float
    abstract val orientation: Float
    abstract fun checkIfA11yAnnounceIsNeeded(angle: Float)

    protected fun update(value: Float) {
        // angle between the magnetic north direction
        // 0=North, 90=East, 180=South, 270=West
        val angle = if (isStopped) -getDeclination else value + orientation
        if (!isStopped) checkIfA11yAnnounceIsNeeded(angle)
        azimuth = lowPass(angle, azimuth)
        setAngle(azimuth)
    }

    /**
     * https://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
     * https://developer.android.com/reference/android/hardware/SensorEvent.html#values
     */
    private fun lowPass(input: Float, output: Float): Float = when {
        abs(180 - input) > 170 -> input
        else -> output + alpha * (input - output)
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
                rotationMatrix, null, acceleration, magneticField,
            )
        ) {
            SensorManager.getOrientation(rotationMatrix, orientationMatrix)
            update(Math.toDegrees(orientationMatrix[0].toDouble()).toFloat())
            isAccelerationsAvailable = false
            isMagneticFieldAvailable = false
        }
    }
}

@VisibleForTesting
fun isNearToDegree(compareTo: Float, degree: Float): Boolean {
    val difference = abs(degree - compareTo)
    return if (difference > 180) 360 - difference < 3f else difference < 3f
}
