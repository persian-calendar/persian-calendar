package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.Manifest
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
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.byagowi.persiancalendar.DEFAULT_CITY
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ReleaseDebugDifference.logDebug
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.askForLocationPermission
import com.byagowi.persiancalendar.utils.copyToClipboard
import com.byagowi.persiancalendar.utils.dp
import com.byagowi.persiancalendar.utils.formatCoordinate
import com.byagowi.persiancalendar.utils.formatCoordinateISO6709
import com.byagowi.persiancalendar.utils.logException
import com.google.openlocationcode.OpenLocationCode
import io.github.persiancalendar.praytimes.Coordinate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

fun Fragment.showGPSLocationDialog(): Boolean {
    runCatching { showGPSLocationDialogMain() }.onFailure(logException)
    return true
}

private fun Fragment.showGPSLocationDialogMain() {
    val activity = activity ?: return

    if (ActivityCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        askForLocationPermission(activity)
        return
    }

    var latitude: String? = null
    var longitude: String? = null
    var altitude: String? = null
    var cityName: String? = null
    val resultTextView = TextView(activity).also {
        it.setPadding(16.dp)
        it.setText(R.string.pleasewaitgps)
    }
    var isLocationShown = false

    fun showLocation(location: Location) {
        logDebug("A", "AAA")
        latitude = "%f".format(Locale.ENGLISH, location.latitude)
        longitude = "%f".format(Locale.ENGLISH, location.longitude)
        altitude = "%f".format(Locale.ENGLISH, location.altitude)

        val result = listOf(
            "",
            formatCoordinate(
                activity,
                Coordinate(location.latitude, location.longitude, location.altitude), "\n"
            ),
            formatCoordinateISO6709(location.latitude, location.longitude, location.altitude),
            "https://plus.codes/${OpenLocationCode.encode(location.latitude, location.longitude)}"
        ).joinToString("\n\n")
        resultTextView.text = result
        resultTextView.setOnClickListener {
            copyToClipboard(resultTextView, "coords", resultTextView.text, showToastInstead = true)
        }

        isLocationShown = true

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                Geocoder(context, Locale.getDefault())
                    .getFromLocation(location.latitude, location.longitude, 1)
                    .firstOrNull()?.locality?.takeIf { it.isNotEmpty() }?.also {
                        withContext(Dispatchers.Main.immediate) {
                            cityName = it
                            resultTextView.text = listOf(cityName, result).joinToString("")
                        }
                    }
            }.onFailure(logException)
        }
    }

    fun checkGPSProvider() {
        if (latitude != null && longitude != null) return

        runCatching {
            val gps = activity.getSystemService<LocationManager>()
            if (gps?.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
                AlertDialog.Builder(activity)
                    .setMessage(R.string.gps_internet_desc)
                    .setPositiveButton(R.string.accept) { _, _ ->
                        runCatching {
                            activity.startActivity(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            )
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
        override fun onLocationChanged(location: Location) = showLocation(location)
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {
            isOneProviderEnabled = true
            if (!isLocationShown)
                resultTextView.setText(R.string.pleasewaitgps)
        }

        override fun onProviderDisabled(provider: String) {
            if (!isLocationShown && !isOneProviderEnabled)
                resultTextView.setText(R.string.enable_location_services)
        }
    }

    val locationManager = activity.getSystemService<LocationManager>() ?: return
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

    handler.postDelayed(checkGPSProviderCallback, TimeUnit.SECONDS.toMillis(30))
    var dialog: AlertDialog? = null
    val lifeCyclceObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
            dialog?.dismiss()
        }
    }
    lifecycle.addObserver(lifeCyclceObserver)

    dialog = AlertDialog.Builder(activity)
        .setPositiveButton("", null)
        .setNegativeButton("", null)
        .setView(resultTextView)
        .setOnDismissListener {
            if (latitude != null && longitude != null) {
                activity.appPrefs.edit {
                    putString(PREF_LATITUDE, latitude)
                    putString(PREF_LONGITUDE, longitude)
                    putString(PREF_ALTITUDE, altitude)
                    putString(PREF_GEOCODED_CITYNAME, cityName ?: "")
                    putString(PREF_SELECTED_LOCATION, DEFAULT_CITY)
                }
            }

            activity.let { activity ->
                if (ActivityCompat.checkSelfPermission(
                        activity, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        activity, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) locationManager.removeUpdates(locationListener)
            }

            handler.removeCallbacks(checkGPSProviderCallback)
            lifecycle.removeObserver(lifeCyclceObserver)
        }
        .create()
    dialog.show()
}
