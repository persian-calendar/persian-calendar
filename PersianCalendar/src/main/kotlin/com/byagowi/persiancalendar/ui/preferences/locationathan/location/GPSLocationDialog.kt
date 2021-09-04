package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.byagowi.persiancalendar.DEFAULT_CITY
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.Variants.logDebug
import com.byagowi.persiancalendar.databinding.GpsLocationDialogBinding
import com.byagowi.persiancalendar.ui.utils.askForLocationPermission
import com.byagowi.persiancalendar.ui.utils.copyToClipboard
import com.byagowi.persiancalendar.utils.THIRTY_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.formatCoordinate
import com.byagowi.persiancalendar.utils.formatCoordinateISO6709
import com.byagowi.persiancalendar.utils.language
import com.byagowi.persiancalendar.utils.logException
import com.google.openlocationcode.OpenLocationCode
import io.github.persiancalendar.praytimes.Coordinate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import java.util.*

fun showGPSLocationDialog(activity: Activity?, viewLifecycleOwner: LifecycleOwner) {
    activity ?: return
    if (ActivityCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        activity.askForLocationPermission()
        return
    }

    val coordinatesFlow = MutableStateFlow<Coordinate?>(null)
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
        .onEach { logDebug("GPSLocationDialog: A location is received") }
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
                logDebug("Geocoder country code ${countryCode ?: "empty"}")
                logDebug("Geocoder locality ${result?.locality ?: "empty"}")
                result?.locality?.takeIf { it.isNotEmpty() }
            }.onFailure(logException).getOrNull()
        }
        .flowOn(Dispatchers.IO)
        .onEach { locality ->
            logDebug("GPSLocationDialog: A geocoder locality result is received")
            binding.cityName.isVisible = true
            binding.cityName.text = locality
            cityName = locality
        }
        .flowOn(Dispatchers.Main.immediate)
        .catch { logException(it) }
        .launchIn(viewLifecycleScope)

    val locationManager = activity.getSystemService<LocationManager>() ?: return

    fun checkGPSProvider() {
        if (coordinatesFlow.value != null) return

        runCatching {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                AlertDialog.Builder(activity)
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
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onLocationChanged(location: Location) {
            coordinatesFlow.value =
                Coordinate(location.latitude, location.longitude, location.altitude)
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
    val dialog = AlertDialog.Builder(activity)
        .setPositiveButton("", null)
        .setNegativeButton("", null)
        .setView(binding.root)
        .create()

    val lifeCycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) dialog.dismiss()
    }

    viewLifecycleOwner.lifecycle.addObserver(lifeCycleObserver)
    dialog.setOnDismissListener {
        logDebug("GPSLocationDialog: Dialog is dismissed")
        coordinatesFlow.value?.let { coordinate ->
            activity.appPrefs.edit {
                putString(PREF_LATITUDE, "%f".format(Locale.ENGLISH, coordinate.latitude))
                putString(PREF_LONGITUDE, "%f".format(Locale.ENGLISH, coordinate.longitude))
                // Don't store elevation on Iranian cities, it degrades the calculations quality
                val elevation = if (countryCode == "IR") .0 else coordinate.elevation
                putString(PREF_ALTITUDE, "%f".format(Locale.ENGLISH, elevation))
                putString(PREF_GEOCODED_CITYNAME, cityName ?: "")
                putString(PREF_SELECTED_LOCATION, DEFAULT_CITY)
            }
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
