package com.byagowi.persiancalendar.ui.level

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.SensorEventAnnouncer
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.utils.debugLog

@Composable
fun Level(
    isStopped: Boolean,
    angleToShow1: MutableFloatState,
    angleToShow2: MutableFloatState,
    setShowTwoAngles: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    var updateToken by remember { mutableLongStateOf(0) }
    val resources = LocalResources.current
    val levelView = remember {
        LevelView(resources, angleToShow1, angleToShow2, setShowTwoAngles) { ++updateToken }
    }
    val orientationProvider = remember {
        OrientationProvider(
            context,
            invalidate = { ++updateToken },
        ) { newOrientation, newPitch, newRoll, newBalance ->
            levelView.setOrientation(newOrientation, newPitch, newRoll, newBalance)
        }
    }
    val density = LocalDensity.current
    val announcer = remember { SensorEventAnnouncer(R.string.level) }
    val view = LocalView.current
    LaunchedEffect(isStopped) {
        var lastFeedback = -1L
        var previouslyIsLevel = false
        if (isStopped && orientationProvider.isListening) {
            levelView.onIsLevel = {}
            orientationProvider.stopListening()
        } else if (!orientationProvider.isListening) {
            levelView.onIsLevel = { isLevel ->
                if (isLevel && !previouslyIsLevel) {
                    val now = System.currentTimeMillis()
                    if (now - lastFeedback > 2000L) { // 2 seconds
                        view.performHapticFeedbackVirtualKey()
                    }
                    lastFeedback = now
                    previouslyIsLevel = true
                }
                if (!isLevel) previouslyIsLevel = false
                announcer.check(context, isLevel)
            }
            orientationProvider.startListening()
        }
    }
    BoxWithConstraints {
        val width = this.maxWidth
        val height = this.maxHeight
        LaunchedEffect(width, height) {
            with(density) { levelView.updateSize(width.roundToPx(), height.roundToPx()) }
        }
        Canvas(Modifier.fillMaxSize()) {
            updateToken.let {}
            levelView.draw(this.drawContext.canvas.nativeCanvas)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = LocalActivity.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
                if (!orientationProvider.isListening) orientationProvider.startListening()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                debugLog("level: ON_PAUSE")
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                if (orientationProvider.isListening) orientationProvider.stopListening()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            if (orientationProvider.isListening) orientationProvider.stopListening()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
