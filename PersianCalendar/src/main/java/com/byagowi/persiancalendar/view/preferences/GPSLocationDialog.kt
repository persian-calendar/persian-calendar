package com.byagowi.persiancalendar.view.preferences

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView

import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.util.Utils
import com.github.praytimes.Coordinate

import java.io.IOException
import java.util.Locale

import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceManager

class GPSLocationDialog : PreferenceDialogFragmentCompat() {

  private var locationManager: LocationManager? = null
  private var textView: TextView? = null

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

  private var latitude: String? = null
  private var longitude: String? = null
  private var cityName: String? = null

  override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
    super.onPrepareDialogBuilder(builder)

    textView = TextView(context)
    textView?.setPadding(32, 32, 32, 32)
    textView?.textSize = 20f
    textView?.setText(R.string.pleasewaitgps)

    locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

    getLocation()
    if (lacksPermission) {
      askForPermission()
    }

    builder?.setPositiveButton("", null)
    builder?.setNegativeButton("", null)
    builder?.setView(textView)
  }

  private fun getLocation() {
    val locationMgr = locationManager
    val ctx = context
    if (locationMgr != null && ctx != null) {
      if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
          ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        lacksPermission = true
        return
      }

      if (locationMgr.allProviders.contains(LocationManager.GPS_PROVIDER)) {
        locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
        everRegisteredCallback = true
      }

      if (locationMgr.allProviders.contains(LocationManager.NETWORK_PROVIDER)) {
        locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
        everRegisteredCallback = true
      }
    }
  }

  private fun askForPermission() =
      requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
          Constants.LOCATION_PERMISSION_REQUEST_CODE)

  private fun showLocation(location: Location) {
    latitude = String.format(Locale.ENGLISH, "%f", location.latitude)
    longitude = String.format(Locale.ENGLISH, "%f", location.longitude)
    val gcd = Geocoder(context, Locale.getDefault())
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
      result = cityName + "\n\n"
    }
    // this time, with native digits
    result += Utils.formatCoordinate(context,
        Coordinate(location.latitude, location.longitude), "\n")
    textView?.text = result
  }

  override fun onDialogClosed(positiveResult: Boolean) {
    if (latitude != null && longitude != null) {
      val preferences = PreferenceManager.getDefaultSharedPreferences(context)
      val editor = preferences.edit()
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

    if (everRegisteredCallback) {
      locationManager?.removeUpdates(locationListener)
    }

    val ctx = context
    if (ctx != null)
      LocalBroadcastManager.getInstance(ctx)
          .sendBroadcast(Intent(Constants.LOCAL_INTENT_UPDATE_PREFERENCE))
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                          grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    if (requestCode == Constants.LOCATION_PERMISSION_REQUEST_CODE) {
      getLocation()
      if (lacksPermission)
      // request for permission is rejected
        dismiss()
    }
  }
}
