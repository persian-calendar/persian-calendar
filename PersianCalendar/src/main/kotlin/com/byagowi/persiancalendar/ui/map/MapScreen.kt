package com.byagowi.persiancalendar.ui.map

import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_SHOW_QIBLA_IN_COMPASS
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.MapScreenBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.astronomy.AstronomyViewModel
import com.byagowi.persiancalendar.ui.calendar.dialogs.showDayPickerDialog
import com.byagowi.persiancalendar.ui.common.ArrowView
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showCoordinatesDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showGPSLocationDialog
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.isLandscape
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackLongPress
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.setupLayoutTransition
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.abs

class MapScreen : Fragment(R.layout.map_screen) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = MapScreenBinding.bind(view)
        binding.toolbar.inflateMenu(R.menu.map_menu)
        val directPathButton = binding.toolbar.menu.findItem(R.id.menu_direct_path)
        val gridButton = binding.toolbar.menu.findItem(R.id.menu_grid)
        val myLocationButton = binding.toolbar.menu.findItem(R.id.menu_my_location)
        val locationButton = binding.toolbar.menu.findItem(R.id.menu_location)
        val mapTypeButton = binding.toolbar.menu.findItem(R.id.menu_map_type)
        val globeViewButton = binding.toolbar.menu.findItem(R.id.menu_globe_view)

        // Just that our UI tests don't have access to the nav controllers, let's don't access nav there
        val ifNavAvailable = runCatching { findNavController() }.getOrNull() != null
        val viewModel =
            if (ifNavAvailable) navGraphViewModels<MapViewModel>(R.id.map).value else MapViewModel()

        // Don't set the title as we got lots of icons
        // binding.toolbar.setTitle(R.string.map)
        binding.toolbar.setupUpNavigation { findNavController().navigateUp() }

        // Set time from Astronomy screen state if we are brought from the screen to here directly
        if (ifNavAvailable && findNavController().previousBackStackEntry?.destination?.id == R.id.astronomy) {
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
            binding.startArrow.performHapticFeedbackLongPress()
            if (mapDraw.currentMapType.isCrescentVisibility) viewModel.addDays(-1)
            else viewModel.subtractOneHour()
        }
        binding.startArrow.setOnLongClickListener { viewModel.addDays(-10); true }
        binding.endArrow.rotateTo(ArrowView.Direction.END)
        binding.endArrow.setOnClickListener {
            binding.endArrow.performHapticFeedbackLongPress()
            if (mapDraw.currentMapType.isCrescentVisibility) viewModel.addDays(1)
            else viewModel.addOneHour()
        }
        binding.endArrow.setOnLongClickListener { viewModel.addDays(10); true }
        binding.timeBar.setupLayoutTransition()
        binding.date.setOnClickListener {
            val currentJdn =
                Jdn(Date(viewModel.state.value.time).toGregorianCalendar().toCivilDate())
            showDayPickerDialog(
                activity ?: return@setOnClickListener, currentJdn, R.string.accept
            ) { jdn -> viewModel.addDays(jdn - currentJdn) }
        }
        binding.date.setOnLongClickListener { viewModel.changeToTime(Date()); true }

        fun bringGps() = activity?.let { showGPSLocationDialog(it, viewLifecycleOwner) }.let { }
        directPathButton.onClick {
            if (coordinates.value == null) bringGps() else viewModel.toggleDirectPathMode()
        }
        gridButton.onClick { viewModel.toggleDisplayGrid() }
        myLocationButton.onClick { bringGps() }
        locationButton.onClick {
            if (coordinates.value == null) bringGps() else viewModel.toggleDisplayLocation()
        }
        mapTypeButton.onClick {
            if (viewModel.state.value.mapType == MapType.None) {
                val context = context ?: return@onClick
                val options = MapType.entries
                    .drop(1) // Hide "None" option
                    // Hide moon visibilities for now unless is a development build
                    .filter { !it.isCrescentVisibility || BuildConfig.DEVELOPMENT }
                val titles = options.map { context.getString(it.title) }
                androidx.appcompat.app.AlertDialog.Builder(context)
                    .setItems(titles.toTypedArray()) { dialog, i ->
                        viewModel.changeMapType(options[i])
                        dialog.dismiss()
                    }.show()
            } else viewModel.changeMapType(MapType.None)
        }
        globeViewButton.onClick {
            val textureSize = 2048
            val bitmap = runCatching { createBitmap(textureSize, textureSize) }
                .onFailure(logException).getOrNull() ?: return@onClick
            val matrix = Matrix()
            matrix.setScale(
                textureSize.toFloat() / mapDraw.mapWidth, textureSize.toFloat() / mapDraw.mapHeight
            )
            binding.map.onDraw(Canvas(bitmap), matrix)
            showGlobeDialog(activity ?: return@onClick, bitmap) // DO NOT use bitmap after this
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
                if (abs(latitude) < 2 && abs(longitude) < 2 && viewModel.state.value.displayGrid) {
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

        // Best effort solution for landscape view till figuring out something better
        if (resources.isLandscape)
            binding.map.setBackgroundColor(view.context.resolveColor(R.attr.screenBackgroundColor))

        val showKaaba = view.context.appPrefs.getBoolean(PREF_SHOW_QIBLA_IN_COMPASS, true)

        fun onStateUpdate(state: MapState) {
            mapDraw.drawKaaba = coordinates.value != null && state.displayLocation && showKaaba
            mapDraw.updateMap(state.time, state.mapType)
            binding.map.invalidate()
            binding.date.text = mapDraw.maskFormattedTime
            binding.timeBar.isVisible = mapDraw.maskFormattedTime.isNotEmpty()
            directPathButton.icon?.alpha = if (state.isDirectPathMode) 127 else 255
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            binding.timeBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom + (16 * resources.dp).toInt()
            }
            WindowInsetsCompat.CONSUMED
        }

        // Setup view model change listener
        // https://developer.android.com/topic/libraries/architecture/coroutines#lifecycle-aware
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collectLatest(::onStateUpdate) }
                launch { coordinates.collectLatest { onStateUpdate(viewModel.state.value) } }
            }
        }
    }
}
