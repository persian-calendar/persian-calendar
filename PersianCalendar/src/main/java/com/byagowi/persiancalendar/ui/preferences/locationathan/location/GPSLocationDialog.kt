package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.di.dependencies.AppDependency
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency
import com.byagowi.persiancalendar.praytimes.Coordinate
import com.byagowi.persiancalendar.utils.Utils
import dagger.android.support.DaggerAppCompatDialogFragment
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GPSLocationDialog : DaggerAppCompatDialogFragment() {
    @Inject
    lateinit var appDependency: AppDependency
    @Inject
    lateinit var mainActivityDependency: MainActivityDependency
    private var locationManager: LocationManager? = null
    private var textView: TextView? = null
    private val handler = Handler()
    private var latitude: String? = null
    private var longitude: String? = null
    private var cityName: String? = null
    private val checkGPSProviderCallback = Runnable { this.checkGPSProvider() }
    private var lacksPermission = false
    private var everRegisteredCallback = false
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            if (location != null)
                showLocation(location)
        }

        override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}

        override fun onProviderEnabled(s: String) {}

        override fun onProviderDisabled(s: String) {}
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        textView = TextView(mainActivityDependency.mainActivity)
        textView!!.setPadding(32, 32, 32, 32)
        textView!!.setText(R.string.pleasewaitgps)

        locationManager = mainActivityDependency.mainActivity
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager

        getLocation()
        if (lacksPermission) {
            Utils.askForLocationPermission(mainActivityDependency.mainActivity)
        }

        handler.postDelayed(checkGPSProviderCallback, TimeUnit.SECONDS.toMillis(30))

        return AlertDialog.Builder(mainActivityDependency.mainActivity)
                .setPositiveButton("", null)
                .setNegativeButton("", null)
                .setView(textView)
                .create()
    }

    private fun checkGPSProvider() {
        if (latitude != null && longitude != null) return

        try {
            val gps = mainActivityDependency.mainActivity
                    .getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (gps != null && !gps.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                AlertDialog.Builder(mainActivityDependency.mainActivity)
                        .setMessage(R.string.gps_internet_desc)
                        .setPositiveButton(R.string.accept) { dialogInterface, i ->
                            try {
                                mainActivityDependency.mainActivity.startActivity(
                                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            } catch (ignore: Exception) {
                            }
                        }.create().show()
            }
        } catch (ignore: Exception) {
        }

    }

    private fun getLocation() {
        if (locationManager == null) {
            return
        }

        if (ActivityCompat.checkSelfPermission(mainActivityDependency.mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mainActivityDependency.mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            lacksPermission = true
            return
        }

        if (locationManager!!.allProviders.contains(LocationManager.GPS_PROVIDER)) {
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            everRegisteredCallback = true
        }

        if (locationManager!!.allProviders.contains(LocationManager.NETWORK_PROVIDER)) {
            locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
            everRegisteredCallback = true
        }
    }

    private fun showLocation(location: Location) {
        latitude = String.format(Locale.ENGLISH, "%f", location.latitude)
        longitude = String.format(Locale.ENGLISH, "%f", location.longitude)
        val gcd = Geocoder(mainActivityDependency.mainActivity, Locale.getDefault())
        val addresses: List<Address>
        try {
            addresses = gcd.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses.size > 0) {
                cityName = addresses[0].locality
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var result = ""
        if (!TextUtils.isEmpty(cityName)) {
            result = cityName!! + "\n\n"
        }
        // this time, with native digits
        result += Utils.formatCoordinate(mainActivityDependency.mainActivity,
                Coordinate(location.latitude, location.longitude,
                        location.altitude), "\n")
        textView!!.text = result
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (latitude != null && longitude != null) {
            val editor = appDependency.sharedPreferences.edit()
            editor.putString(Constants.PREF_LATITUDE, latitude)
            editor.putString(Constants.PREF_LONGITUDE, longitude)
            if (cityName != null) {
                editor.putString(Constants.PREF_GEOCODED_CITYNAME, cityName)
            } else {
                editor.putString(Constants.PREF_GEOCODED_CITYNAME, "")
            }
            editor.putString(Constants.PREF_SELECTED_LOCATION, Constants.DEFAULT_CITY)
            editor.apply()
        }

        if (everRegisteredCallback && locationManager != null) {
            locationManager!!.removeUpdates(locationListener)
        }

        handler.removeCallbacks(checkGPSProviderCallback)

        super.onDismiss(dialog)
    }

    override fun onPause() {
        dismiss()
        super.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.LOCATION_PERMISSION_REQUEST_CODE) {
            getLocation()
            if (lacksPermission)
            // request for permission is rejected
                dismiss()
        }
    }
}
