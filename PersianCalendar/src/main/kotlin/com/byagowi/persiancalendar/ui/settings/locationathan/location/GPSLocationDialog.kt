package com.byagowi.persiancalendar.ui.settings.locationathan.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.GpsLocationDialogBinding
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.utils.askForLocationPermission
import com.byagowi.persiancalendar.ui.utils.copyToClipboard
import com.byagowi.persiancalendar.utils.THIRTY_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.formatCoordinate
import com.byagowi.persiancalendar.utils.formatCoordinateISO6709
import com.byagowi.persiancalendar.utils.friendlyName
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.saveLocation
import com.byagowi.persiancalendar.variants.debugLog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.openlocationcode.OpenLocationCode
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

fun showGPSLocationDialog(activity: FragmentActivity, viewLifecycleOwner: LifecycleOwner) {
    if (ActivityCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        activity.askForLocationPermission()
        return
    }

    val coordinatesFlow = MutableStateFlow<Coordinates?>(null)
    var cityName: String? = null
    var countryCode: String? = null
    val binding = GpsLocationDialogBinding.inflate(activity.layoutInflater)
    listOf(binding.cityName, binding.coordinates, binding.coordinatesIso6709, binding.plusLink)
        .forEach { it.setOnClickListener { _ -> activity.copyToClipboard(it.text) } }

    // This is preference fragment view lifecycle but ideally we should've used
    // dialog's view lifecycle which resultTextView.findViewTreeLifecycleOwner()
    // won't give it to us probably because AlertDialog isn't fragment based
    val viewLifecycleScope = viewLifecycleOwner.lifecycleScope

    val distinctCoordinatesFlow = coordinatesFlow
        .filterNotNull()
        .onEach { debugLog("GPSLocationDialog: A location is received") }
        .distinctUntilChanged { old, new ->
            old.latitude == new.latitude && old.longitude == new.longitude &&
                    old.elevation == new.elevation
        }

    val updateCoordinatesJob = distinctCoordinatesFlow
        .onEach { coordinates ->
            binding.message.isVisible = false
            binding.coordinatesBox.isVisible = true
            binding.coordinates.text = formatCoordinate(activity, coordinates, "\n")
            binding.coordinatesIso6709.text = formatCoordinateISO6709(
                coordinates.latitude, coordinates.longitude, coordinates.elevation
            )
            binding.plusLink.text = listOf(
                "https://plus.codes/",
                OpenLocationCode.encode(coordinates.latitude, coordinates.longitude)
            ).joinToString("")
        }
        .flowOn(Dispatchers.Main.immediate)
        .catch { logException(it) }
        .launchIn(viewLifecycleScope)

    val updateGeocoderResultJob = distinctCoordinatesFlow
        .mapNotNull { coordinates ->
            runCatching {
                val result = Geocoder(activity, language.asSystemLocale())
                    .getFromLocation(coordinates.latitude, coordinates.longitude, 1)
                    .firstOrNull()
                countryCode = result?.countryCode
                result?.friendlyName
            }.onFailure(logException).getOrNull()
        }
        .flowOn(Dispatchers.IO)
        .onEach { friendlyLocationName ->
            binding.cityName.isVisible = true
            binding.cityName.text = friendlyLocationName
            cityName = friendlyLocationName
        }
        .flowOn(Dispatchers.Main.immediate)
        .catch { logException(it) }
        .launchIn(viewLifecycleScope)

    val locationManager = activity.getSystemService<LocationManager>() ?: return

    fun checkGPSProvider() {
        if (coordinatesFlow.value != null) return

        runCatching {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                MaterialAlertDialogBuilder(activity)
                    .setMessage(R.string.gps_internet_desc)
                    .setPositiveButton(R.string.accept) { _, _ ->
                        runCatching {
                            activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }.onFailure(logException)
                    }
                    .show()
            }
        }.onFailure(logException)
    }

    val handler = Handler(Looper.getMainLooper())
    val checkGPSProviderCallback = Runnable { checkGPSProvider() }
    var isOneProviderEnabled = false
    val locationListener = object : LocationListener {
        @Deprecated("")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onLocationChanged(location: Location) {
            coordinatesFlow.value =
                Coordinates(location.latitude, location.longitude, location.altitude)
        }

        override fun onProviderEnabled(provider: String) {
            isOneProviderEnabled = true
            binding.message.setText(R.string.pleasewaitgps)
        }

        override fun onProviderDisabled(provider: String) {
            if (!isOneProviderEnabled) binding.message.setText(R.string.enable_location_services)
        }
    }

    if (LocationManager.GPS_PROVIDER in locationManager.allProviders) {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 0, 0f, locationListener
        )
    }
    if (LocationManager.NETWORK_PROVIDER in locationManager.allProviders) {
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener
        )
    }

    handler.postDelayed(checkGPSProviderCallback, THIRTY_SECONDS_IN_MILLIS)
    val dialog = MaterialAlertDialogBuilder(activity)
        .setView(binding.root)
        .create()

    val lifeCycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) dialog.dismiss()
    }

    viewLifecycleOwner.lifecycle.addObserver(lifeCycleObserver)
    dialog.setOnDismissListener {
        debugLog("GPSLocationDialog: Dialog is dismissed")
        coordinatesFlow.value?.let { coordinate ->
            activity.appPrefs.saveLocation(coordinate, cityName ?: "", countryCode ?: "")
        }

        // AGP 7 has false alarms claims removeUpdates can't be called here
        // but that's incorrect and we are exactly checking permission before
        // trying to remove the listener
        @SuppressLint("MissingPermission")
        if (ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) locationManager.removeUpdates(locationListener)

        handler.removeCallbacks(checkGPSProviderCallback)
        viewLifecycleOwner.lifecycle.removeObserver(lifeCycleObserver)

        // This wasn't necessary if we had a proper view lifecycle scope, yet, this is more
        // preferred than going for a fragment based dialog for now.
        updateCoordinatesJob.cancel()
        updateGeocoderResultJob.cancel()
    }

    dialog.show()
}
