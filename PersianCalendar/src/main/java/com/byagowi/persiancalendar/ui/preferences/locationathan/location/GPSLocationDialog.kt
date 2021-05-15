package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
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
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.view.setPadding
import com.byagowi.persiancalendar.DEFAULT_CITY
import com.byagowi.persiancalendar.LOCATION_PERMISSION_REQUEST_CODE
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.askForLocationPermission
import com.byagowi.persiancalendar.utils.copyToClipboard
import com.byagowi.persiancalendar.utils.dp
import com.byagowi.persiancalendar.utils.formatCoordinate
import com.byagowi.persiancalendar.utils.formatCoordinateISO6709
import com.byagowi.persiancalendar.utils.logException
import com.google.openlocationcode.OpenLocationCode
import io.github.persiancalendar.praytimes.Coordinate
import java.util.*
import java.util.concurrent.TimeUnit

class GPSLocationDialog : AppCompatDialogFragment() {

    private var locationManager: LocationManager? = null
    private var textView: TextView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var latitude: String? = null
    private var longitude: String? = null
    private var altitude: String? = null
    private var cityName: String? = null
    private val checkGPSProviderCallback = Runnable { checkGPSProvider() }
    private var lacksPermission = false
    private var everRegisteredCallback = false
    private var isLocationShown = false
    private var isOneProviderEnabled = false
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) = showLocation(location)
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {
            isOneProviderEnabled = true
            if (!isLocationShown)
                textView?.setText(R.string.pleasewaitgps)
        }

        override fun onProviderDisabled(provider: String) {
            if (!isLocationShown && !isOneProviderEnabled)
                textView?.setText(R.string.enable_location_services)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        textView = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()

        textView = TextView(activity).also {
            it.setPadding(16.dp)
            it.setText(R.string.pleasewaitgps)
        }

        locationManager = activity.getSystemService()

        getLocation()
        if (lacksPermission) {
            askForLocationPermission(activity)
        }

        handler.postDelayed(checkGPSProviderCallback, TimeUnit.SECONDS.toMillis(30))

        return AlertDialog.Builder(activity)
            .setPositiveButton("", null)
            .setNegativeButton("", null)
            .setView(textView)
            .create()
    }

    private fun checkGPSProvider() {
        val activity = activity ?: return
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

    private fun getLocation() {
        val activity = activity ?: return
        if (ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            lacksPermission = true
            return
        }

        locationManager?.also {
            if (LocationManager.GPS_PROVIDER in it.allProviders) {
                it.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
                everRegisteredCallback = true
            }

            if (LocationManager.NETWORK_PROVIDER in it.allProviders) {
                it.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
                everRegisteredCallback = true
            }
        }
    }

    private fun showLocation(location: Location) {
        val activity = activity ?: return
        val textView = textView ?: return
        latitude = "%f".format(Locale.ENGLISH, location.latitude)
        longitude = "%f".format(Locale.ENGLISH, location.longitude)
        altitude = "%f".format(Locale.ENGLISH, location.altitude)

        val result = listOf(
            formatCoordinate(
                activity,
                Coordinate(location.latitude, location.longitude, location.altitude), "\n"
            ),
            formatCoordinateISO6709(location.latitude, location.longitude, location.altitude),
            "https://plus.codes/${OpenLocationCode.encode(location.latitude, location.longitude)}"
        ).joinToString("\n\n").trim()
        textView.text = result
        textView.setOnClickListener {
            copyToClipboard(textView, "coords", textView.text, showToastInstead = true)
        }

        isLocationShown = true

        // TODO: Rewrite with some more modern stuff
        cityName = null
        Thread {
            runCatching {
                Geocoder(context, Locale.getDefault())
                    .getFromLocation(location.latitude, location.longitude, 1)
                    .firstOrNull()?.locality?.takeIf { it.isNotEmpty() }?.also {
                        activity.runOnUiThread {
                            cityName = it
                            textView.text =
                                listOf(cityName, textView.text).joinToString("\n").trim()
                        }
                    }
            }
        }.run()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (latitude != null && longitude != null) {
            activity?.appPrefs?.edit {
                putString(PREF_LATITUDE, latitude)
                putString(PREF_LONGITUDE, longitude)
                putString(PREF_ALTITUDE, altitude)
                putString(PREF_GEOCODED_CITYNAME, cityName ?: "")
                putString(PREF_SELECTED_LOCATION, DEFAULT_CITY)
            }
        }

        activity?.let { activity ->
            if (ActivityCompat.checkSelfPermission(
                    activity, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    activity, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) locationManager?.removeUpdates(locationListener)
        }

        handler.removeCallbacks(checkGPSProviderCallback)

        super.onDismiss(dialog)
    }

    override fun onPause() {
        dismiss()
        super.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            getLocation()
            // request for permission is rejected
            if (lacksPermission)
                dismiss()
        }
    }
}
