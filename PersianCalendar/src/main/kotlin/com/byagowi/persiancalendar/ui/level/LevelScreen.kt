package com.byagowi.persiancalendar.ui.level

import android.content.pm.ActivityInfo
import android.os.PowerManager
import android.view.Surface
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.getSystemService
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.ShrinkingFloatingActionButton
import com.byagowi.persiancalendar.ui.common.StopButton
import com.byagowi.persiancalendar.ui.utils.ExtraLargeShapeCornerSize
import com.byagowi.persiancalendar.ui.utils.SensorEventAnnouncer
import com.byagowi.persiancalendar.ui.utils.getActivity
import com.byagowi.persiancalendar.utils.FIFTEEN_MINUTES_IN_MILLIS
import com.byagowi.persiancalendar.variants.debugLog
import java.util.UUID

@Composable
fun LevelScreen(navigateUp: () -> Unit, navigateToCompass: () -> Unit) {
    var isStopped by remember { mutableStateOf(false) }
    var orientationProvider by remember { mutableStateOf<OrientationProvider?>(null) }
    val announcer = remember { SensorEventAnnouncer(R.string.level) }
    var cmInchFlip by remember { mutableStateOf(false) }
    var fullscreenToken by remember { mutableStateOf<UUID?>(null) }
    val isFullscreen by derivedStateOf { fullscreenToken != null }
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(null) {
        val activity = context.getActivity() ?: return@DisposableEffect onDispose {}
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                debugLog("level: ON_RESUME")
                // Rotation lock, https://stackoverflow.com/a/75984863
                val destination =
                    @Suppress("DEPRECATION") activity.windowManager?.defaultDisplay?.rotation
                activity.requestedOrientation = when (destination) {
                    Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
                if (orientationProvider?.isListening == false && !isStopped) {
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

    if (fullscreenToken != null) DisposableEffect(fullscreenToken) {
        val lock = context.getSystemService<PowerManager>()
            ?.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "persiancalendar:level")
        lock?.acquire(FIFTEEN_MINUTES_IN_MILLIS)

        val activity = context.getActivity() ?: return@DisposableEffect onDispose {}
        val windowInsetsController =
            WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        onDispose {
            lock?.release()
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    Column {
        AnimatedVisibility(visible = !isFullscreen) {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                title = { Text(stringResource(R.string.level)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = LocalContentColor.current,
                    actionIconContentColor = LocalContentColor.current,
                    titleContentColor = LocalContentColor.current,
                ),
                navigationIcon = {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_up)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { cmInchFlip = !cmInchFlip }) {
                        Icon(
                            imageVector = Icons.Default.SyncAlt,
                            contentDescription = "cm / in",
                        )
                    }
                    IconButton(onClick = { fullscreenToken = UUID.randomUUID() }) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = stringResource(R.string.full_screen),
                        )
                    }
                },
            )
        }

        val topCornersRoundness by animateDpAsState(
            if (isFullscreen) 0.dp else ExtraLargeShapeCornerSize.dp,
            animationSpec = tween(
                durationMillis = integerResource(android.R.integer.config_longAnimTime),
                easing = LinearEasing
            ),
            label = "corner",
        )
        Surface(
            shape = MaterialTheme.shapes.large.copy(
                topStart = CornerSize(topCornersRoundness),
                topEnd = CornerSize(topCornersRoundness),
                bottomStart = ZeroCornerSize,
                bottomEnd = ZeroCornerSize,
            )
        ) {
            Box {
                AnimatedContent(targetState = cmInchFlip, label = "ruler") { state ->
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (isFullscreen) Modifier.safeDrawingPadding()
                                else Modifier.padding(top = topCornersRoundness)
                            ),
                        factory = ::RulerView,
                        update = { it.cmInchFlip = state },
                    )
                }
                Column {
                    AndroidView(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .then(if (isFullscreen) Modifier.safeDrawingPadding() else Modifier),
                        factory = {
                            val levelView = LevelView(it)
                            context.getActivity()?.let { activity ->
                                orientationProvider = OrientationProvider(activity, levelView)
                            }
                            levelView
                        },
                        update = update@{ levelView ->
                            val provider = orientationProvider ?: return@update
                            if (isStopped && provider.isListening) {
                                levelView.onIsLevel = {}
                                provider.stopListening()
                            } else if (!provider.isListening) {
                                levelView.onIsLevel =
                                    { isLevel -> announcer.check(context, isLevel) }
                                provider.startListening()
                            }
                        },
                    )
                    AnimatedVisibility(visible = !isFullscreen) {
                        BottomAppBar {
                            Spacer(Modifier.width(8.dp))
                            AnimatedVisibility(visible = !isFullscreen) {
                                IconButton(onClick = navigateToCompass) {
                                    Icon(
                                        Icons.Default.Explore,
                                        contentDescription = stringResource(R.string.compass)
                                    )
                                }
                            }
                            Spacer(Modifier.weight(1f, fill = true))
                            StopButton(isStopped) { isStopped = it }
                            Spacer(Modifier.width(16.dp))
                        }
                    }
                }
                if (isFullscreen) Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(horizontal = 20.dp, vertical = 32.dp),
                ) { StopButton(isStopped) { isStopped = it } }

                ShrinkingFloatingActionButton(
                    Modifier
                        .align(Alignment.TopCenter)
                        .safeGesturesPadding()
                        .padding(top = 32.dp),
                    isVisible = isFullscreen,
                    action = { fullscreenToken = null },
                    icon = Icons.Default.FullscreenExit,
                    title = stringResource(R.string.exit_full_screen),
                )
            }
        }
    }
}
