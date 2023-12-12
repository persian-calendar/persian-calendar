package com.byagowi.persiancalendar.ui.map

import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_SHOW_QIBLA_IN_COMPASS
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.astronomy.AstronomyViewModel
import com.byagowi.persiancalendar.ui.calendar.dialogs.DayPickerDialog
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.common.ZoomableView
import com.byagowi.persiancalendar.ui.settings.locationathan.location.CoordinatesDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.GPSLocationDialog
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackLongPress
import com.byagowi.persiancalendar.utils.ONE_MINUTE_IN_MILLIS
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.abs

class MapFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = ComposeView(inflater.context)

        // Just that our UI tests don't have access to the nav controllers, let's don't access nav there
        val ifNavAvailable = runCatching { findNavController() }.getOrNull() != null
        val viewModel =
            if (ifNavAvailable) navGraphViewModels<MapViewModel>(R.id.map).value else MapViewModel()
        // Set time from Astronomy screen state if we are brought from the screen to here directly
        if (ifNavAvailable && findNavController().previousBackStackEntry?.destination?.id == R.id.astronomy) {
            val astronomyViewModel by navGraphViewModels<AstronomyViewModel>(R.id.astronomy)
            viewModel.changeToTime(astronomyViewModel.astronomyState.value.date.time)
            // Let's apply changes here to astronomy screen's view model also
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.state.flowWithLifecycle(
                    viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED
                ).collectLatest { state -> astronomyViewModel.changeToTime(state.time) }
            }
        }

        root.setContent { AppTheme { MapScreen(viewModel) { findNavController().navigateUp() } } }
        return root
    }
}

