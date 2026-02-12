package com.byagowi.persiancalendar.ui.level

import android.content.pm.ActivityInfo
import android.view.Surface
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.SensorEventAnnouncer
import com.byagowi.persiancalendar.utils.debugLog

@Composable
fun Level(
    isStopped: Boolean,
    angleToShow1: MutableFloatState,
    angleToShow2: MutableFloatState,
    showTwoAngles: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var orientationProvider by remember { mutableStateOf<OrientationProvider?>(null) }
    val resources = LocalResources.current
    var updateToken by remember { mutableLongStateOf(0) }
    val levelView = remember {
        LevelView(resources, angleToShow1, angleToShow2, showTwoAngles) { ++updateToken }
    }
    val density = LocalDensity.current
    LaunchedEffect(activity) {
        activity?.let { activity ->
            orientationProvider = OrientationProvider(
                activity,
                invalidate = { ++updateToken },
            ) { newOrientation, newPitch, newRoll, newBalance ->
                levelView.setOrientation(newOrientation, newPitch, newRoll, newBalance)
            }
        }
    }
    val announcer = remember { SensorEventAnnouncer(R.string.level) }
    LaunchedEffect(orientationProvider, isStopped) {
        val provider = orientationProvider ?: return@LaunchedEffect
        if (isStopped && provider.isListening) {
            levelView.onIsLevel = {}
            provider.stopListening()
        } else if (!provider.isListening) {
            levelView.onIsLevel = { isLevel -> announcer.check(context, isLevel) }
            provider.startListening()
        }
    }
    BoxWithConstraints {
        val width = this.maxWidth
        val height = this.maxHeight
        with(density) { levelView.updateSize(width.roundToPx(), height.roundToPx()) }
        Canvas(Modifier.fillMaxSize()) {
            updateToken.let {}
            levelView.draw(this.drawContext.canvas.nativeCanvas)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        activity ?: return@DisposableEffect onDispose {}
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                debugLog("level: ON_RESUME")
                // Rotation lock, https://stackoverflow.com/a/75984863
                val destination = ActivityCompat.getDisplayOrDefault(activity).rotation
                activity.requestedOrientation = when (destination) {
                    Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
                if (orientationProvider?.isListening == false) {
                    orientationProvider?.startListening()
                }
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                debugLog("level: ON_PAUSE")
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                if (orientationProvider?.isListening == true) orientationProvider?.stopListening()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
