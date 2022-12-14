package com.byagowi.persiancalendar.ui.map

import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.core.graphics.createBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentMapBinding
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.astronomy.AstronomyViewModel
import com.byagowi.persiancalendar.ui.common.ArrowView
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showCoordinatesDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showGPSLocationDialog
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupLayoutTransition
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.byagowi.persiancalendar.ui.utils.viewKeeper
import com.byagowi.persiancalendar.utils.appPrefs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue

class MapScreen : Fragment(R.layout.fragment_map) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMapBinding.bind(view)
        val directPathButton by viewKeeper { binding.toolbar.menu.findItem(R.id.menu_direct_path) }
        val gridButton by viewKeeper { binding.toolbar.menu.findItem(R.id.menu_grid) }
        val myLocationButton by viewKeeper { binding.toolbar.menu.findItem(R.id.menu_my_location) }
        val locationButton by viewKeeper { binding.toolbar.menu.findItem(R.id.menu_location) }
        val mapTypeButton by viewKeeper { binding.toolbar.menu.findItem(R.id.menu_map_type) }
        val globeViewButton by viewKeeper { binding.toolbar.menu.findItem(R.id.menu_globe_view) }

        val viewModel by navGraphViewModels<MapViewModel>(R.id.map)

        // Don't set the title as we got lots of icons
        // binding.toolbar.setTitle(R.string.map)
        binding.toolbar.setupUpNavigation()

        // Set time from Astronomy screen state if we are brought from the screen to here directly
        if (findNavController().previousBackStackEntry?.destination?.id == R.id.astronomy) {
            val astronomyViewModel by navGraphViewModels<AstronomyViewModel>(R.id.astronomy)
            viewModel.changeToTime(astronomyViewModel.astronomyState.value.date.time)
            // Let's apply changes here to astronomy screen's view model also
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.state
                    .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                    .collectLatest { state -> astronomyViewModel.changeToTime(state.time) }
            }
        }

        val mapDraw = MapDraw(view.context)

        binding.startArrow.rotateTo(ArrowView.Direction.START)
        binding.startArrow.setOnClickListener {
            binding.startArrow.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            if (mapDraw.currentMapType.isCrescentVisibility) viewModel.subtractOneDay()
            else viewModel.subtractOneHour()
        }
        binding.startArrow.setOnLongClickListener {
            viewModel.subtractTenDays()
            true
        }
        binding.endArrow.rotateTo(ArrowView.Direction.END)
        binding.endArrow.setOnClickListener {
            binding.endArrow.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            if (mapDraw.currentMapType.isCrescentVisibility) viewModel.addOneDay()
            else viewModel.addOneHour()
        }
        binding.endArrow.setOnLongClickListener {
            viewModel.addTenDays()
            true
        }

        binding.toolbar.inflateMenu(R.menu.map_menu)
        fun bringGps() = activity?.let { showGPSLocationDialog(it, viewLifecycleOwner) }.let { }
        directPathButton.onClick {
            if (coordinates == null) bringGps() else viewModel.toggleDirectPathMode()
        }
        gridButton.onClick { viewModel.toggleDisplayGrid() }
        myLocationButton.onClick { bringGps() }
        locationButton.onClick {
            if (coordinates == null) bringGps() else viewModel.toggleDisplayLocation()
        }
        mapTypeButton.onClick {
            if (viewModel.state.value.mapType == MapType.None) {
                val context = context ?: return@onClick
                val options = enumValues<MapType>()
                    .drop(1) // Hide "None" option
                    // Hide moon visibilities for now unless is a development build
                    .filter { !it.isCrescentVisibility || BuildConfig.DEVELOPMENT }
                val titles = options.map { context.getString(it.title) }
                MaterialAlertDialogBuilder(context).setItems(titles.toTypedArray()) { dialog, i ->
                    viewModel.changeMapType(options[i])
                    dialog.dismiss()
                }.show()
            } else viewModel.changeMapType(MapType.None)
        }
        globeViewButton.onClick {
            val textureSize = 1024
            val bitmap = createBitmap(textureSize, textureSize)
            val matrix = Matrix().also {
                it.setScale(
                    textureSize.toFloat() / mapDraw.mapWidth,
                    textureSize.toFloat() / mapDraw.mapHeight
                )
            }
            binding.map.onDraw(Canvas(bitmap), matrix)
            showGlobeDialog(activity ?: return@onClick, bitmap)
        }

        binding.root.setupLayoutTransition()
        view.context.appPrefs.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == PREF_LATITUDE) viewModel.turnOnDisplayLocation()
        }

        binding.map.onClick = { x: Float, y: Float ->
            val latitude = 90 - y / mapDraw.mapScaleFactor
            val longitude = x / mapDraw.mapScaleFactor - 180
            if (abs(latitude) < 90 && abs(longitude) < 180) {
                // Easter egg like feature, bring sky renderer fragment
                if (latitude.absoluteValue < 2 && longitude.absoluteValue < 2 && viewModel.state.value.displayGrid) {
                    findNavController().navigateSafe(MapScreenDirections.actionMapToSkyRenderer())
                } else {
                    val coordinates = Coordinates(latitude.toDouble(), longitude.toDouble(), 0.0)
                    if (viewModel.state.value.isDirectPathMode)
                        viewModel.changeDirectPathDestination(coordinates)
                    else
                        activity?.let { showCoordinatesDialog(it, viewLifecycleOwner, coordinates) }
                }
            }
        }

        binding.map.onDraw = { canvas, matrix ->
            val state = viewModel.state.value
            mapDraw.draw(
                canvas, matrix, state.displayLocation, state.directPathDestination,
                state.displayGrid
            )
        }
        binding.map.contentWidth = mapDraw.mapWidth.toFloat()
        binding.map.contentHeight = mapDraw.mapHeight.toFloat()
        binding.map.maxScale = 512f

        // Setup view model change listener
        // https://developer.android.com/topic/libraries/architecture/coroutines#lifecycle-aware
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest { state ->
                    mapDraw.updateMap(state.time, state.mapType)
                    binding.map.invalidate()
                    binding.date.text = mapDraw.maskFormattedTime
                    binding.timeBar.isVisible = mapDraw.maskFormattedTime.isNotEmpty()
                    directPathButton.icon?.alpha = if (state.isDirectPathMode) 127 else 255
                }
        }
    }
}
