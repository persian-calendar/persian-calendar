package com.byagowi.persiancalendar.ui.level

import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_COMPASS
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_STOP
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.AppBottomAppBar
import com.byagowi.persiancalendar.ui.common.AppFloatingActionButton
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.StopButton
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.ExtraLargeShapeCornerSize
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun SharedTransitionScope.LevelScreen(
    navigateUp: () -> Unit,
    navigateToCompass: () -> Unit,
) {
    val isStopped = rememberSaveable { mutableStateOf(false) }
    var cmInchFlip by rememberSaveable { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    val activity = LocalActivity.current

    if (isFullscreen) DisposableEffect(Unit) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    Column {
        AnimatedVisibility(visible = !isFullscreen) {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                title = { Text(stringResource(R.string.level)) },
                colors = appTopAppBarColors(),
                navigationIcon = { NavigationNavigateUpIcon(navigateUp) },
                actions = {
                    run {
                        val rotation by animateFloatAsState(
                            targetValue = if (cmInchFlip) 180f else 0f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        )
                        AppIconButton(
                            icon = Icons.Default.SyncAlt,
                            title = language.centimeter + " / " + language.inch,
                            modifier = Modifier.rotate(rotation),
                        ) { cmInchFlip = !cmInchFlip }
                    }
                    AppIconButton(
                        icon = Icons.Default.Fullscreen,
                        title = stringResource(R.string.full_screen),
                    ) { isFullscreen = true }
                },
            )
        }

        val topCornersRoundness by animateDpAsState(
            targetValue = if (isFullscreen) 0.dp else ExtraLargeShapeCornerSize.dp,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        )
        var everWentFullscreen by remember { mutableStateOf(false) }
        if (isFullscreen) everWentFullscreen = true
        ScreenSurface(
            shape = MaterialTheme.shapes.large.copy(
                topStart = CornerSize(topCornersRoundness),
                topEnd = CornerSize(topCornersRoundness),
                bottomStart = ZeroCornerSize,
                bottomEnd = ZeroCornerSize,
            ),
            disableSharedContent = everWentFullscreen,
        ) {
            Box {
                Crossfade(targetState = cmInchFlip) { cmInchFlip ->
                    RulerView(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (isFullscreen) Modifier.safeDrawingPadding()
                                else Modifier.padding(top = topCornersRoundness),
                            ),
                        cmInchFlip = cmInchFlip,
                        isFullscreen = isFullscreen,
                    )
                }
                Column {
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(horizontal = 24.dp)
                            .then(if (isFullscreen) Modifier.safeDrawingPadding() else Modifier),
                    ) { Level(isStopped.value) }
                    AnimatedVisibility(visible = !isFullscreen) {
                        AppBottomAppBar {
                            AppIconButton(
                                icon = Icons.Default.Explore,
                                title = stringResource(R.string.compass),
                                modifier = Modifier.sharedBounds(
                                    rememberSharedContentState(key = SHARED_CONTENT_KEY_COMPASS),
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    boundsTransform = appBoundsTransform,
                                ),
                                onClick = navigateToCompass,
                            )
                            Spacer(Modifier.weight(1f))
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

                var bottomWindowInset by remember { mutableStateOf(0.dp) }
                if (!isFullscreen) bottomWindowInset = with(LocalDensity.current) {
                    WindowInsets.systemBars.getBottom(this).toDp()
                }

                if (isFullscreen) Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = bottomWindowInset + 16.dp),
                ) { StopButton(isStopped) }

                ShrinkingFloatingActionButton(
                    Modifier
                        .align(Alignment.TopCenter)
                        .safeGesturesPadding()
                        .padding(top = 32.dp),
                    isVisible = isFullscreen,
                    action = { isFullscreen = false },
                    icon = Icons.Default.FullscreenExit,
                    title = stringResource(R.string.exit_full_screen),
                )
            }
        }
    }
}

@Composable
private fun ShrinkingFloatingActionButton(
    modifier: Modifier,
    isVisible: Boolean,
    action: () -> Unit,
    icon: ImageVector,
    title: String,
) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = scaleIn(),
        exit = scaleOut(),
    ) {
        AppFloatingActionButton(onClick = action) {
            var showLabel by rememberSaveable { mutableStateOf(true) }
            Row(
                Modifier.padding(horizontal = if (showLabel) 16.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = title)
                LaunchedEffect(Unit) {
                    // Matches https://github.com/aosp-mirror/platform_frameworks_base/blob/1dcde70/services/core/java/com/android/server/notification/NotificationManagerService.java#L382
                    delay(3.5.seconds)
                    showLabel = false
                }
                AnimatedVisibility(showLabel) { Text(title) }
            }
        }
    }
}
