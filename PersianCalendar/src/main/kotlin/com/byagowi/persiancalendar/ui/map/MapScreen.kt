package com.byagowi.persiancalendar.ui.map

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.SocialDistance
import androidx.compose.material.icons.filled._3dRotation
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withScale
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_MAP
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_TIME_BAR
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.showQibla
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.DatePickerDialog
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.TimeArrow
import com.byagowi.persiancalendar.ui.common.appTransformable
import com.byagowi.persiancalendar.ui.settings.locationathan.location.CoordinatesDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.GPSLocationDialog
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.math.abs
import kotlin.math.min
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun SharedTransitionScope.MapScreen(
    navigateUp: () -> Unit,
    fromSettings: Boolean,
    initialTime: Long,
    today: Jdn,
) {
    val resources = LocalResources.current
    val mapDraw = remember(resources) { MapDraw(resources) }

    val timeInMillis = rememberSaveable { mutableLongStateOf(initialTime) }
    var mapType by rememberSaveable { mutableStateOf(MapType.DAY_NIGHT) }
    var displayLocation by rememberSaveable { mutableStateOf(true) }
    var displayGrid by rememberSaveable { mutableStateOf(false) }
    var isDirectPathMode by rememberSaveable { mutableStateOf(false) }
    var markedCoordinates by remember { mutableStateOf(coordinates) }
    var dialogInputCoordinates by remember { mutableStateOf<Coordinates?>(null) }
    var directPathDestination by remember { mutableStateOf<Coordinates?>(null) }

    LaunchedEffect(key1 = coordinates) { markedCoordinates = coordinates }

    LaunchedEffect(Unit) {
        val interval = 1.minutes.inWholeMilliseconds
        while (true) {
            delay(timeMillis = interval)
            timeInMillis.longValue += interval
        }
    }

    var showGpsDialog by rememberSaveable { mutableStateOf(false) }
    if (showGpsDialog) GPSLocationDialog { showGpsDialog = false }

    var showGlobeView by rememberSaveable { mutableStateOf(false) }

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

    val context = LocalContext.current
    var formattedTime by remember { mutableStateOf("") }
    Box {
        Column {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
            Spacer(Modifier.height((16 + menuHeight + 16).dp))
            ScreenSurface { Box(Modifier.fillMaxSize()) }
        }
        val mapString = stringResource(R.string.map)
        val scale = rememberSaveable { mutableFloatStateOf(1f) }
        val offsetX = rememberSaveable { mutableFloatStateOf(0f) }
        val offsetY = rememberSaveable { mutableFloatStateOf(0f) }
        if (!showGlobeView) Canvas(
            modifier = Modifier
                .semantics { this.contentDescription = mapString }
                .fillMaxSize()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = SHARED_CONTENT_KEY_MAP),
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                    boundsTransform = appBoundsTransform,
                )
                .appTransformable(
                    scale = scale,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    disableHorizontalLimit = true,
                    contentSize = { (width: Float, height: Float) ->
                        Size(width = width, height = min(width / 2, height))
                    },
                    scaleRange = 1f..512f,
                    onClick = { x: Float, y: Float, canvasSize: Size ->
                        val mapSize = min(canvasSize.width / 2, canvasSize.height)
                        val contentScale = mapSize / mapDraw.mapHeight
                        val translateY = (canvasSize.height - mapSize) / 2f
                        val mapX = x.mod(mapSize * 2) / contentScale
                        val mapY = (y - translateY) / contentScale
                        val latitude = 90 - mapY / mapDraw.mapScaleFactor
                        val longitude = mapX / mapDraw.mapScaleFactor - 180
                        if (abs(latitude) < 90 && abs(longitude) < 180) {
                            if (abs(latitude) < 2 && abs(longitude) < 2 && displayGrid) {
                                Toast.makeText(context, "Null Island!", Toast.LENGTH_SHORT).show()
                            } else {
                                val coords =
                                    Coordinates(latitude.toDouble(), longitude.toDouble(), 0.0)
                                if (isDirectPathMode) directPathDestination = coords else {
                                    dialogInputCoordinates = coords
                                    showCoordinatesDialog = true
                                }
                            }
                        }
                    },
                )
                .graphicsLayer {
                    this.scaleX = scale.floatValue
                    this.scaleY = scale.floatValue
                    val (width, height) = this.size
                    val mapSize = min(width / 2, height)
                    this.translationX = offsetX.floatValue.mod(mapSize * 2 * scale.floatValue)
                    this.translationY = offsetY.floatValue
                },
        ) {
            val (width, height) = this.size
            val mapSize = min(width / 2, height)
            val contentScale = mapSize / mapDraw.mapHeight
            mapDraw.drawKaaba = coordinates != null && displayLocation && showQibla
            mapDraw.updateMap(timeInMillis.longValue, mapType)
            formattedTime = mapDraw.maskFormattedTime
            repeat(if (width > height) 3 else 2) { tileIndex ->
                translate(left = (tileIndex - 1) * mapSize * 2, top = (height - mapSize) / 2) {
                    scale(contentScale, pivot = Offset.Zero) {
                        mapDraw.draw(
                            canvas = this.drawContext.canvas.nativeCanvas,
                            scale = scale.floatValue * contentScale,
                            displayLocation = displayLocation,
                            coordinates = markedCoordinates,
                            directPathDestination = directPathDestination,
                            displayGrid = displayGrid,
                        )
                    }
                }
            }
        }
    }

    val globeTextureSize = 2048
    if (showGlobeView) runCatching {
        createBitmap(globeTextureSize, globeTextureSize)
    }.onFailure(logException).onFailure {
        showGlobeView = false
    }.getOrNull()?.let { bitmap ->
        mapDraw.drawKaaba = coordinates != null && displayLocation && showQibla
        mapDraw.updateMap(timeInMillis.longValue, mapType)
        formattedTime = mapDraw.maskFormattedTime
        bitmap.applyCanvas {
            val scale = globeTextureSize / 2f / mapDraw.mapHeight
            withScale(x = scale, y = scale * 2) {
                mapDraw.draw(
                    canvas = this,
                    scale = scale,
                    displayLocation = displayLocation,
                    coordinates = markedCoordinates,
                    directPathDestination = directPathDestination,
                    displayGrid = displayGrid,
                )
            }
        }
        GlobeView(bitmap) { showGlobeView = false }
        DisposableEffect(key1 = bitmap) { onDispose { bitmap.recycle() } }
    }

    Column {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
        ) {
            Row(
                Modifier
                    .alpha(.85f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .height(menuHeight.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    NavigationNavigateUpIcon {
                        if (showGlobeView) showGlobeView = false else navigateUp()
                    }
                }

                @Composable
                fun MenuItem(
                    icon: ImageVector,
                    @StringRes titleId: Int,
                    modifier: Modifier = Modifier,
                    isEnabled: Boolean = false,
                    onClick: () -> Unit,
                ) {
                    val title = language.mapButtons(stringId = titleId) ?: stringResource(titleId)
                    Box(modifier.weight(weight = 1f)) {
                        val tint by animateColor(
                            color = if (isEnabled) {
                                MaterialTheme.colorScheme.inversePrimary
                            } else LocalContentColor.current,
                        )
                        CompositionLocalProvider(LocalContentColor provides tint) {
                            AppIconButton(icon, title, onClick = onClick)
                        }
                    }
                }

                MenuItem(
                    icon = Icons.Default._3dRotation,
                    titleId = R.string.show_globe_view_label,
                    isEnabled = showGlobeView,
                ) { showGlobeView = !showGlobeView }

                MenuItem(
                    icon = Icons.Default.SocialDistance,
                    titleId = R.string.show_direct_path_label,
                    isEnabled = isDirectPathMode,
                    modifier = Modifier.alpha(
                        alpha = animateFloatAsState(
                            targetValue = if (showGlobeView) .25f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        ).value,
                    ),
                ) {
                    if (showGlobeView) return@MenuItem
                    if (markedCoordinates == null) showGpsDialog = true else {
                        directPathDestination =
                            directPathDestination.takeIf { !isDirectPathMode }
                        isDirectPathMode = !isDirectPathMode
                    }
                }

                MenuItem(
                    icon = Icons.Default.Grid3x3,
                    titleId = R.string.show_grid_label,
                    isEnabled = displayGrid,
                ) { displayGrid = !displayGrid }

                MenuItem(
                    icon = Icons.Default.MyLocation,
                    titleId = R.string.show_my_location_label,
                ) { showGpsDialog = true }

                MenuItem(
                    icon = Icons.Default.LocationOn,
                    titleId = R.string.show_location_label,
                    isEnabled = markedCoordinates != null && displayLocation,
                ) {
                    if (markedCoordinates == null) {
                        showGpsDialog = true
                    } else displayLocation = !displayLocation
                }

                MenuItem(
                    icon = Icons.Default.NightlightRound,
                    titleId = R.string.show_night_mask_label,
                    isEnabled = mapType != MapType.NONE,
                ) {
                    if (mapType == MapType.NONE) {
                        showMapTypesDialog = true
                    } else mapType = MapType.NONE
                }
            }
        }
    }

    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    if (showDatePickerDialog) {
        val currentJdn = Jdn(Date(timeInMillis.longValue).toGregorianCalendar().toCivilDate())
        DatePickerDialog(
            initialJdn = currentJdn,
            onDismissRequest = { showDatePickerDialog = false },
            today = today,
        ) { jdn -> timeInMillis.longValue += (jdn - currentJdn).days.inWholeMilliseconds }
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
                    .padding(vertical = 16.dp, horizontal = 8.dp)
                    .height(46.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(.75f), CircleShape)
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
            ) {
                TimeArrow(mapType, timeInMillis, isPrevious = true)
                AnimatedContent(
                    modifier = Modifier.weight(1f, fill = false),
                    targetState = formattedTime,
                    transitionSpec = appCrossfadeSpec,
                ) { state ->
                    Text(
                        text = state,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { showDatePickerDialog = true },
                                onClickLabel = stringResource(R.string.select_date),
                                onLongClick = {
                                    timeInMillis.longValue = System.currentTimeMillis()
                                },
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
                TimeArrow(mapType, timeInMillis, isPrevious = false)
            }
        }
    }
}

@Composable
private fun SharedTransitionScope.TimeArrow(
    mapType: MapType,
    timeInMillis: MutableLongState,
    isPrevious: Boolean,
) {
    TimeArrow(
        onClick = {
            timeInMillis.longValue += run {
                val amount = if (isPrevious) -1 else 1
                if (mapType.isCrescentVisibility) amount.days else amount.hours
            }.inWholeMilliseconds
        },
        onClickLabel = stringResource(
            if (isPrevious) R.string.previous_x else R.string.next_x,
            pluralStringResource(R.plurals.hours, 1, numeral.format(1)),
        ),
        onLongClick = {
            timeInMillis.longValue += (if (isPrevious) -10 else 10).days.inWholeMilliseconds
        },
        onLongClickLabel = stringResource(
            if (isPrevious) R.string.previous_x else R.string.next_x,
            pluralStringResource(R.plurals.days, 10, numeral.format(10)),
        ),
        isPrevious = isPrevious,
    )
}

private const val menuHeight = 56