private const val menuHeight = 56

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MapViewModel, popNavigation: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val coord by coordinates.collectAsState()
    val context = LocalContext.current
    val mapDraw = remember { MapDraw(context) }

    LaunchedEffect(null) { coordinates.collectLatest { viewModel.turnOnDisplayLocation() } }

    LaunchedEffect(null) {
        while (true) {
            delay(ONE_MINUTE_IN_MILLIS)
            viewModel.addOneMinute()
        }
    }

    var showGpsDialog by rememberSaveable { mutableStateOf(false) }
    if (showGpsDialog) GPSLocationDialog { showGpsDialog = false }

    var clickedCoordinates by remember { mutableStateOf<Coordinates?>(null) }
    var showCoordinatesDialog by rememberSaveable { mutableStateOf(false) }
    if (showCoordinatesDialog) CoordinatesDialog(
        inputCoordinates = clickedCoordinates,
        onDismissRequest = { showCoordinatesDialog = false },
    )

    var showMapTypesDialog by remember { mutableStateOf(false) }
    if (showMapTypesDialog) AppDialog(onDismissRequest = { showMapTypesDialog = false }) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            MapType.entries.drop(1) // Hide "None" option
                // Hide moon visibilities for now unless is a development build
                .filter { !it.isCrescentVisibility || BuildConfig.DEVELOPMENT }
                .forEach {
                    Text(
                        stringResource(it.title),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMapTypesDialog = false
                                viewModel.changeMapType(it)
                            }
                            .padding(vertical = 16.dp, horizontal = 24.dp)
                    )
                }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val menu = listOf<Triple<ImageVector, @StringRes Int, () -> Unit>>(
        Triple(Icons.Default._3dRotation, R.string.show_globe_view_label) onClick@{
            val textureSize = 2048
            val bitmap =
                runCatching { createBitmap(textureSize, textureSize) }.onFailure(logException)
                    .getOrNull() ?: return@onClick
            val matrix = Matrix()
            matrix.setScale(
                textureSize.toFloat() / mapDraw.mapWidth,
                textureSize.toFloat() / mapDraw.mapHeight
            )
            mapDraw.draw(
                Canvas(bitmap),
                matrix,
                state.displayLocation,
                state.directPathDestination,
                state.displayGrid
            )
            showGlobeDialog(context, bitmap, lifecycleOwner.lifecycle)
            // DO NOT use bitmap after this
        },
        Triple(Icons.Default.SocialDistance, R.string.show_direct_path_label) {
            if (coordinates.value == null) showGpsDialog = true
            else viewModel.toggleDirectPathMode()
        },
        Triple(Icons.Default.Grid3x3, R.string.show_grid_label) {
            viewModel.toggleDisplayGrid()
        },
        Triple(Icons.Default.MyLocation, R.string.show_my_location_label) { showGpsDialog = true },
        Triple(Icons.Default.LocationOn, R.string.show_location_label) {
            if (coordinates.value == null) showGpsDialog = true
            else viewModel.toggleDisplayLocation()
        },
        Triple(Icons.Default.NightlightRound, R.string.show_night_mask_label) {
            if (viewModel.state.value.mapType == MapType.None) showMapTypesDialog = true
            else viewModel.changeMapType(MapType.None)
        },
    )
    val showKaaba = remember { context.appPrefs.getBoolean(PREF_SHOW_QIBLA_IN_COMPASS, true) }
    var formattedTime by remember { mutableStateOf("") }
    Box {
        // Best effort solution for landscape view till figuring out something better
        if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            Column {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
                Spacer(Modifier.height((16 + menuHeight + 16).dp))
                Surface(Modifier.fillMaxSize(), shape = MaterialCornerExtraLargeTop()) {}
            }
        }
        AndroidView(
            modifier = Modifier.fillMaxSize(),
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
                        if (abs(latitude) < 2 && abs(longitude) < 2 && viewModel.state.value.displayGrid) {
                            Toast.makeText(context, "Null Island!", Toast.LENGTH_SHORT).show()
                        } else {
                            val coordinates =
                                Coordinates(latitude.toDouble(), longitude.toDouble(), 0.0)
                            if (viewModel.state.value.isDirectPathMode) viewModel.changeDirectPathDestination(
                                coordinates
                            )
                            else {
                                clickedCoordinates = coordinates
                                showCoordinatesDialog = true
                            }
                        }
                    }
                }
                root
            },
            update = {
                it.onDraw = { canvas, matrix ->
                    mapDraw.draw(
                        canvas,
                        matrix,
                        state.displayLocation,
                        state.directPathDestination,
                        state.displayGrid
                    )
                }
                mapDraw.drawKaaba = coord != null && state.displayLocation && showKaaba
                mapDraw.updateMap(state.time, state.mapType)
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
                onClick = popNavigation,
                icon = {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_up),
                    )
                },
            )
            menu.forEach { (icon, title, action) ->
                NavigationRailItem(
                    modifier = Modifier.weight(1f),
                    onClick = action,
                    selected = false,
                    icon = {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = { PlainTooltip { Text(text = stringResource(title)) } },
                            state = rememberTooltipState()
                        ) {
                            Icon(
                                icon,
                                contentDescription = stringResource(title),
                                // We need more than Triple or defining a new class, oh well
                                tint = if (when (title) {
                                        R.string.show_grid_label -> state.displayGrid
                                        R.string.show_location_label -> coord != null && state.displayLocation
                                        R.string.show_direct_path_label -> state.isDirectPathMode
                                        R.string.show_night_mask_label -> state.mapType != MapType.None
                                        else -> false
                                    }
                                ) MaterialTheme.colorScheme.inversePrimary else LocalContentColor.current
                            )
                        }
                    },
                )
            }
        }
    }

    var showDayPickerDialog by rememberSaveable { mutableStateOf(false) }
    if (showDayPickerDialog) {
        val currentJdn = Jdn(
            Date(viewModel.state.value.time).toGregorianCalendar().toCivilDate()
        )
        DayPickerDialog(currentJdn, R.string.accept, { jdn ->
            viewModel.addDays(jdn - currentJdn)
        }) { showDayPickerDialog = false }
    }

    AnimatedVisibility(visible = formattedTime.isNotEmpty()) {
        Box {
            @OptIn(ExperimentalFoundationApi::class) Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .safeDrawingPadding()
                    .height(46.dp)
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
            ) {
                val view = LocalView.current
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    contentDescription = null,
                    Modifier.combinedClickable(
                        onClick = {
                            view.performHapticFeedbackLongPress()
                            if (mapDraw.currentMapType.isCrescentVisibility) viewModel.addDays(-1)
                            else viewModel.subtractOneHour()
                        },
                        onClickLabel = stringResource(
                            R.string.previous_x, stringResource(R.string.day)
                        ),
                        onLongClick = { viewModel.addDays(-10) },
                    ),
                    tint = MaterialTheme.colorScheme.primary,
                )
                AnimatedContent(
                    modifier = Modifier.weight(1f, fill = false),
                    targetState = formattedTime,
                    label = "time"
                ) { state ->
                    Text(
                        state,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { showDayPickerDialog = true },
                                onClickLabel = stringResource(R.string.goto_date),
                                onLongClick = { viewModel.changeToTime(Date()) },
                                onLongClickLabel = stringResource(R.string.today),
                            )
                    )
                }
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = null,
                    Modifier.combinedClickable(
                        onClick = {
                            view.performHapticFeedbackLongPress()
                            if (mapDraw.currentMapType.isCrescentVisibility) viewModel.addDays(1)
                            else viewModel.addOneHour()
                        },
                        onClickLabel = stringResource(
                            R.string.next_x, stringResource(R.string.day)
                        ),
                        onLongClick = { viewModel.addDays(10) },
                    ),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
