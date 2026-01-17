package com.byagowi.persiancalendar.ui.map

import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Matrix
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.SocialDistance
import androidx.compose.material.icons.filled._3dRotation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_MAP
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_TIME_BAR
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.showQibla
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.common.DatePickerDialog
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ZoomableView
import com.byagowi.persiancalendar.ui.settings.locationathan.location.CoordinatesDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.GPSLocationDialog
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform
import com.byagowi.persiancalendar.ui.utils.performLongPress
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.math.abs
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.MapScreen(
    navigateUp: () -> Unit,
    fromSettings: Boolean,
    initialTime: Long,
) {
    val resources = LocalResources.current
    val mapDraw = remember(resources) { MapDraw(resources) }

    val time = rememberSaveable { mutableLongStateOf(initialTime) }
    var mapType by rememberSaveable { mutableStateOf(MapType.DAY_NIGHT) }
    var displayLocation by rememberSaveable { mutableStateOf(true) }
    var displayGrid by rememberSaveable { mutableStateOf(false) }
    var isDirectPathMode by rememberSaveable { mutableStateOf(false) }
    var markedCoordinates by remember { mutableStateOf(coordinates) }
    var dialogInputCoordinates by remember { mutableStateOf<Coordinates?>(null) }
    var directPathDestination by remember { mutableStateOf<Coordinates?>(null) }

    LaunchedEffect(key1 = coordinates) { markedCoordinates = coordinates }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1.minutes)
            time.longValue += 1.minutes.inWholeMilliseconds
        }
    }

    var showGpsDialog by rememberSaveable { mutableStateOf(false) }
    if (showGpsDialog) GPSLocationDialog { showGpsDialog = false }

    var showCoordinatesDialog by rememberSaveable { mutableStateOf(false) }
    var saveCoordinates by rememberSaveable { mutableStateOf(fromSettings) }
    if (showCoordinatesDialog) CoordinatesDialog(
        inputCoordinates = dialogInputCoordinates,
        isFromMap = true,
        onDismissRequest = { showCoordinatesDialog = false },
        saveCoordinates = saveCoordinates,
        toggleSaveCoordinates = { saveCoordinates = it },
        notifyChange = {
            markedCoordinates = it
            displayLocation = true
        },
    )

    var showMapTypesDialog by rememberSaveable { mutableStateOf(false) }
    if (showMapTypesDialog) AppDialog(onDismissRequest = { showMapTypesDialog = false }) {
        MapType.entries.drop(1) // Hide "None" option
            // Hide moon visibilities for now unless is a development build
            .filter { !it.isCrescentVisibility || BuildConfig.DEVELOPMENT }.forEach {
                Text(
                    language.mapType(it) ?: stringResource(it.title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showMapTypesDialog = false
                            mapType = it
                        }
                        .padding(vertical = 16.dp, horizontal = 24.dp),
                )
            }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    class MenuItem(
        val icon: ImageVector,
        @get:StringRes val titleId: Int,
        val isEnabled: () -> Boolean = { false },
        val onClick: () -> Unit,
    ) {
        val title
            @Composable get() = language.mapButtons(titleId) ?: stringResource(titleId)
    }

    val context = LocalContext.current
    val menu = listOf(
        MenuItem(
            icon = Icons.Default._3dRotation,
            titleId = R.string.show_globe_view_label,
        ) onClick@{
            val textureSize = 2048
            val bitmap =
                runCatching { createBitmap(textureSize, textureSize) }.onFailure(logException)
                    .getOrNull() ?: return@onClick
            val matrix = Matrix()
            matrix.setScale(
                textureSize.toFloat() / mapDraw.mapWidth,
                textureSize.toFloat() / mapDraw.mapHeight,
            )
            mapDraw.draw(
                canvas = Canvas(bitmap),
                matrix = matrix,
                displayLocation = displayLocation,
                coordinates = markedCoordinates,
                directPathDestination = directPathDestination,
                displayGrid = displayGrid,
            )
            showGlobeDialog(context, bitmap, lifecycleOwner.lifecycle)
            // DO NOT use bitmap after this
        },
        MenuItem(
            icon = Icons.Default.SocialDistance,
            titleId = R.string.show_direct_path_label,
            isEnabled = { isDirectPathMode },
        ) {
            if (markedCoordinates == null) showGpsDialog = true else {
                directPathDestination = directPathDestination.takeIf { !isDirectPathMode }
                isDirectPathMode = !isDirectPathMode
            }
        },
        MenuItem(
            icon = Icons.Default.Grid3x3,
            titleId = R.string.show_grid_label,
            isEnabled = { displayGrid },
        ) { displayGrid = !displayGrid },
        MenuItem(
            icon = Icons.Default.MyLocation,
            titleId = R.string.show_my_location_label,
        ) { showGpsDialog = true },
        MenuItem(
            icon = Icons.Default.LocationOn,
            titleId = R.string.show_location_label,
            isEnabled = { markedCoordinates != null && displayLocation },
        ) {
            if (markedCoordinates == null) {
                showGpsDialog = true
            } else displayLocation = !displayLocation
        },
        MenuItem(
            icon = Icons.Default.NightlightRound,
            titleId = R.string.show_night_mask_label,
            isEnabled = { mapType != MapType.NONE },
        ) { if (mapType == MapType.NONE) showMapTypesDialog = true else mapType = MapType.NONE },
    )
    var formattedTime by remember { mutableStateOf("") }
    Box {
        // Best effort solution for landscape view till figuring out something better
        if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE) Column {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
            Spacer(Modifier.height((16 + menuHeight + 16).dp))
            ScreenSurface { Box(Modifier.fillMaxSize()) }
        }
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = SHARED_CONTENT_KEY_MAP),
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                    boundsTransform = appBoundsTransform,
                ),
            factory = {
                val root = ZoomableView(it)
                root.contentWidth = mapDraw.mapWidth.toFloat()
                root.contentHeight = mapDraw.mapHeight.toFloat()
                root.maxScale = 512f
                root.contentDescription = it.getString(R.string.map)
                root.onClick = { x: Float, y: Float ->
                    val latitude = 90 - y / mapDraw.mapScaleFactor
                    val longitude = x / mapDraw.mapScaleFactor - 180
                    if (abs(latitude) < 90 && abs(longitude) < 180) {
                        // Easter egg like feature, bring sky renderer fragment
                        if (abs(latitude) < 2 && abs(longitude) < 2 && displayGrid) {
                            Toast.makeText(context, "Null Island!", Toast.LENGTH_SHORT).show()
                        } else {
                            val coords = Coordinates(latitude.toDouble(), longitude.toDouble(), 0.0)
                            if (isDirectPathMode) directPathDestination = coords else {
                                dialogInputCoordinates = coords
                                showCoordinatesDialog = true
                            }
                        }
                    }
                }
                root
            },
            update = {
                displayLocation.let {}
                markedCoordinates.let {}
                directPathDestination.let {}
                displayGrid.let {}

                it.onDraw = { canvas, matrix ->
                    mapDraw.draw(
                        canvas = canvas,
                        matrix = matrix,
                        displayLocation = displayLocation,
                        coordinates = markedCoordinates,
                        directPathDestination = directPathDestination,
                        displayGrid = displayGrid,
                    )
                }
                mapDraw.drawKaaba = coordinates != null && displayLocation && showQibla
                mapDraw.updateMap(time.longValue, mapType)
                formattedTime = mapDraw.maskFormattedTime
                it.invalidate()
            },
        )
    }

    Column {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
        Row(
            Modifier
                .alpha(.85f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.extraLarge)
                .height(menuHeight.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavigationRailItem(
                modifier = Modifier.weight(1f),
                selected = false,
                onClick = navigateUp,
                icon = {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_up),
                    )
                },
            )
            menu.forEach {
                NavigationRailItem(
                    modifier = Modifier.weight(1f),
                    onClick = it.onClick,
                    selected = false,
                    icon = {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                TooltipAnchorPosition.Above,
                            ),
                            tooltip = { PlainTooltip { Text(it.title) } },
                            state = rememberTooltipState(),
                        ) {
                            val tint by animateColor(
                                if (it.isEnabled()) MaterialTheme.colorScheme.inversePrimary
                                else LocalContentColor.current,
                            )
                            Icon(it.icon, it.title, tint = tint)
                        }
                    },
                )
            }
        }
    }

    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    if (showDatePickerDialog) {
        val currentJdn = Jdn(Date(time.longValue).toGregorianCalendar().toCivilDate())
        DatePickerDialog(currentJdn, { showDatePickerDialog = false }) { jdn ->
            time.longValue += (jdn - currentJdn).days.inWholeMilliseconds
        }
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = formattedTime.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(all = 16.dp)
                    .height(46.dp)
                    .fillMaxWidth(),
            ) {
                TimeArrow(mapDraw, time, isPrevious = true)
                AnimatedContent(
                    modifier = Modifier.weight(1f, fill = false),
                    targetState = formattedTime,
                    transitionSpec = appCrossfadeSpec,
                ) { state ->
                    Text(
                        state,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { showDatePickerDialog = true },
                                onClickLabel = stringResource(R.string.select_date),
                                onLongClick = { time.longValue = System.currentTimeMillis() },
                                onLongClickLabel = stringResource(R.string.today),
                            )
                            .sharedElement(
                                rememberSharedContentState(key = SHARED_CONTENT_KEY_TIME_BAR),
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                boundsTransform = appBoundsTransform,
                            ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                TimeArrow(mapDraw, time, isPrevious = false)
            }
        }
    }
}

@Composable
private fun TimeArrow(mapDraw: MapDraw, time: MutableState<Long>, isPrevious: Boolean) {
    val hapticFeedback = LocalHapticFeedback.current
    Icon(
        if (isPrevious) Icons.AutoMirrored.Default.KeyboardArrowLeft
        else Icons.AutoMirrored.Default.KeyboardArrowRight,
        contentDescription = stringResource(
            if (isPrevious) R.string.previous_x else R.string.next_x,
            stringResource(R.string.day),
        ),
        Modifier.combinedClickable(
            indication = ripple(bounded = false),
            interactionSource = null,
            onClick = {
                hapticFeedback.performLongPress()
                time.value += run {
                    val amount = if (isPrevious) -1 else 1
                    if (mapDraw.currentMapType.isCrescentVisibility) amount.days else amount.hours
                }.inWholeMilliseconds
            },
            onClickLabel = stringResource(R.string.select_day),
            onLongClick = { time.value += (if (isPrevious) -10 else 10).days.inWholeMilliseconds },
        ),
        tint = MaterialTheme.colorScheme.primary,
    )
}

private const val menuHeight = 56
