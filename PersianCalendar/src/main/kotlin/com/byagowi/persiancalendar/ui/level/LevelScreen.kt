package com.byagowi.persiancalendar.ui.level

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.getSystemService
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.ExtraLargeShapeCornerSize
import com.byagowi.persiancalendar.ui.utils.SensorEventAnnouncer
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.FIFTEEN_MINUTES_IN_MILLIS
import com.byagowi.persiancalendar.utils.THREE_SECONDS_AND_HALF_IN_MILLIS
import kotlinx.coroutines.delay
import java.util.UUID

class LevelFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = ComposeView(inflater.context)
        val activity = activity ?: return root
        root.setContent {
            AppTheme {
                LevelScreen(
                    activity,
                    popNavigation = { findNavController().navigateUp() },
                    navigateToCompass = {
                        // If compass wasn't in backstack (level is brought from shortcut), navigate to it
                        val controller = findNavController()
                        if (!controller.popBackStack(R.id.compass, false)) {
                            controller.navigateSafe(LevelFragmentDirections.actionLevelToCompass())
                        }
                    },
                )
            }
        }
        return root
    }
}

@Composable
fun LevelScreen(
    activity: ComponentActivity,
    popNavigation: () -> Unit,
    navigateToCompass: () -> Unit,
) {
    var isStopped by remember { mutableStateOf(false) }
    var orientationProvider by remember { mutableStateOf<OrientationProvider?>(null) }
    val announcer = remember { SensorEventAnnouncer(R.string.level) }
    var cmInchFlip by remember { mutableStateOf(false) }
    var fullscreenToken by remember { mutableStateOf<UUID?>(null) }
    val isFullscreen by derivedStateOf { fullscreenToken != null }
    val context = LocalContext.current

    LocalLifecycleOwner.current.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event != Lifecycle.Event.ON_PAUSE && event != Lifecycle.Event.ON_RESUME) return@LifecycleEventObserver

        // Rotation lock, https://stackoverflow.com/a/75984863
        val destination = if (event == Lifecycle.Event.ON_PAUSE) null else {
            @Suppress("DEPRECATION") activity.windowManager?.defaultDisplay?.rotation
        }
        activity.requestedOrientation = when (destination) {
            Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        if (event == Lifecycle.Event.ON_PAUSE) {
            if (orientationProvider?.isListening == true) orientationProvider?.stopListening()
        } else {
            if (orientationProvider?.isListening == false && !isStopped) orientationProvider?.startListening()
        }
    })

    if (fullscreenToken != null) DisposableEffect(fullscreenToken) {
        val lock = context.getSystemService<PowerManager>()
            ?.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "persiancalendar:level")
        lock?.acquire(FIFTEEN_MINUTES_IN_MILLIS)

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
            // TODO: Ideally this should be onPrimary
            val colorOnAppBar = Color(context.resolveColor(R.attr.colorOnAppBar))
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                title = { Text(stringResource(R.string.level)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = colorOnAppBar,
                    actionIconContentColor = colorOnAppBar,
                    titleContentColor = colorOnAppBar,
                ),
                navigationIcon = {
                    IconButton(onClick = popNavigation) {
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
            shape = RoundedCornerShape(
                topStart = topCornersRoundness,
                topEnd = topCornersRoundness,
                bottomStart = 0.dp,
                bottomEnd = 0.dp,
            )
        ) {
            Box {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (isFullscreen) Modifier.safeDrawingPadding()
                            else Modifier.padding(top = topCornersRoundness)
                        ),
                    factory = ::RulerView,
                    update = { it.cmInchFlip = cmInchFlip },
                )
                Column {
                    AndroidView(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .then(if (isFullscreen) Modifier.safeDrawingPadding() else Modifier),
                        factory = {
                            val levelView = LevelView(it)
                            orientationProvider = OrientationProvider(activity, levelView)
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
                                        ImageVector.vectorResource(R.drawable.ic_compass_menu),
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

                var showFullScreenLabel by remember { mutableStateOf(true) }
                if (isFullscreen) {
                    LaunchedEffect(null) {
                        delay(THREE_SECONDS_AND_HALF_IN_MILLIS)
                        showFullScreenLabel = false
                    }
                    FloatingActionButton(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .safeGesturesPadding()
                            .padding(top = 32.dp),
                        onClick = { fullscreenToken = null },
                        content = {
                            Row(
                                Modifier.padding(horizontal = if (showFullScreenLabel) 16.dp else 0.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.FullscreenExit,
                                    contentDescription = stringResource(R.string.exit_full_screen),
                                )
                                AnimatedVisibility(visible = showFullScreenLabel) {
                                    Text(stringResource(R.string.exit_full_screen))
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun StopButton(isStopped: Boolean, setStop: (Boolean) -> Unit) {
    FloatingActionButton(onClick = { setStop(!isStopped) }) {
        Icon(
            if (isStopped) Icons.Default.PlayArrow else Icons.Default.Stop,
            contentDescription = stringResource(
                if (isStopped) R.string.resume else R.string.stop
            )
        )
    }
}
